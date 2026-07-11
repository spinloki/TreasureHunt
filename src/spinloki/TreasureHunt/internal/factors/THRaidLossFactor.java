package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class THRaidLossFactor extends BaseOneTimeFactor {

    private final String colonyName;

    public THRaidLossFactor(int penalty, String colonyName) {
        super(-Math.abs(penalty));
        this.colonyName = colonyName;
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Clan raid on " + colonyName;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Salvor clan raiders struck your colony of %s, making off with data and "
                                + "salvage that would have advanced your hunt.", 0f,
                        Misc.getHighlightColor(), colonyName);
            }
        };
    }
}
