package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.items.THDynamicPackagePlugin;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.List;

public class TH_SpawnPackage implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("This command requires a campaign context.");
            return CommandResult.WRONG_CONTEXT;
        }

        var rewards = THRegistry.getRewardRegistry();
        String key = args.trim();

        if (key.isEmpty()) {
            List<String> all = rewards.getAllBlueprintPackages();
            Console.showMessage("=== Blueprint packages (" + all.size() + ") ===");
            for (String k : all) {
                THBlueprintPackage pkg = rewards.getBlueprintPackage(k);
                String name = pkg.getName() != null ? pkg.getName() : "(no name)";
                String form = pkg.usesTagExpression() ? "tags" : "explicit";
                Console.showMessage("  " + k + "  [" + form + (pkg.isOneTime() ? ", oneTime" : "") + "]  " + name);
            }
            Console.showMessage("Usage: th_spawnpackage <package key>");
            return CommandResult.SUCCESS;
        }

        THBlueprintPackage pkg = rewards.getBlueprintPackage(key);
        if (pkg == null) {
            Console.showMessage("No blueprint package named '" + key + "'. Run with no arguments to list them.");
            return CommandResult.ERROR;
        }

        var cargo = Global.getSector().getPlayerFleet().getCargo();
        cargo.addSpecial(new SpecialItemData(THDynamicPackagePlugin.ITEM_ID, key), 1);

        Console.showMessage("Added dynamic package '" + key + "' ("
                + pkg.getShips().size() + " ships, "
                + pkg.getWeapons().size() + " weapons, "
                + pkg.getFighters().size() + " fighters listed; tags="
                + pkg.getEffectiveTags() + ")");
        return CommandResult.SUCCESS;
    }
}
