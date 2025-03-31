package spinloki.treasurehunt;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;

public class THTimeFactor extends BaseOneTimeFactor {
    public THTimeFactor(int points) {
        super(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "ARRR YA GONNA FIND THAT TREASURE MATEY";
    }

}
