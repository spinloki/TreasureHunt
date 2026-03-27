package spinloki.TreasureHunt.internal.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.factors.THExcavationRaidFactor;
import spinloki.TreasureHunt.internal.factors.THScavengerDataFactor;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class TH_CMD extends BaseCommandPlugin {


    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity == null) return false;

        String action = params.get(0).getString(memoryMap);

        MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
        if (memory == null) return false; // should not be possible unless there are other big problems already

        if ("computeDataStats".equals(action)) {
            if (entity instanceof CampaignFleetAPI) {
                CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
                float fp = fleet.getFleetPoints();
                int cost = getDataCost(fp);
                memory.set("$th_dataCost", Misc.getWithDGS(cost));
                int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
                if (credits < cost) {
                    memory.set("$th_cant_afford", true);
                } else {
                    memory.unset("$th_cant_afford");
                }
                return true;
            }
            return false;
        } else if ("getScavengerData".equals(action)) {
            if (entity instanceof CampaignFleetAPI) {
                CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
                float fp = fleet.getFleetPoints();

                int points = getDataPoints(fp);
                if (points > 0) {
                    TreasureHuntEventIntel.addFactorCreateIfNecessary(new THScavengerDataFactor(points), dialog);
                }
                return true;
            }
            return false;
        } else if ("determineScavengerVoice".equals(action)) {
            if (entity instanceof CampaignFleetAPI) {
                CampaignFleetAPI fleet = (CampaignFleetAPI) entity;

                String voice = determineScavengerVoice(fleet);

                // Store into LOCAL memory so rules can check $th_scav_voice == soldier, etc.
                memory.set("$th_scav_voice", voice);

                // Optional: also expose faction for rules if you want combined branching
                memory.set("$th_scav_faction", fleet.getFaction().getId());

                return true;
            }
            return false;
        } else if ("isScavenger".equals(action)) {
            if (entity instanceof CampaignFleetAPI fleet) {
                return THUtils.isScavenger(fleet);
            }
            return false;
        } else if ("excavationRaidReward".equals(action)) {
            int progressPoints = THRegistry.getSettings().getExcavationProgressPoints();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(
                    new THExcavationRaidFactor(progressPoints), dialog);

            String rewardName = "salvaged materials";
            TreasureHuntEventIntel intel = TreasureHuntEventIntel.get();
            if (intel != null) {
                var items = intel.getRandomRewardItems(1);
                for (String itemId : items) {
                    pf.getCargo().addSpecial(new SpecialItemData(itemId, null), 1);
                    rewardName = THUtils.getSpecialItemDisplayName(itemId);
                }
            }
            dialog.getTextPanel().addPara("Your survey teams explore the excavation site and recover the " +
                    rewardName + " that the operatives were defending.");
            dialog.getTextPanel().highlightInLastPara(Misc.getHighlightColor(), rewardName);
            // Clean up ground ops flags
            entity.getMemoryWithoutUpdate().unset("$th_excavation_ground_ops");
            entity.getMemoryWithoutUpdate().unset("$th_excavation_faction");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cost_text");
            return true;
        } else if ("excavationBombardMenu".equals(action)) {
            int fuelCost = THRegistry.getSettings().getExcavationBombardFuelCost();
            float playerFuel = Global.getSector().getPlayerFleet().getCargo().getFuel();
            memory.set("$th_bombard_cost_text", "" + fuelCost);
            if (playerFuel < fuelCost) {
                entity.getMemoryWithoutUpdate().set("$th_bombard_cant_afford", true);
            } else {
                entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
            }
            return true;
        } else if ("excavationBombardShowText".equals(action)) {
            String factionName = entity.getMemoryWithoutUpdate().getString("$th_excavation_faction");
            if (factionName == null) factionName = "Unknown";
            int fuelCost = THRegistry.getSettings().getExcavationBombardFuelCost();
            float playerFuel = pf.getCargo().getFuel();
            if (playerFuel < fuelCost) {
                dialog.getTextPanel().addPara("You consider bombarding the " + factionName +
                        " excavation site from orbit, however you lack the " + fuelCost + " fuel it would require. ");
            } else {
                dialog.getTextPanel().addPara("You consider bombarding the " + factionName +
                        " excavation site from orbit. This will cost " + fuelCost + " fuel. " +
                        "It will eliminate the operatives below, but will also destroy " +
                        "whatever they have found at their excavation site.");
            }
            dialog.getTextPanel().highlightInLastPara(Misc.getHighlightColor(), fuelCost + " fuel");
            return true;
        } else if ("excavationBombard".equals(action)) {
            int fuelCost = THRegistry.getSettings().getExcavationBombardFuelCost();
            pf.getCargo().removeFuel(fuelCost);
            com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity.addCommodityLossText(
                    "fuel", fuelCost, dialog.getTextPanel());
            // Clean up ground ops flags
            entity.getMemoryWithoutUpdate().unset("$th_excavation_ground_ops");
            entity.getMemoryWithoutUpdate().unset("$th_excavation_faction");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cost_text");
            return true;
        } else if ("excavationConvinceDefender".equals(action)) {
            if (entity instanceof CampaignFleetAPI fleet) {
                // Find the intel matching this defender fleet
                for (var intel : Global.getSector().getIntelManager().getIntel(
                        spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel.class)) {
                    var excIntel = (spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel) intel;
                    if (excIntel.getDefenderFleet() == fleet) {
                        excIntel.convinceDefenderToLeave();
                        break;
                    }
                }
            }
            return true;
        } else if ("excavationTrickDefenders".equals(action)) {
            int progressPoints = THRegistry.getSettings().getExcavationProgressPoints();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(
                    new THExcavationRaidFactor(progressPoints), dialog);

            String rewardName = "salvaged materials";
            TreasureHuntEventIntel intel = TreasureHuntEventIntel.get();
            if (intel != null) {
                Random rewardRng = new Random(entity.getId() != null ? entity.getId().hashCode() : 0);
                var items = intel.getRandomRewardItems(1, rewardRng);
                for (String itemId : items) {
                    pf.getCargo().addSpecial(new SpecialItemData(itemId, null), 1);
                    rewardName = THUtils.getSpecialItemDisplayName(itemId);
                }
            }
            dialog.getTextPanel().addPara("Your survey teams move through the abandoned " +
                    "fortifications into the excavation site and find a " + rewardName + ".");
            dialog.getTextPanel().highlightInLastPara(Misc.getHighlightColor(), rewardName);
            // Clean up ground ops flags
            entity.getMemoryWithoutUpdate().unset("$th_excavation_ground_ops");
            entity.getMemoryWithoutUpdate().unset("$th_excavation_faction");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cost_text");
            return true;
        }

        return false;
    }

    public static int getDataCost(float fp) {
        return getDataPoints(fp) * THRegistry.getSettings().getScavengerDataCreditsPerPoint();
    }

    public static int getDataPoints(float fp) {
        float min = THRegistry.getSettings().getScavengerDataMinPoints();
        float max = THRegistry.getSettings().getScavengerDataMaxPoints();

        float f = Math.max(fp - THRegistry.getSettings().getScavengerMinFp(), 0) / (THRegistry.getSettings().getScavengerMaxFp() - THRegistry.getSettings().getScavengerMinFp());
        if (f > 1f) f = 1f;
        if (f < 0f) f = 0f;

        int result = Math.round(min + (max - min) * f);
        return result;
    }

    public static String determineScavengerVoice(CampaignFleetAPI fleet) {
        String factionId = (fleet != null && fleet.getFaction() != null) ? fleet.getFaction().getId() : null;

        if (factionId == null) return Voices.SPACER;

        return switch (factionId) {
            case Factions.PIRATES -> Voices.VILLAIN;
            case Factions.LUDDIC_PATH -> Voices.PATHER;
            case Factions.LUDDIC_CHURCH, Factions.KOL -> Voices.FAITHFUL;
            case Factions.HEGEMONY, Factions.PERSEAN -> Voices.OFFICIAL;
            case Factions.TRITACHYON -> Voices.BUSINESS;
            default -> Voices.SPACER;
        };
    }
}
