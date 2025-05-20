package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import spinloki.treasurehunt.config.Settings;
import spinloki.treasurehunt.util.THUtils;

import java.awt.*;

public class THColonyRuinFactor extends BaseEventFactor {
    MarketAPI colony;
    int BASE_PROGRESS;
    String ruins_type;
    boolean ENABLED = true;
    THColonyRuinFactor(MarketAPI colony){
        super();
        this.colony = colony;
        ruins_type = Misc.getRuinsType(colony);
        try {
            BASE_PROGRESS = Settings.TH_EXPLORATION_VALUES.getInt(ruins_type) / Settings.TH_COLONY_RUINS_BASE_PROGRESS_DIVISOR;
        } catch (JSONException e) {
            BASE_PROGRESS = 10;
        }
    }

    @Override
    public boolean shouldShow(BaseEventIntel intel) {
        return ENABLED;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                Color h = Misc.getHighlightColor();

                tooltip.addPara("Information gathered from your colony, %s, built over long-dead ruins.", opad, h, colony.getName());

                String tense = THUtils.hasTechMining(colony) ? "is being" : "can be";
                tooltip.addPara("Will contribute %s points per month to the treasure hunt, which %s " +
                        "boosted by the tech mining industry.", opad, h, "" + getProgress(intel), tense);
            }

        };
    }

    @Override
    public String getDesc(BaseEventIntel intel){
        return "Ruins at " + colony.getName();
    }

    @Override
    public int getProgress(BaseEventIntel intel){
        if (!ENABLED) return 0;
        int progress = BASE_PROGRESS;
        if (THUtils.hasTechMining(colony)){
            progress = BASE_PROGRESS * Settings.TH_COLONY_TECH_MINING_PROGRESS_MULTIPLIER;
        }
        return progress;
    }

    @Override
    public boolean isExpired() {
        return (!colony.isPlayerOwned()); // If the player abandons or otherwise loses the colony
    }
}
