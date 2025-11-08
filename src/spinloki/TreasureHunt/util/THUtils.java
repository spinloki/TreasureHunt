package spinloki.TreasureHunt.util;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;

public class THUtils {
    public static final String TH_TREASURE_HUNT_BOOST = "th_treasure_hunt_boost";

    public static final String TH_BLUEPRINT_PACKAGE = "th_blueprint_package";
    public static final String TH_SPECIAL_ITEM = "th_special_item";

    public static boolean hasTechMining(MarketAPI market) {
        boolean techMining = false;
        for (Industry curr : market.getIndustries()) {
            if (curr.getSpec().hasTag(Industries.TECHMINING)) {
                techMining = true;
            }
        }
        return techMining;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
