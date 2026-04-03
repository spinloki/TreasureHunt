package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import spinloki.TreasureHunt.util.THUtils;

public class THExcavationRaidFactor extends BaseOneTimeFactor {

    public THExcavationRaidFactor(int points) {
        super(points);
        this.points = THUtils.applyBoost(points);
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
