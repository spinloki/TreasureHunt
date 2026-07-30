package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Usage: th_testexcavation [survey|explore|colonize] [factionId]
 * Stages an excavation in the named market state and teleports there. Default is colonize.
 */
public class TH_TestExcavation implements BaseCommand {

    private enum State { SURVEY, EXPLORE, COLONIZE }

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        State state = State.COLONIZE;
        String factionId = null;

        for (String token : (args == null ? "" : args.trim()).split("\\s+")) {
            if (token.isEmpty()) continue;
            switch (token.toLowerCase()) {
                case "survey" -> state = State.SURVEY;
                case "explore" -> state = State.EXPLORE;
                case "colonize" -> state = State.COLONIZE;
                default -> factionId = token;
            }
        }

        PlanetAPI planet = pickCandidate();
        if (planet == null) {
            Console.showMessage("Error: no eligible vast/extensive ruins planet found. "
                    + "Every candidate may already be excavated (th_testexcavation clears nothing).");
            return CommandResult.ERROR;
        }

        if (factionId == null) factionId = pickFaction();
        if (factionId == null) {
            Console.showMessage("Error: no registered Treasure Hunt faction with a market to source from.");
            return CommandResult.ERROR;
        }
        if (Global.getSector().getFaction(factionId) == null) {
            Console.showMessage("Error: unknown faction id \"" + factionId + "\".");
            return CommandResult.ERROR;
        }

        MarketAPI market = planet.getMarket();
        applyState(market, state);

        String iconPath;
        try {
            iconPath = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, "ruin_excavation");
        } catch (Exception e) {
            iconPath = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, "found_opportunity");
        }

        new THRuinExcavationIntel(planet, factionId, iconPath);

        teleportTo(planet);

        Console.showMessage(String.format(
                "Excavation staged on %s (%s) in %s, faction %s.",
                planet.getName(), Misc.getRuinsType(market),
                planet.getStarSystem().getName(), factionId));
        Console.showMessage(String.format(
                "State: surveyed=%s, ruinsExplored=%s -> expect the \"%s\" option path.",
                market.getSurveyLevel() == MarketAPI.SurveyLevel.FULL,
                !Misc.hasUnexploredRuins(market),
                expectedOption(state)));
        Console.showMessage("Station is live. Options are blocked until you destroy it.");
        Console.showMessage("Teleported to the planet.");
        return CommandResult.SUCCESS;
    }

    private void applyState(MarketAPI market, State state) {
        switch (state) {
            case SURVEY -> {
                market.setSurveyLevel(MarketAPI.SurveyLevel.NONE);
                market.getMemoryWithoutUpdate().unset("$surveyed");
                market.getMemoryWithoutUpdate().unset("$ruinsExplored");
            }
            case EXPLORE -> {
                market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                market.getMemoryWithoutUpdate().set("$surveyed", true);
                market.getMemoryWithoutUpdate().unset("$ruinsExplored");
            }
            case COLONIZE -> {
                market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                market.getMemoryWithoutUpdate().set("$surveyed", true);
                market.getMemoryWithoutUpdate().set("$ruinsExplored", true);
            }
        }
    }

    private String expectedOption(State state) {
        return switch (state) {
            case SURVEY -> "Perform a survey";
            case EXPLORE -> "Explore the ruins";
            case COLONIZE -> "Establish a colony";
        };
    }

    private PlanetAPI pickCandidate() {
        List<PlanetAPI> vast = new ArrayList<>();
        List<PlanetAPI> extensive = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!system.isProcgen()) continue;
            if (system.hasTag(Tags.THEME_SPECIAL)) continue;
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar() || planet.isGasGiant()) continue;
                if (planet.hasTag(Tags.NOT_RANDOM_MISSION_TARGET)) continue;
                MarketAPI m = planet.getMarket();
                if (!THUtils.isUncolonizedRuinsWorld(m)) continue;
                var mem = planet.getMemoryWithoutUpdate();
                if (mem.getBoolean("$th_excavation_blocked")) continue;
                if (mem.getBoolean("$th_excavation_ground_ops")) continue;
                if (mem.getBoolean(THUtils.MEMORY_KEY_EXCAVATION_DONE)) continue;
                switch (Misc.getRuinsType(m)) {
                    case "ruins_vast" -> vast.add(planet);
                    case "ruins_extensive" -> extensive.add(planet);
                    default -> {}
                }
            }
        }
        Random r = new Random();
        if (!vast.isEmpty()) return vast.get(r.nextInt(vast.size()));
        if (!extensive.isEmpty()) return extensive.get(r.nextInt(extensive.size()));
        return null;
    }

    private String pickFaction() {
        for (var entry : THRegistry.getFactionRegistry().getAll().entrySet()) {
            THFactionConfig config = entry.getValue();
            String marketFaction = config.getMarketFactionIdOrDefault(entry.getKey());
            boolean hasMarket = Global.getSector().getEconomy().getMarketsCopy().stream()
                    .anyMatch(m -> !m.isHidden() && marketFaction.equals(m.getFactionId()));
            if (hasMarket) return entry.getKey();
        }
        return null;
    }

    private void teleportTo(PlanetAPI planet) {
        StarSystemAPI system = planet.getStarSystem();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI from = player.getContainingLocation();
        if (from != null && from != system) from.removeEntity(player);
        if (player.getContainingLocation() != system) system.addEntity(player);
        player.setLocation(planet.getLocation().x + 150f, planet.getLocation().y + 150f);
        Global.getSector().setCurrentLocation(system);
    }
}
