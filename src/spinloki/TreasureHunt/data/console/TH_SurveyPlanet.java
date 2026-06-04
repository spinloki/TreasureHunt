package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.lazywizard.console.BaseCommandWithSuggestion;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;

public class TH_SurveyPlanet implements BaseCommandWithSuggestion {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        String name = args == null ? "" : args.trim();
        if (name.isEmpty()) return CommandResult.BAD_SYNTAX;
        // Strip surrounding quotes if present
        if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }

        PlanetAPI match = findPlanetByName(name);
        if (match == null) {
            Console.showMessage("No unsurveyed planet found matching \"" + name + "\".");
            return CommandResult.ERROR;
        }

        MarketAPI market = match.getMarket();
        if (market == null) {
            Console.showMessage("Planet " + match.getName() + " has no market — cannot mark as surveyed.");
            return CommandResult.ERROR;
        }

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.getMemoryWithoutUpdate().set("$surveyed", true);

        Console.showMessage("Marked " + match.getName() + " in "
                + match.getStarSystem().getNameWithLowercaseTypeShort() + " as fully surveyed.");
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> getSuggestions(int parameter, List<String> previous, CommandContext context) {
        if (parameter != 0 || !context.isInCampaign()) return new ArrayList<>();

        List<String> suggestions = new ArrayList<>();
        for (PlanetAPI planet : collectUnsurveyedPlanets()) {
            suggestions.add(planet.getName());
        }
        return suggestions;
    }

    private static PlanetAPI findPlanetByName(String name) {
        String needle = name.toLowerCase();
        PlanetAPI exact = null;
        PlanetAPI partial = null;
        for (PlanetAPI planet : collectUnsurveyedPlanets()) {
            String pn = planet.getName().toLowerCase();
            if (pn.equals(needle)) {
                exact = planet;
                break;
            }
            if (partial == null && pn.contains(needle)) {
                partial = planet;
            }
        }
        return exact != null ? exact : partial;
    }

    private static List<PlanetAPI> collectUnsurveyedPlanets() {
        List<PlanetAPI> result = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar()) continue;
                MarketAPI market = planet.getMarket();
                if (market == null) continue;
                if (market.getSurveyLevel() == MarketAPI.SurveyLevel.FULL) continue;
                result.add(planet);
            }
        }
        return result;
    }
}
