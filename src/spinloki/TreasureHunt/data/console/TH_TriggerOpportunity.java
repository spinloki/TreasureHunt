package spinloki.TreasureHunt.data.console;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.BaseCommandWithSuggestion;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TH_TriggerOpportunity implements BaseCommandWithSuggestion {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        List<ITHOpportunity> opportunities = THRegistry.getOpportunityRegistry().getAll();
        if (opportunities.isEmpty()) {
            Console.showMessage("No opportunities are registered.");
            return CommandResult.ERROR;
        }

        if (args.isEmpty()) {
            Console.showMessage("Available opportunities:");
            for (ITHOpportunity opp : opportunities) {
                String status = opp.getProbabilityWeight() > 0 ? "available" : "unavailable (weight=0)";
                Console.showMessage("  " + opp.getDisplayName() + " [" + status + "]");
            }
            return CommandResult.BAD_SYNTAX;
        }

        String needle = args.trim().toLowerCase();
        ITHOpportunity match = null;
        for (ITHOpportunity opp : opportunities) {
            if (opp.getDisplayName().toLowerCase().equals(needle)) {
                match = opp;
                break;
            }
        }
        if (match == null) {
            for (ITHOpportunity opp : opportunities) {
                if (opp.getDisplayName().toLowerCase().contains(needle)) {
                    match = opp;
                    break;
                }
            }
        }

        if (match == null) {
            Console.showMessage("No opportunity found matching '" + args + "'. Available:");
            for (ITHOpportunity opp : opportunities) {
                Console.showMessage("  " + opp.getDisplayName());
            }
            return CommandResult.ERROR;
        }

        if (match.getProbabilityWeight() <= 0) {
            Console.showMessage("Opportunity '" + match.getDisplayName() + "' has zero probability weight and cannot be triggered.");
            return CommandResult.ERROR;
        }

        match.trigger();
        Console.showMessage("Triggered opportunity: " + match.getDisplayName());
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> getSuggestions(int parameter, List<String> previous, CommandContext context) {
        if (!context.isInCampaign()) return new ArrayList<>();
        if (parameter != 0) return new ArrayList<>();

        return THRegistry.getOpportunityRegistry().getAll().stream()
                .filter(opp -> opp.getProbabilityWeight() > 0)
                .map(ITHOpportunity::getDisplayName)
                .collect(Collectors.toList());
    }
}
