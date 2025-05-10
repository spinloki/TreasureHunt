package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class THScavengerDataFactor extends BaseOneTimeFactor {

    public THScavengerDataFactor(int points) {
        super(points);
        int boost = (int) Misc.getFleetwideTotalMod(Global.getSector().getPlayerFleet(), THConstants.TH_TREASURE_HUNT_BOOST, 0);
        this.points = Math.min(points * 3, points + boost);
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

