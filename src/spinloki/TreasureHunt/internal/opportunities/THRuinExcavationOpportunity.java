package spinloki.TreasureHunt.internal.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.*;

public class THRuinExcavationOpportunity extends BaseTHOpportunity {

    @Override
    public float getProbabilityWeight() {
        if (!hasValidTargetPlanets()) return 0;
        return super.getProbabilityWeight();
    }

    @Override
    public void trigger() {
        super.trigger();
        PlanetAPI planet = pickTargetPlanet();
        if (planet == null) return;

        String factionId = pickFaction(planet);
        if (factionId == null) return;

        new THRuinExcavationIntel(planet, factionId, getIconPath());
    }

    @Override
    public String getDisplayName() {
        return "Ruin Excavation";
    }

    @Override
    public String getIcon() {
        return "ruin_excavation";
    }

    private PlanetAPI pickTargetPlanet() {
        WeightedRandomPicker<PlanetAPI> vastPicker = new WeightedRandomPicker<>();
        WeightedRandomPicker<PlanetAPI> extensivePicker = new WeightedRandomPicker<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!isValidTargetSystem(system)) continue;

            for (PlanetAPI planet : system.getPlanets()) {
                if (!isValidTargetPlanet(planet)) continue;

                String ruinsType = Misc.getRuinsType(planet.getMarket());
                switch (ruinsType) {
                    case "ruins_vast" -> vastPicker.add(planet);
                    case "ruins_extensive" -> extensivePicker.add(planet);
                    default -> {} // scattered/widespread not eligible
                }
            }
        }

        if (!vastPicker.isEmpty()) return vastPicker.pick();
        return extensivePicker.pick(); // returns null if empty
    }

    private boolean hasValidTargetPlanets() {
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!isValidTargetSystem(system)) continue;

            for (PlanetAPI planet : system.getPlanets()) {
                if (!isValidTargetPlanet(planet)) continue;
                String ruinsType = Misc.getRuinsType(planet.getMarket());
                if ("ruins_vast".equals(ruinsType) || "ruins_extensive".equals(ruinsType)) return true;
            }
        }
        return false;
    }

    private boolean isValidTargetSystem(StarSystemAPI system) {
        if (!system.isProcgen()) return false;
        if (system.isEnteredByPlayer()) return false;
        if (system.hasPulsar()) return false;
        if (system.hasTag(Tags.THEME_REMNANT_MAIN)) return false;
        if (system.hasTag(Tags.THEME_REMNANT_SECONDARY)) return false;
        // Story-critical systems: red planet, PK system, TT black site, Limbo, etc.
        if (system.hasTag(Tags.THEME_SPECIAL)) return false;
        return true;
    }

    private boolean isValidTargetPlanet(PlanetAPI planet) {
        if (planet.isStar() || planet.isGasGiant()) return false;
        // Excludes the planetary shield "red planet" and other story planets
        if (planet.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) return false;

        MarketAPI market = planet.getMarket();
        if (market == null) return false;
        if (market.isPlayerOwned()) return false;
        if (!market.isPlanetConditionMarketOnly()) return false;
        if (!Misc.hasRuins(market)) return false;

        // Skip planets already targeted by an active excavation
        if (planet.getMemoryWithoutUpdate().getBoolean("$th_excavation_blocked")) return false;
        if (planet.getMemoryWithoutUpdate().getBoolean("$th_excavation_ground_ops")) return false;
        return true;
    }

    /**
     * Picks a faction using randomized round-robin from registered factions,
     * weighted by proximity to the target planet's system.
     */
    private String pickFaction(PlanetAPI planet) {
        var allFactions = THRegistry.getFactionRegistry().getAll();
        if (allFactions.isEmpty()) return null;

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (var entry : allFactions.entrySet()) {
            String factionId = entry.getKey();
            THFactionConfig config = entry.getValue();
            String marketFaction = config.getMarketFactionIdOrDefault(factionId);
            // Check that the faction has at least one market to plausibly source from
            boolean hasMarket = Global.getSector().getEconomy().getMarketsCopy().stream()
                    .anyMatch(m -> !m.isHidden() && marketFaction.equals(m.getFactionId()));
            if (!hasMarket) continue;

            float distLY = Misc.getDistanceLY(
                    planet.getStarSystem().getLocation(),
                    getClosestMarketLocation(marketFaction, planet));
            // Closer factions get higher weight
            float weight = 1f / Math.max(1f, distLY);
            picker.add(factionId, weight);
        }

        return picker.pick();
    }

    private org.lwjgl.util.vector.Vector2f getClosestMarketLocation(String factionId, PlanetAPI planet) {
        org.lwjgl.util.vector.Vector2f systemLoc = planet.getStarSystem().getLocation();
        float bestDist = Float.MAX_VALUE;
        org.lwjgl.util.vector.Vector2f bestLoc = systemLoc;

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!factionId.equals(market.getFactionId())) continue;
            if (market.isHidden()) continue;
            float dist = Misc.getDistanceLY(systemLoc, market.getLocationInHyperspace());
            if (dist < bestDist) {
                bestDist = dist;
                bestLoc = market.getLocationInHyperspace();
            }
        }
        return bestLoc;
    }
}
