package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;

public class THSalvageFactor extends BaseOneTimeFactor {
    public THSalvageFactor(int points, String type) {
        super(points);
        int boost = (int) Misc.getFleetwideTotalMod(Global.getSector().getPlayerFleet(), THUtils.TH_TREASURE_HUNT_BOOST, 0);
        this.points = Math.min(points * 3, points + boost);
        mType = type;
    }

    private final String mType;

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Gained info by " + mType;
    }

}
