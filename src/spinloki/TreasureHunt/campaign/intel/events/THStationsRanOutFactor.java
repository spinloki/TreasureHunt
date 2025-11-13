package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;

public class THStationsRanOutFactor extends BaseOneTimeFactor {
    public THStationsRanOutFactor(int points){
        super(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "There are no derelict stations left in the Sector";
    }
}
