package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.factors.THClanRivalryFactor;
import spinloki.TreasureHunt.internal.intel.THClanRivalryIntel;
import spinloki.TreasureHunt.util.THUtils;

/**
 * Usage: th_clanrivalry [status|on|clear]
 *   status - report every gate the rivalry feature checks
 *   on     - activate the rivalry as though the opportunity had fired
 *   clear  - forget which colonies have driven the clans off
 */
public class TH_ClanRivalry implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        String mode = args == null || args.trim().isEmpty() ? "status" : args.trim().toLowerCase();
        switch (mode) {
            case "on":
                THUtils.setClanRivalryActive();
                THFactorTracker.syncClanRivalryFactors();
                Console.showMessage("Clanner Rivalry activated.");
                break;
            case "clear":
                THUtils.getClanClearedColonies().clear();
                THFactorTracker.syncClanRivalryFactors();
                Console.showMessage("Cleared-colony list emptied.");
                break;
            case "status":
                break;
            default:
                Console.showMessage("Error: expected status, on or clear.");
                return CommandResult.BAD_SYNTAX;
        }

        printStatus();
        return CommandResult.SUCCESS;
    }

    private void printStatus() {
        Console.showMessage("rivalry active: " + THUtils.isClanRivalryActive());
        Console.showMessage("gate met: " + THUtils.isClanRivalryGateMet());
        Console.showMessage("intel present: " + (THClanRivalryIntel.get() != null));
        Console.showMessage("cleared colonies: " + THUtils.getClanClearedColonies());

        Console.showMessage("qualifying colonies:");
        boolean any = false;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!THUtils.isRivalryRuinsColony(market)) continue;
            any = true;
            Console.showMessage("  " + market.getName() + " (" + Misc.getRuinsType(market) + ")");
        }
        if (!any) Console.showMessage("  none");

        HostileActivityEventIntel intel = HostileActivityEventIntel.get();
        if (intel == null) {
            Console.showMessage("hostile activity intel: absent (no player colonies yet?)");
            return;
        }
        Console.showMessage("hostile activity progress: " + intel.getProgress()
                + "/" + HostileActivityEventIntel.MAX_PROGRESS);
        THClanRivalryFactor factor = null;
        for (EventFactor curr : intel.getFactors()) {
            if (curr instanceof THClanRivalryFactor found) factor = found;
        }
        if (factor == null) {
            Console.showMessage("rivalry factor: not installed");
            return;
        }
        Console.showMessage("rivalry factor: +" + factor.getProgress(intel) + "/month, causes:");
        if (factor.getCauses().isEmpty()) Console.showMessage("  none");
        for (HostileActivityCause2 cause : factor.getCauses()) {
            Console.showMessage("  " + cause.getDesc() + " " + cause.getProgressStr());
        }
    }
}
