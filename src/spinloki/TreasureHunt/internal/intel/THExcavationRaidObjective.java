package spinloki.TreasureHunt.internal.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.graid.AbstractGoalGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;

import java.util.Random;

public class THExcavationRaidObjective extends AbstractGoalGroundRaidObjectivePluginImpl {

    public THExcavationRaidObjective(MarketAPI market) {
        super(market, RaidDangerLevel.HIGH);
    }

    @Override
    public String getName() {
        return "Excavation Site";
    }

    @Override
    public String getIconName() {
        return Global.getSettings().getCommoditySpec("hand_weapons").getIconName();
    }

    @Override
    public int performRaid(CargoAPI loot, Random random, float raidMult, TextPanelAPI text) {
        if (text != null) {
            text.addPara("Your marines break through automated defenses and secure the excavation site.");
        }
        int xp = 500;
        xpGained = xp;
        return xp;
    }
}
