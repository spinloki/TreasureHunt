package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.List;
import java.util.UUID;

public class TH_ClaimRelaysInAllSystems implements BaseCommand {

    private static final String RELAY_TYPE = "comm_relay_makeshift";

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        String playerFactionId = playerFaction.getId();

        int systemsChecked = 0;
        int systemsMatched = 0;
        int relaysConverted = 0;
        int relaysCreated = 0;

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            systemsChecked++;

            // Unclaimed only: skip if there's a claiming faction at the system's star/center
            if (system.getCenter() != null) {
                FactionAPI claiming = Misc.getClaimingFaction(system.getCenter());
                if (claiming != null) continue;
            }

            // Must have at least one stable location
            List<SectorEntityToken> stables = system.getEntitiesWithTag(Tags.STABLE_LOCATION);
            if (stables == null || stables.isEmpty()) continue;

            systemsMatched++;

            // Convert any existing comm relays to player control
            List<SectorEntityToken> relays = system.getEntitiesWithTag(Tags.COMM_RELAY);
            if (relays != null && !relays.isEmpty()) {
                for (SectorEntityToken relay : relays) {
                    if (relay.getFaction() != playerFaction) {
                        relay.setFaction(playerFactionId);
                        relaysConverted++;
                    }
                }
                continue;
            }

            // No relay in system: create a makeshift relay at the first stable location
            SectorEntityToken stable = stables.get(0);
            String id = "th_seed_relay_" + system.getId() + "_" + UUID.randomUUID();

            SectorEntityToken newRelay = system.addCustomEntity(id, "Comm Relay", RELAY_TYPE, playerFactionId);
            if (newRelay != null) {
                newRelay.setLocation(stable.getLocation().x, stable.getLocation().y);
                newRelay.setFaction(playerFactionId);
                relaysCreated++;
            }
        }

        Console.showMessage("=== TH_SetupStablePointRelays ===");
        Console.showMessage("Systems checked:  " + systemsChecked);
        Console.showMessage("Systems matched (unclaimed + stable point): " + systemsMatched);
        Console.showMessage("Relays converted to player: " + relaysConverted);
        Console.showMessage("Relays created: " + relaysCreated);

        return CommandResult.SUCCESS;
    }
}