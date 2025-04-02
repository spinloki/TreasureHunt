package spinloki.treasurehunt;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;

public class THSalvageFactor extends BaseOneTimeFactor {
    public THSalvageFactor(int points, String type) {
        super(points);
        mType = type;
    }

    private final String mType;

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Gained info by " + mType;
    }

}
