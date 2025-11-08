package spinloki.TreasureHunt.data.console;

import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.campaign.intel.events.THTimeFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;

import static org.lazywizard.console.CommandUtils.isInteger;

public class TH_AddProgress implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign())
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (!isInteger(args)){
            return CommandResult.BAD_SYNTAX;
        }

        var amount = Integer.parseInt(args);
        if (amount < 0){
            return CommandResult.BAD_SYNTAX;
        }

        TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(amount), null);
        Console.showMessage("Created factor to add progress");
        return CommandResult.SUCCESS;
    }
}
