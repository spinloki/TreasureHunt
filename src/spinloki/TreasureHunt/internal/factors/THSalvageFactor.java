package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import spinloki.TreasureHunt.util.THUtils;

public class THSalvageFactor extends BaseOneTimeFactor {
    public THSalvageFactor(int points, String type) {
        super(points);
        this.points = THUtils.applyBoost(points);
        mType = type;
    }

    private final String mType;

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Gained info by " + mType;
    }

}
