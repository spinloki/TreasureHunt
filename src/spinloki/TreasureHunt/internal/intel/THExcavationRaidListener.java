package spinloki.TreasureHunt.internal.intel;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;

import java.util.List;
import java.util.Map;

public class THExcavationRaidListener implements GroundRaidObjectivesListener {

    @Override
    public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity,
                                     List<GroundRaidObjectivePlugin> objectives,
                                     RaidType type, int marineTokens, int iteration) {
        if (iteration != 0) return;
        if (entity == null) return;

        Object flag = entity.getMemoryWithoutUpdate().get("$th_excavation_ground_ops");
        if (!Boolean.TRUE.equals(flag)) return;

        objectives.add(new THExcavationRaidObjective(market));
    }

    @Override
    public void reportRaidObjectivesAchieved(RaidResultData raidData,
                                             InteractionDialogAPI dialog,
                                             Map<String, MemoryAPI> memoryMap) {
        // Rewards handled by thExcavation_raidCont in rules.csv
    }
}
