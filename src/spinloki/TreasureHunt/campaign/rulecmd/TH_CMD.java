package spinloki.TreasureHunt.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.events.THScavengerDataFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class TH_CMD extends BaseCommandPlugin {

    public static int CREDITS_PER_TD_POINT = 2000;

    public static float MIN_SCAVENGER_FP = 50;
    public static float MAX_SCAVENGER_FP = 150;

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
        }

        return false;
    }

    public static int getDataCost(float fp) {
        return getDataPoints(fp) * CREDITS_PER_TD_POINT;
    }

    public static int getDataPoints(float fp) {
        float min = 20;
        float max = 40;

        float f = Math.max(fp - MIN_SCAVENGER_FP, 0) / (MAX_SCAVENGER_FP - MIN_SCAVENGER_FP);
        if (f > 1f) f = 1f;
        if (f < 0f) f = 0f;

        int result = Math.round(min + (max - min) * f);
        return result;
    }

    public static String determineScavengerVoice(CampaignFleetAPI fleet) {
        String factionId = (fleet != null && fleet.getFaction() != null) ? fleet.getFaction().getId() : null;

        long seed = 0L;
        if (fleet != null) {
            seed = (fleet.getId() != null) ? fleet.getId().hashCode() : System.identityHashCode(fleet);
        }
        Random rand = new Random(seed);

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

    private static String pick(Random rand, String... options) {
        return options[rand.nextInt(options.length)];
    }
}
