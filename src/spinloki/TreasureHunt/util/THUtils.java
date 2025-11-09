package spinloki.TreasureHunt.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

public class THUtils {
    public static final String TH_TREASURE_HUNT_BOOST = "th_treasure_hunt_boost";

    public static final String TH_BLUEPRINT_PACKAGE = "th_blueprint_package";
    public static final String TH_SPECIAL_ITEM = "th_special_item";

    public static final String TH_TAG = "Treasure Hunt";

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

    public static Set<StarSystemAPI> getRandomUninhabitedSystemsWithStablePoints(int count) {
        Stack<StarSystemAPI> good_candidates = new Stack<>();
        Stack<StarSystemAPI> bad_candidates = new Stack<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            // skip core worlds or claimed systems
            if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
            if (system.getCenter() != null){
                if (Misc.getClaimingFaction(system.getCenter()) != null) continue;
            }

            boolean hasRelay = !system.getEntitiesWithTag(Tags.COMM_RELAY).isEmpty();
            boolean hasStable = !system.getEntitiesWithTag(Tags.STABLE_LOCATION).isEmpty();

            if (hasStable) bad_candidates.push(system);
            if (hasRelay) good_candidates.push(system); // weight comm relays extra
        }

        Collections.shuffle(good_candidates, new Random());
        Collections.shuffle(bad_candidates, new Random());

        count = Math.min(Math.min(count, good_candidates.size()), bad_candidates.size());

        Set<StarSystemAPI> systems = new HashSet<>();
        for (int i = 0; i < count; i++) {
            Stack<StarSystemAPI> candidates = new Random().nextBoolean() ? good_candidates : bad_candidates;
            systems.add(candidates.pop());
        }
        return systems;
    }
}
