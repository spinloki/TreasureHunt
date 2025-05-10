package spinloki.treasurehunt.campaign.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import spinloki.treasurehunt.campaign.intel.events.TreasureHuntEventIntel;

public class THResetProgressAbility extends BaseDurationAbility {
    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded){
        float pad = 10f;
        tooltip.addPara("Abandons the current lead in your Treasure Hunt" +
                        " by resetting event progress to zero." +
                        " Do this if you would rather look for something else." +
                        " The item in question will not appear as a lead again until" +
                        " all other special items have been found." +
                        " If the item is a blueprint, it will never reappear as a lead.", pad
        );
    }

    @Override
    protected void activateImpl() {
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel()) {
            if (intel instanceof TreasureHuntEventIntel) {
                ((TreasureHuntEventIntel) intel).setProgress(0);
            }
        }
    }

    @Override
    protected void applyEffect(float amount, float level) {

    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }
}
