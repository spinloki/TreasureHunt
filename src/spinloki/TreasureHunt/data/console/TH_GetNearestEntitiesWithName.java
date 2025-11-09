package spinloki.TreasureHunt.data.console;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.BaseCommandWithSuggestion;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.util.THUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TH_GetNearestEntitiesWithName implements BaseCommandWithSuggestion {

    private static final List<String> SUGGESTED_NAMES = List.of(
            "Comm Relay", "Nav Buoy", "Sensor Array",
            "Stable Location", "Research Station", "Mining Station",
            "Inactive Gate", "Weapons Cache"
    );

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: This command is campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        int n = 10;
        String nameFilter = "";

        try {
            if (args != null && !args.trim().isEmpty()) {
                // Use regex to split while respecting quoted text
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("([^\"]\\S*|\".+?\")\\s*")
                        .matcher(args);

                List<String> tokens = new ArrayList<>();
                while (matcher.find()) {
                    tokens.add(matcher.group(1));
                }

                for (String token : tokens) {
                    if (token.toLowerCase().startsWith("n=")) {
                        n = Math.max(1, Integer.parseInt(token.substring(2)));
                    } else if (token.toLowerCase().startsWith("name=")) {
                        nameFilter = token.substring(5).replaceAll("^\"|\"$", "");
                    } else if (token.matches("\\d+")) {
                        n = Integer.parseInt(token);
                    } else {
                        nameFilter = token.replaceAll("^\"|\"$", "");
                    }
                }
            }
        } catch (Exception ex) {
            Console.showMessage("Usage: TH_GetNearestEntitiesWithName [n=1] [name=\"substring\"]\n" +
                    "Example: TH_GetNearestEntitiesWithName n=10 \"Research Station\"");
            return CommandResult.BAD_SYNTAX;
        }

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        List<SectorEntityToken> matches = THUtils.getNearestEntitiesWithName(player, n, nameFilter);

        if (matches.isEmpty()) {
            Console.showMessage("No entities found" +
                    (nameFilter.isEmpty() ? "" : " containing \"" + nameFilter + "\"") + ".");
            return CommandResult.SUCCESS;
        }

        Console.showMessage("Closest " + matches.size() +
                " entities" + (nameFilter.isEmpty() ? "" : " matching \"" + nameFilter + "\"") + ":");
        Console.showMessage(String.format("%-16s | %-28s | %-20s | %s", "ID", "Name", "Location", "Dist (SU)"));
        Console.showMessage("-------------------------------------------------------------------------------");

        for (SectorEntityToken e : matches) {
            String id = e.getId() != null ? truncate(e.getId()) : "(no id)";
            String nm = e.getFullName();
            String where = e.getContainingLocation() != null ? e.getContainingLocation().getName() : "(unknown)";
            int dist;
            if (player.getStarSystem() == e.getStarSystem()){
                dist = (int) Misc.getDistance(player, e);
            }
            else{
                dist = (int) Misc.getDistance(player.getLocationInHyperspace(), e.getLocationInHyperspace());
            }

            Console.showMessage(String.format("%-16s | %-40s | %-20s | %5d", id, nm, where, dist));
        }

        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> getSuggestions(int parameter, List<String> previous, CommandContext context) {
        if (!context.isInCampaign()) return new ArrayList<>();

        if (parameter == 0) {
            return List.of("n=1", "n=3", "n=5", "n=10");
        } else if (parameter == 1) {
            return SUGGESTED_NAMES.stream()
                    .map(name -> "\"" + name + "\"")
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private static String truncate(String id) {
        return id.length() > 14 ? id.substring(0, 12) + "…" : id;
    }
}