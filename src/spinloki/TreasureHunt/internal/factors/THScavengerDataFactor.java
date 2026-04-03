package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import spinloki.TreasureHunt.util.THUtils;

public class THScavengerDataFactor extends BaseOneTimeFactor {

    public THScavengerDataFactor(int points) {
        super(points);
        this.points = THUtils.applyBoost(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Bought treasure hunt data";
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Salvage data bought from a fellow treasure hunter, far out in the black.",
                        0f);
            }

        };
    }

}

