package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.command.WarSimScript;
import com.fs.starfarer.api.util.CountingMap;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dev/testing command: dumps WarSimScript's per-faction strengths for the player's
 * current system, plus a faction's own-vs-enemy strength. Run while raider fleets
 * are in-system to confirm whether WarSim's strength comparison is what defeats a
 * clan raid, and which faction/station supplies the defending weight.
 *
 * Usage: th_warsimstrength [factionId=salvor_clan]
 */
public class TH_WarSimStrength implements BaseCommand {

    private static final String RAIDER = "salvor_clan";

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }
        String faction = (args != null && !args.trim().isEmpty()) ? args.trim() : RAIDER;

        StarSystemAPI system = Global.getSector().getPlayerFleet().getStarSystem();
        if (system == null) {
            Console.showMessage("Error: not in a star system (in hyperspace?).");
            return CommandResult.ERROR;
        }

        Console.showMessage("=== WarSim strengths in " + system.getName() + " ===");
        CountingMap<FactionAPI> strengths = WarSimScript.getFactionStrengths(system);
        List<Map.Entry<FactionAPI, Integer>> entries = new ArrayList<>(strengths.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        if (entries.isEmpty()) {
            Console.showMessage("  (no factions with strength in system)");
        }
        for (Map.Entry<FactionAPI, Integer> e : entries) {
            Console.showMessage(String.format("  %-22s %d", e.getKey().getId(), e.getValue()));
        }

        float own = WarSimScript.getFactionStrength(faction, system);
        float enemy = WarSimScript.getEnemyStrength(faction, system);
        float rel = WarSimScript.getRelativeEnemyStrength(faction, system);
        Console.showMessage(String.format("%s own: %.0f | enemy (defenders): %.0f | relativeEnemy: %.2f",
                faction, own, enemy, rel));
        Console.showMessage(enemy > own
                ? "-> Defenders STRONGER than " + faction + ": WarSim would defeat/repel the raid."
                : "-> " + faction + " stronger than defenders: WarSim would let the raid proceed.");
        return CommandResult.SUCCESS;
    }
}
