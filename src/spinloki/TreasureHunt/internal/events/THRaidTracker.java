package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.HashMap;

public class THRaidTracker {
    protected HashMap<MarketAPI, Long> marketRaidLog = new HashMap<>();

    int calculateRaidReward(int baseReward, MarketAPI market, long timestamp){
        var lastTimestamp = marketRaidLog.put(market, timestamp);

        if (!THRegistry.getSettings().isRaidDiminishingReturnsEnabled() || lastTimestamp == null){
            return baseReward;
        }

        var daysSinceLastRaidReward = Global.getSector().getClock().getElapsedDaysSince(lastTimestamp);
        var diminishedReward = baseReward * THRegistry.getSettings().getRaidDiminishingReturnsFactor();
        var rewardRecoveryFactor = Math.min(1f, daysSinceLastRaidReward / THRegistry.getSettings().getRaidDiminishingReturnsRecoveryDays());
        var reward = diminishedReward + (baseReward - diminishedReward) * rewardRecoveryFactor;
        return (int) reward;
    }
}
