package spinloki.TreasureHunt.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.events.THScavengerDataFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;

import java.util.List;
import java.util.Map;

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

        if ("hasRecentReadingsNearby".equals(action)) {
            return HyperspaceTopographyEventIntel.hasRecentReadingsNearPlayer();
        } else if ("computeDataStats".equals(action)) {
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
}
