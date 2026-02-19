package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;

public class THAbandonCarryoverFactor  extends BaseOneTimeFactor {
    public THAbandonCarryoverFactor(int points) {
        super(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Relevant information from abandoned hunt";
    }
}