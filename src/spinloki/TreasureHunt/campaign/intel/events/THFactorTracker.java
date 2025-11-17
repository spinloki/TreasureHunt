package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import spinloki.TreasureHunt.config.THSettings;
import spinloki.TreasureHunt.util.THUtils;

import java.util.LinkedList;
import java.util.Queue;

public class THFactorTracker implements ShowLootListener, PlayerColonizationListener, EveryFrameScript {
    public THFactorTracker(){

    }

    private static float calculateProgressFromBaseValue(float baseValue) {
        int min = getExplorationPointValue("scav_kill_min", 1);
        int max = getExplorationPointValue("scav_kill_max", 50);
        float scale = 10000.0F;
        float value = baseValue / scale;
        return (float) THUtils.clamp(Math.round(value), min, max);
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
            value = (int) THSettings.TH_EXPLORATION_VALUES.get(settingsId);
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
        var customEntityType = entity.getCustomEntityType();
        if (THSettings.customEntityTypeHasTHReward(customEntityType)){
            mFactors.add(new THSalvageFactor(THSettings.getTHRewardValue(customEntityType), THSettings.getTHRewardDescription(customEntityType)));
        }
        else if (entity.getMarket() != null){
            var market = entity.getMarket();
            if (Misc.getDaysSinceLastRaided(market) < .3){ // figure .3 should be a reasonable value to determine that the player raided the market
                mFactors.add(new THSalvageFactor(THSettings.getTHRewardValue("raid"), THSettings.getTHRewardDescription("raid")));
            }
            else if (Misc.hasRuins(market)){
                var ruin = Misc.getRuinsType(market);
                mFactors.add(new THSalvageFactor(THSettings.getTHRewardValue(ruin), THSettings.getTHRewardDescription(ruin)));
            }
        }
    }

    private final Queue<THSalvageFactor> mFactors = new LinkedList<>();
    private float timePassed = 0;

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused(){
        return false;
    }

    public void advance(float amount){
         if (THSettings.TH_DEBUG_USE_TIME_FACTOR){
            timePassed += amount;
             float interval = 1;
             if (timePassed > interval){
                timePassed = 0;
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(THSettings.TH_DEBUG_TIME_FACTOR_POINTS), null);
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

    public Queue<THSalvageFactor> getmFactors() {
        return mFactors;
    }
}
