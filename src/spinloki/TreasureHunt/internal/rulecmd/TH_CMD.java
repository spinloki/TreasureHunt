package spinloki.TreasureHunt.internal.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.api.ITHOpportunity;
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

        SectorEntityToken entity = dialog.getInteractionTarget();
        if (entity == null) return false;

        String action = params.get(0).getString(memoryMap);

        MemoryAPI memory = memoryMap.get(MemKeys.LOCAL);
        if (memory == null) return false;

        return switch (action) {
            case "computeDataStats"          -> computeDataStats(entity, memory);
            case "getScavengerData"          -> getScavengerData(entity, dialog);
            case "determineScavengerVoice"   -> determineScavengerVoice(entity, memory);
            case "isScavenger"               -> entity instanceof CampaignFleetAPI fleet && THUtils.isScavenger(fleet);
            case "excavationRaidReward"      -> excavationRaidReward(entity, dialog);
            case "excavationBombardMenu"     -> excavationBombardMenu(entity, memory);
            case "excavationBombardShowText" -> excavationBombardShowText(entity, dialog);
            case "excavationBombard"         -> excavationBombard(entity, dialog);
            case "excavationConvinceDefender"-> excavationConvinceDefender(entity);
            case "excavationTrickDefenders"  -> excavationTrickDefenders(entity, dialog);
            case "salvorClanTrigger"         -> salvorClanTrigger(dialog);
            default -> false;
        };
    }

    private boolean computeDataStats(SectorEntityToken entity, MemoryAPI memory) {
        if (!(entity instanceof CampaignFleetAPI fleet)) return false;
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

    private boolean getScavengerData(SectorEntityToken entity, InteractionDialogAPI dialog) {
        if (!(entity instanceof CampaignFleetAPI fleet)) return false;
        int points = getDataPoints(fleet.getFleetPoints());
        if (points > 0) {
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THScavengerDataFactor(points), dialog);
        }
        return true;
    }

    private boolean determineScavengerVoice(SectorEntityToken entity, MemoryAPI memory) {
        if (!(entity instanceof CampaignFleetAPI fleet)) return false;
        String voice = determineScavengerVoice(fleet);
        memory.set("$th_scav_voice", voice);
        memory.set("$th_scav_faction", fleet.getFaction().getId());
        return true;
    }

    private boolean excavationRaidReward(SectorEntityToken entity, InteractionDialogAPI dialog) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
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
        cleanUpExcavationFlags(entity);
        return true;
    }

    private boolean excavationBombardMenu(SectorEntityToken entity, MemoryAPI memory) {
        int fuelCost = THRegistry.getSettings().getExcavationBombardFuelCost();
        float playerFuel = Global.getSector().getPlayerFleet().getCargo().getFuel();
        memory.set("$th_bombard_cost_text", "" + fuelCost);
        if (playerFuel < fuelCost) {
            entity.getMemoryWithoutUpdate().set("$th_bombard_cant_afford", true);
        } else {
            entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
        }
        return true;
    }

    private boolean excavationBombardShowText(SectorEntityToken entity, InteractionDialogAPI dialog) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
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
    }

    private boolean excavationBombard(SectorEntityToken entity, InteractionDialogAPI dialog) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        int fuelCost = THRegistry.getSettings().getExcavationBombardFuelCost();
        pf.getCargo().removeFuel(fuelCost);
        com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity.addCommodityLossText(
                "fuel", fuelCost, dialog.getTextPanel());
        cleanUpExcavationFlags(entity);
        return true;
    }

    private boolean excavationConvinceDefender(SectorEntityToken entity) {
        if (!(entity instanceof CampaignFleetAPI fleet)) return true;
        for (var intel : Global.getSector().getIntelManager().getIntel(
                spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel.class)) {
            var excIntel = (spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel) intel;
            if (excIntel.getDefenderFleet() == fleet) {
                excIntel.convinceDefenderToLeave();
                break;
            }
        }
        return true;
    }

    private boolean excavationTrickDefenders(SectorEntityToken entity, InteractionDialogAPI dialog) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
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
        cleanUpExcavationFlags(entity);
        return true;
    }

    private boolean salvorClanTrigger(InteractionDialogAPI dialog) {
        ITHOpportunity opportunity = THRegistry.getOpportunityRegistry().pickCandidate();
        if (opportunity != null) {
            opportunity.trigger();
            String iconPath;
            try {
                iconPath = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, opportunity.getIcon());
            } catch (Exception e) {
                iconPath = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, "found_opportunity");
            }
            var tooltip = dialog.getTextPanel().beginTooltip();
            var imgText = tooltip.beginImageWithText(iconPath, 40f);
            imgText.addPara("Opportunity found: " + opportunity.getDisplayName(), Misc.getHighlightColor(), 0f);
            tooltip.addImageWithText(0f);
            dialog.getTextPanel().addTooltip();
        }
        return true;
    }

    private void cleanUpExcavationFlags(SectorEntityToken entity) {
        entity.getMemoryWithoutUpdate().unset("$th_excavation_ground_ops");
        entity.getMemoryWithoutUpdate().unset("$th_excavation_faction");
        entity.getMemoryWithoutUpdate().unset("$th_excavation_faction_the");
        entity.getMemoryWithoutUpdate().unset("$th_bombard_cant_afford");
        entity.getMemoryWithoutUpdate().unset("$th_bombard_cost_text");
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
