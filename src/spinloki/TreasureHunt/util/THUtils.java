package spinloki.TreasureHunt.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.*;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class THUtils {
    public static final String TH_TREASURE_HUNT_BOOST = "th_treasure_hunt_boost";
    public static final String TH_TAG = "Treasure Hunt";
    public static final String MEMORY_KEY_TH_SCAVENGER = "$isThScavenger";

    public static final String MISSING_IMAGE_FALLBACK = "graphics/icons/campaign/major_bad_event.png";

    public static boolean isScavenger(CampaignFleetAPI fleet) {
        return fleet.getMemoryWithoutUpdate().getBoolean(MEMORY_KEY_TH_SCAVENGER) ||
                Misc.isScavenger(fleet);
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

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    public static Set<StarSystemAPI> getRandomUninhabitedSystemsWithStablePoints(int count) {
        List<StarSystemAPI> good_candidates = new ArrayList<>();
        List<StarSystemAPI> bad_candidates = new ArrayList<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            // skip core worlds or claimed systems
            if (system.hasTag(Tags.THEME_CORE_POPULATED)) continue;
            if (system.getCenter() != null){
                if (Misc.getClaimingFaction(system.getCenter()) != null) continue;
            }

            boolean hasRelay = !system.getEntitiesWithTag(Tags.COMM_RELAY).isEmpty();
            boolean hasStable = !system.getEntitiesWithTag(Tags.STABLE_LOCATION).isEmpty();

            if (hasRelay) {
                good_candidates.add(system);
            }
            else if (hasStable) {
                bad_candidates.add(system);
            }
        }

        Collections.shuffle(good_candidates, new Random());
        Collections.shuffle(bad_candidates, new Random());

        count = Math.min(Math.min(count, good_candidates.size()), bad_candidates.size());

        Set<StarSystemAPI> systems = new HashSet<>();
        for (int i = 0; i < count; i++) {
            List<StarSystemAPI> candidates = new Random().nextBoolean() ? good_candidates : bad_candidates;
            systems.add(candidates.remove(candidates.size() - 1));
        }
        return systems;
    }

    // Same blacklist logic used in GoTo.getSuggestions()
    private static final List<String> BLACKLIST = List.of(
            "magiclib_campaign_trail_custom_entity",
            "nex_mining_gui_dummy",
            "luna_campaign_renderer",
            "orbital_junk"
    );

    public static List<SectorEntityToken> getNearestEntitiesWithName(
            CampaignFleetAPI player, int n, String nameFilter){
        return getNearestEntitiesWithName(player, n, nameFilter, false);
    }

    /**
     * Finds up to N nearest entities across all star systems (and hyperspace), starting
     * with the player's current system (if any). Systems are processed in order of
     * squared hyperspace distance to the player for efficiency.
     */
    public static List<SectorEntityToken> getNearestEntitiesWithName(
            CampaignFleetAPI player, int n, String nameFilter, boolean strictMatch)
    {
        if (player == null)
            return List.of();

        final StarSystemAPI playerSystem = player.getStarSystem();
        final Vector2f playerHyperspaceLoc = player.getLocationInHyperspace();
        final String needle = nameFilter == null ? "" : nameFilter.trim().toLowerCase(Locale.ROOT);

        List<SectorEntityToken> results = new ArrayList<>();

        List<StarSystemAPI> systems = new ArrayList<>(Global.getSector().getStarSystems());
        systems.sort(Comparator.comparingDouble(sys ->
                MathUtils.getDistanceSquared(playerHyperspaceLoc, sys.getLocation())));

        for (StarSystemAPI system : systems)
        {
            List<SectorEntityToken> matches = system.getAllEntities().stream()
                    .filter(e -> !BLACKLIST.contains(e.getId()))
                    .filter(THUtils::isRelevantEntity)
                    .filter(e -> {
                        String nm = e.getFullName();
                        return nm != null && (needle.isEmpty() ||
                                (!strictMatch && nm.toLowerCase(Locale.ROOT).contains(needle)) ||
                                (strictMatch && nm.equals(nameFilter)));
                    })
                    // Sort distances differently depending on whether we're in the same system
                    .sorted(Comparator.comparingDouble(e -> {
                        if (system == playerSystem)
                            return MathUtils.getDistanceSquared(player, e);
                        else
                            return MathUtils.getDistanceSquared(playerHyperspaceLoc, system.getLocation());
                    }))
                    .toList();

            results.addAll(matches);
        }

        results.sort(Comparator.comparingDouble(e -> getCrossSystemDistance(player, e)));

        return results.stream().limit(n).toList();
    }

    public static String getSpecialItemDisplayName(String specialItemId) {
        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(specialItemId);
        if (spec != null) {
            return spec.getName();
        } else {
            return specialItemId; // fallback if not found
        }
    }

    // --- Helpers -----------------------------------------------------------

    /**
     * Computes a consistent "distance" metric between the player and an entity,
     * even across systems.
     */
    public static double getCrossSystemDistance(CampaignFleetAPI player, SectorEntityToken entity)
    {
        Vector2f playerHS = player.getLocationInHyperspace();
        if (entity == null) return Double.MAX_VALUE;
        if (entity.isInHyperspace()) {
            return MathUtils.getDistanceSquared(player.getLocationInHyperspace(), entity.getLocationInHyperspace());
        }
        if (player.getStarSystem() == entity.getStarSystem()) {
            return 0.0;
        }
        Vector2f entityHS = entity.getStarSystem().getLocation();
        return MathUtils.getDistanceSquared(playerHS, entityHS);
    }

    private static boolean isRelevantEntity(SectorEntityToken e)
    {
        if (e == null) return false;
        if (e.isExpired()) return false;

        if (e instanceof CampaignAsteroid) return false;
        if (e instanceof CampaignTerrain) return false;
        if (e instanceof RingBandAPI) return false;

        if (e.getCustomEntitySpec() == null &&
                !(e instanceof CampaignFleetAPI) &&
                !(e instanceof PlanetAPI) &&
                !(e instanceof JumpPointAPI))
            return false;

        return true;
    }
}

