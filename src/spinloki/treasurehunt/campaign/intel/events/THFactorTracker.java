package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import spinloki.treasurehunt.config.Settings;

import java.util.LinkedList;
import java.util.Queue;

public class THFactorTracker implements ShowLootListener, PlayerColonizationListener, EveryFrameScript {
    public THFactorTracker(){

    }

    // Sigmoid because I'm FANCY even though a straight line clamping from 15 to 50 would work exactly as well
    private static final double K = 0.00001;     // Steepness
    private static final double X0 = 250000.0;  // Midpoint at 250k base value
    private static float calculateProgressFromBaseValue(float baseValue) {
        double sigmoid = 1.0 / (1.0 + Math.exp(-K * ((double) baseValue - X0)));
        int min = getExplorationPointValue("scav_kill_min", 1);
        int max = getExplorationPointValue("scav_kill_max", 50);
        double scaled = min + (max - min) * sigmoid;
        return (float) Math.round(scaled);
    }

    public void queueFactorForDestroyingFleet(CampaignFleetAPI fleet){
        float value = 0;
        for (var fleetMember : Misc.getSnapshotMembersLost(fleet)){
            value += fleetMember.getHullSpec().getBaseValue();
        }
        mFactors.add(new THSalvageFactor((int) calculateProgressFromBaseValue(value), "destroying a scavenger fleet"));

    }

    protected static int getExplorationPointValue(String settingsId, int defaultVal){
        int value;
        try{
            value = (int) Settings.TH_EXPLORATION_VALUES.get(settingsId);
        } catch (JSONException e) {
            value = defaultVal;
        }
        return value;
    }

    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        var entity = dialog.getInteractionTarget();
        if (entity == null){
            return;
        }
        if (entity instanceof CampaignFleetAPI fleet){
            if (fleet.getBattle() != null){ // Shouldn't be null, but I've seen it be null sometimes when using nuke command
                for (var enemyFleet : fleet.getBattle().getNonPlayerSide()){
                    if (Misc.isScavenger(enemyFleet)){
                        queueFactorForDestroyingFleet(enemyFleet);
                    }
                }
            }
            else if (Misc.isScavenger(fleet)){ // Fallback. Unfortunately, player won't get credit for other fleets that participate
                queueFactorForDestroyingFleet(fleet);
            }
            return;
        }
        var customDescId = entity.getCustomDescriptionId();
        if (Settings.customDescriptionIdHasTHReward(customDescId)){
            mFactors.add(new THSalvageFactor(Settings.getTHRewardValue(customDescId), Settings.getTHRewardDescription(customDescId)));
        }
        else if (entity instanceof PlanetAPI planet){
            var market = planet.getMarket();
            if (Misc.getDaysSinceLastRaided(market) < .3){
                mFactors.add(new THSalvageFactor(getExplorationPointValue("raid", 15), "raiding a colony"));
            }

            else if (Misc.hasRuins(market)){
                var ruin = Misc.getRuinsType(market);
                mFactors.add(new THSalvageFactor(Settings.getTHRewardValue(ruin), Settings.getTHRewardDescription(ruin)));
            }
        }
    }

    private THSalvageFactor mFactor;
    private Queue<THSalvageFactor> mFactors = new LinkedList<>();
    private float interval = 1;
    private float timePassed = 0;

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused(){
        return false;
    }

    public void advance(float amount){
         if (Settings.TH_DEBUG_USE_TIME_FACTOR){
            timePassed += amount;
            if (timePassed > interval){
                timePassed = 0;
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(Settings.TH_DEBUG_TIME_FACTOR_POINTS), null);
            }
        }
        while (!mFactors.isEmpty()){
            TreasureHuntEventIntel.addFactorCreateIfNecessary(mFactors.poll(), null);
        }
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planet) {
        var market = planet.getMarket();
        if (Misc.hasRuins(market)){
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THColonyRuinFactor(market), null);
        }
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI colony) {
        // No need to do anything. Factor will expire by itself when the player abandons the colony
    }
}
