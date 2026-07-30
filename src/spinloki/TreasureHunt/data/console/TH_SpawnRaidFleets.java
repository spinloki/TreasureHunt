package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;


/**
 * Usage: th_spawnraidfleets [strengthMult=1.0]
 * Spawns the raid's fleets at the player, sized off the nearest player colony. The intel is
 * left unregistered, so no raid runs.
 */
public class TH_SpawnRaidFleets implements BaseCommand {

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
            Console.showMessage("Error: no player colony to size the raid against. "
                    + "Run th_maketestcolony first.");
            return CommandResult.ERROR;
        }
        MarketAPI source = pickSourceMarket(target);
        if (source == null) {
            Console.showMessage("Error: no non-player market to source the fleets from.");
            return CommandResult.ERROR;
        }

        GenericRaidFGI.GenericRaidParams p = THClanRaidFGI.buildParams(target, source, mult);

        THClanRaidFGI raid = new THClanRaidFGI(p);

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI where = player.getContainingLocation();
        if (where == null) {
            Console.showMessage("Error: player fleet has no containing location.");
            return CommandResult.ERROR;
        }

        int spawned = 0;
        float angle = 0f;
        for (int size : p.fleetSizes) {
            CampaignFleetAPI fleet = raid.createFleetForTesting(size);
            if (fleet == null) {
                Console.showMessage("Warning: fleet of size " + size + " failed to spawn.");
                continue;
            }
            where.addEntity(fleet);
            float dist = 400f + spawned * 120f;
            fleet.setLocation(
                    player.getLocation().x + dist * (float) Math.cos(Math.toRadians(angle)),
                    player.getLocation().y + dist * (float) Math.sin(Math.toRadians(angle)));
            angle += 360f / Math.max(1, p.fleetSizes.size());
            spawned++;

            Console.showMessage(String.format(
                    "  %-20s role=%-8s fp=%-5d ships=%-3d fuel=%-5.0f marines=%-5.0f supplies=%.0f",
                    fleet.getName(),
                    fleet.getMemoryWithoutUpdate().getString(THClanRaidFGI.MEM_ROLE),
                    fleet.getFleetPoints(),
                    fleet.getFleetData().getMembersListCopy().size(),
                    fleet.getCargo().getFuel(),
                    fleet.getCargo().getCommodityQuantity(Commodities.MARINES),
                    fleet.getCargo().getCommodityQuantity(Commodities.SUPPLIES)));
        }

        Console.showMessage(String.format(
                "Spawned %d/%d clan raid fleets at your position (sizes %s, mult %.1f). "
                        + "No raid is running — these are standalone for testing.",
                spawned, p.fleetSizes.size(), p.fleetSizes, mult));
        return CommandResult.SUCCESS;
    }

    private MarketAPI pickTargetColony() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI playerLoc = player.getContainingLocation();
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!m.isPlayerOwned() || m.getPrimaryEntity() == null) continue;
            if (playerLoc != null && m.getContainingLocation() == playerLoc) return m;
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
