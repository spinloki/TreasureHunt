package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableAggroAssignmentAI;
import com.fs.starfarer.api.impl.campaign.intel.events.DisposableHostileActivityFleetManager;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.factors.THClanRivalryFactor;
import spinloki.TreasureHunt.internal.fleets.THClanPiracyScript;

import java.util.Random;

/**
 * Usage: th_spawnprowler [count=1] [near]
 *   Spawns ambient Clanner Rivalry fleets through the same factor the colony crisis uses.
 *   near - place them next to your fleet instead of away from it
 */
public class TH_SpawnProwler implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        int count = 1;
        boolean near = false;
        if (args != null) {
            for (String token : args.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                if ("near".equalsIgnoreCase(token)) near = true;
                else {
                    try { count = Integer.parseInt(token); } catch (NumberFormatException e) {
                        Console.showMessage("Error: unrecognized argument '" + token + "'.");
                        return CommandResult.BAD_SYNTAX;
                    }
                }
            }
        }
        if (count < 1) count = 1;

        StarSystemAPI system = pickSystem();
        if (system == null) {
            Console.showMessage("Error: no player colony system found to prowl.");
            return CommandResult.ERROR;
        }

        THClanRivalryFactor factor = findFactor();
        if (factor == null) {
            Console.showMessage("Error: no Clanner Rivalry factor installed. Run th_clanrivalry on first.");
            return CommandResult.ERROR;
        }

        float magnitude = factor.getEffectMagnitude(system);
        if (magnitude <= 0f) {
            Console.showMessage("Warning: " + system.getName() + " holds no colony the clans care about, "
                    + "so these fleets spawn at minimum strength.");
        }

        DisposableHostileActivityFleetManager manager = new DisposableHostileActivityFleetManager();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        Random random = new Random();
        int spawned = 0;

        for (int i = 0; i < count; i++) {
            CampaignFleetAPI fleet = factor.createFleet(system, random);
            if (fleet == null || fleet.isEmpty()) {
                Console.showMessage("Warning: fleet " + (i + 1) + " failed to generate.");
                continue;
            }

            fleet.getMemoryWithoutUpdate().set("$dhafm_ID", factor.getId());
            system.addEntity(fleet);
            new DisposableAggroAssignmentAI(fleet, system, manager, 0f);

            if (near) {
                fleet.setLocation(player.getLocation().x + 400f + i * 150f, player.getLocation().y);
            }
            spawned++;

            Console.showMessage(String.format("  %-28s fp=%-4d ships=%-3d piracy=%s",
                    fleet.getName(),
                    fleet.getFleetPoints(),
                    fleet.getFleetData().getMembersListCopy().size(),
                    hasPiracyScript(fleet) ? "yes" : "no"));
        }

        Console.showMessage(String.format(
                "Spawned %d/%d clanner prowlers in %s (magnitude %.2f, cap %d per system).",
                spawned, count, system.getName(), magnitude, factor.getMaxNumFleets(system)));
        if (!near) {
            Console.showMessage("They spawn away from your fleet by design; add 'near' to place them beside you.");
        }
        return CommandResult.SUCCESS;
    }

    private boolean hasPiracyScript(CampaignFleetAPI fleet) {
        for (EveryFrameScript script : fleet.getScripts()) {
            if (script instanceof THClanPiracyScript) return true;
        }
        return false;
    }

    private StarSystemAPI pickSystem() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        StarSystemAPI current = player.getStarSystem();
        if (current != null) return current;

        StarSystemAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (StarSystemAPI system : Misc.getSystemsWithPlayerColonies(false)) {
            float dist = Misc.getDistanceLY(player.getLocationInHyperspace(), system.getLocation());
            if (dist < best) { best = dist; nearest = system; }
        }
        return nearest;
    }

    private THClanRivalryFactor findFactor() {
        THFactorTracker.syncClanRivalryFactors();
        HostileActivityEventIntel intel = HostileActivityEventIntel.get();
        if (intel == null) return null;
        for (EventFactor factor : intel.getFactors()) {
            if (factor instanceof THClanRivalryFactor found) return found;
        }
        return null;
    }
}
