package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import spinloki.TreasureHunt.config.THSettings;

import java.util.HashMap;

public class THRaidTracker {
    protected HashMap<MarketAPI, Long> marketRaidLog = new HashMap<>();

    int calculateRaidReward(int baseReward, MarketAPI market, long timestamp){
        var lastTimestamp = marketRaidLog.put(market, timestamp);

        if (!THSettings.TH_RAID_DIMINISHING_RETURNS_ENABLED || lastTimestamp == null){
            return baseReward;
        }

        var daysSinceLastRaidReward = Global.getSector().getClock().getElapsedDaysSince(lastTimestamp);
        var diminishedReward = baseReward * THSettings.TH_RAID_DIMINISHING_RETURNS_FACTOR;
        var rewardRecoveryFactor = Math.min(1f, daysSinceLastRaidReward / THSettings.TH_RAID_DIMINISHING_RETURNS_RECOVERY_DAYS);
        var reward = diminishedReward + (baseReward - diminishedReward) * rewardRecoveryFactor;
        return (int) reward;
    }
}
