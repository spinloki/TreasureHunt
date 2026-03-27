package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

public class THExcavationRaidFactor extends BaseOneTimeFactor {

    public THExcavationRaidFactor(int points) {
        super(points);
        int boost = (int) Misc.getFleetwideTotalMod(Global.getSector().getPlayerFleet(), THUtils.TH_TREASURE_HUNT_BOOST, 0);
        float mult = THRegistry.getSettings().getTreasureHuntPackageMaxMult();
        this.points = (int) Math.min(points * mult, points + boost);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Claimed an excavation site";
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Recovered valuable data by claiming a scavenger excavation site.", 0f);
            }
        };
    }
}
