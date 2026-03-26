package spinloki.TreasureHunt.internal.events;

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
import spinloki.TreasureHunt.internal.factors.THColonyRuinFactor;
import spinloki.TreasureHunt.internal.factors.THSalvageFactor;
import spinloki.TreasureHunt.internal.factors.THTimeFactor;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.util.LinkedList;
import java.util.Queue;

public class THFactorTracker implements ShowLootListener, PlayerColonizationListener, EveryFrameScript {
    // Threshold (in days) for determining if a market was recently raided by the player
    private static final float RAID_DETECTION_THRESHOLD = 0.3f;

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
            value = (int) THRegistry.getSettings().getExplorationValues().get(settingsId);
        } catch (JSONException e) {
            value = defaultVal;
        }
        return value;
    }

    THRaidTracker mRaidTracker;

    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        if (dialog == null) {
            return;
        }
        var entity = dialog.getInteractionTarget();
        if (entity == null) {
            return;
        }
        if (entity instanceof CampaignFleetAPI fleet) {
            if (fleet.getBattle() != null) { // Shouldn't be null, but I've seen it be null sometimes when using nuke command
                for (var enemyFleet : fleet.getBattle().getNonPlayerSide()) {
                    if (THUtils.isScavenger(enemyFleet)) {
                        queueFactorForDestroyingFleet(enemyFleet);
                    }
                }
            } else if (THUtils.isScavenger(fleet)) { // Fallback. Unfortunately, player won't get credit for other fleets that participate
                queueFactorForDestroyingFleet(fleet);
            }
            return;
        }
        var customEntityType = entity.getCustomEntityType();
        if (customEntityType != null && THRegistry.getRewardRegistry().hasReward(customEntityType)) {
            mFactors.add(new THSalvageFactor(THRegistry.getRewardRegistry().getRewardValue(customEntityType), THRegistry.getRewardRegistry().getRewardDescription(customEntityType)));
        } else if (entity.getMarket() != null) {
            var market = entity.getMarket();
            if (Misc.getDaysSinceLastRaided(market) < RAID_DETECTION_THRESHOLD) {
                if (mRaidTracker == null) {
                    mRaidTracker = new THRaidTracker();
                }
                var baseReward = THRegistry.getRewardRegistry().getRewardValue("raid");
                var reward = mRaidTracker.calculateRaidReward(baseReward, market, Global.getSector().getClock().getTimestamp());
                mFactors.add(new THSalvageFactor(reward, THRegistry.getRewardRegistry().getRewardDescription("raid")));
            } else if (Misc.hasRuins(market)) {
                var ruin = Misc.getRuinsType(market);
                mFactors.add(new THSalvageFactor(THRegistry.getRewardRegistry().getRewardValue(ruin), THRegistry.getRewardRegistry().getRewardDescription(ruin)));
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
         if (THRegistry.getSettings().isDebugUseTimeFactor()){
            timePassed += amount;
             float interval = 1;
             if (timePassed > interval){
                timePassed = 0;
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(THRegistry.getSettings().getDebugTimeFactorPoints()), null);
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
