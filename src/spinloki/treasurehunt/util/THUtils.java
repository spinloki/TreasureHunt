package spinloki.treasurehunt.util;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

public class THUtils {
    public static final String TH_TREASURE_HUNT_BOOST = "th_treasure_hunt_boost";

    public static enum TreasureType{
        ITEM,
        SHIP_BLUEPRINT
    }

    public static boolean hasTechMining(MarketAPI market) {
        boolean techMining = false;
        for (Industry curr : market.getIndustries()) {
            if (curr.getSpec().hasTag(Industries.TECHMINING)) {
                techMining = true;
            }
        }
        return techMining;
    }
}
