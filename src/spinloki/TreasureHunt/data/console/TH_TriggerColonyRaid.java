package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;


/**
 * Dev/testing command: fires a Salvor Clan colony raid at a player colony via the
 * GenericRaidFGI framework. Pair with th_maketestcolony to iterate on raid scaling.
 *
 * Usage: th_triggercolonyraid [strengthMult=1.0]
 *   Targets the player colony in the current system (else nearest player colony).
 *   strengthMult scales the raid's fleet points so you can tune it against the
 *   colony's WarSim defensive strength without rebuilds (try 2, 3, 4...).
 */
public class TH_TriggerColonyRaid implements BaseCommand {

    private static final String RAIDER_FACTION = "salvor_clan";

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        float mult = 1f;
        if (args != null && !args.trim().isEmpty()) {
            try { mult = Float.parseFloat(args.trim()); } catch (NumberFormatException ignored) {}
        }

        MarketAPI target = pickTargetColony();
        if (target == null) {
            Console.showMessage("Error: no player colony found to raid.");
            return CommandResult.ERROR;
        }
        MarketAPI source = pickSourceMarket(target);
        if (source == null) {
            Console.showMessage("Error: no non-player market to source the raid from.");
            return CommandResult.ERROR;
        }
        StarSystemAPI system = target.getStarSystem();

        GenericRaidFGI.GenericRaidParams p = THClanRaidFGI.buildParams(target, source, mult);

        THClanRaidFGI raid = new THClanRaidFGI(p);
        Global.getSector().getIntelManager().addIntel(raid);

        Console.showMessage(String.format(
                "Clan raid launched at %s (size %d) in %s, sourced from %s. Mult %.1f, fleets: %s",
                target.getName(), target.getSize(), system.getName(), source.getName(), mult, p.fleetSizes));
        return CommandResult.SUCCESS;
    }

    private MarketAPI pickTargetColony() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        StarSystemAPI playerSystem = player.getStarSystem();
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!m.isPlayerOwned() || m.getPrimaryEntity() == null) continue;
            if (playerSystem != null && m.getStarSystem() == playerSystem) return m;
            float d = Misc.getDistanceLY(player.getLocationInHyperspace(), m.getLocationInHyperspace());
            if (d < best) { best = d; nearest = m; }
        }
        return nearest;
    }

    private MarketAPI pickSourceMarket(MarketAPI target) {
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.isPlayerOwned() || m.isHidden() || m.getPrimaryEntity() == null) continue;
            float d = Misc.getDistanceLY(target.getLocationInHyperspace(), m.getLocationInHyperspace());
            if (d < best) { best = d; nearest = m; }
        }
        return nearest;
    }
}
