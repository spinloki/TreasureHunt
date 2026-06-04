package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommandWithSuggestion;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.api.ITHClaimHandler;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Debug command to trigger any registered Treasure Hunt claim handler.
 *
 * Usage:
 * TH_TriggerClaimHandler <handler name> [treasureId]
 *
 * If treasureId is omitted, defaults to pristine_nanoforge.
 */
public class TH_TriggerClaimHandler implements BaseCommandWithSuggestion {

    private static final String DEFAULT_TREASURE_ID = "pristine_nanoforge";

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        List<ITHClaimHandler> handlers = THRegistry.getClaimHandlerRegistry().getAll();
        if (handlers.isEmpty()) {
            Console.showMessage("No claim handlers are registered.");
            return CommandResult.ERROR;
        }

        if (args.isBlank()) {
            Console.showMessage("Usage: TH_TriggerClaimHandler <handler name> [treasureId]");
            Console.showMessage("Default treasureId: " + DEFAULT_TREASURE_ID);
            Console.showMessage("Available claim handlers:");
            for (ITHClaimHandler handler : handlers) {
                String status = handler.getProbabilityWeight() > 0 ? "available" : "unavailable (weight=0)";
                Console.showMessage("  " + handler.getDisplayName() + " [" + status + "]");
            }
            return CommandResult.BAD_SYNTAX;
        }

        String input = args.trim();
        String treasureId = DEFAULT_TREASURE_ID;
        String handlerQuery = input;

        int lastSpace = input.lastIndexOf(' ');
        if (lastSpace > 0) {
            String maybeTreasure = input.substring(lastSpace + 1).trim();
            if (!maybeTreasure.isEmpty() && Global.getSettings().getSpecialItemSpec(maybeTreasure) != null) {
                treasureId = maybeTreasure;
                handlerQuery = input.substring(0, lastSpace).trim();
            }
        }

        if (handlerQuery.isEmpty()) {
            Console.showMessage("Please provide a claim handler name.");
            return CommandResult.BAD_SYNTAX;
        }

        ITHClaimHandler match = findHandler(handlers, handlerQuery);
        if (match == null) {
            Console.showMessage("No claim handler found matching '" + handlerQuery + "'. Available:");
            for (ITHClaimHandler handler : handlers) {
                Console.showMessage("  " + handler.getDisplayName());
            }
            return CommandResult.ERROR;
        }

        if (Global.getSettings().getSpecialItemSpec(treasureId) == null) {
            Console.showMessage("Invalid treasure id: " + treasureId);
            return CommandResult.ERROR;
        }

        if (match.getProbabilityWeight() <= 0) {
            Console.showMessage("Claim handler '" + match.getDisplayName() + "' has zero probability weight and cannot be triggered.");
            return CommandResult.ERROR;
        }

        match.trigger(treasureId);
        Console.showMessage("Triggered claim handler: " + match.getDisplayName() + " (treasure=" + treasureId + ")");
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> getSuggestions(int parameter, List<String> previous, CommandContext context) {
        if (!context.isInCampaign()) return new ArrayList<>();

        if (parameter == 0) {
            return THRegistry.getClaimHandlerRegistry().getAll().stream()
                    .filter(handler -> handler.getProbabilityWeight() > 0)
                    .map(ITHClaimHandler::getDisplayName)
                    .collect(Collectors.toList());
        }

        if (parameter == 1) {
            Set<String> ids = new LinkedHashSet<>();
            ids.add(DEFAULT_TREASURE_ID);
            ids.addAll(THRegistry.getRewardRegistry().getOneTimeItems());
            ids.addAll(THRegistry.getRewardRegistry().getRepeatItems());
            return new ArrayList<>(ids);
        }

        return new ArrayList<>();
    }

    private ITHClaimHandler findHandler(List<ITHClaimHandler> handlers, String query) {
        String needle = query.toLowerCase();

        for (ITHClaimHandler handler : handlers) {
            if (handler.getDisplayName().toLowerCase().equals(needle)) {
                return handler;
            }
        }

        for (ITHClaimHandler handler : handlers) {
            if (handler.getDisplayName().toLowerCase().contains(needle)) {
                return handler;
            }
        }

        return null;
    }
}
