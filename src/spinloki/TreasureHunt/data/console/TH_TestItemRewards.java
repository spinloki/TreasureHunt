package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THRewardItem;

public class TH_TestItemRewards implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {

        Console.showMessage("=== Treasure Hunt Reward Item Validation ===");

        testGroup("One-time Items", THRegistry.getRewardRegistry().getOneTimeItems());
        testGroup("Repeatable Items", THRegistry.getRewardRegistry().getRepeatItems());
        testPackages();

        TreasureHuntEventIntel intel = TreasureHuntEventIntel.get();
        if (intel == null) {
            Console.showMessage("\n(no active hunt intel, live pools unavailable)");
        } else {
            testPool("Live one-time pool", intel.getOneTimePool());
            testPool("Live repeatable pool", intel.getRepeatablePool());
        }

        Console.showMessage("=== Done ===");
        return CommandResult.SUCCESS;
    }

    private void testGroup(String title, java.util.List<String> items) {
        Console.showMessage("\n--- " + title + " (" + items.size() + ") ---");

        for (String id : items) {
            SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(id);

            if (spec == null) {
                Console.showMessage("!! MISSING special item spec: " + id);
                continue;
            }

            String name = spec.getName();
            Console.showMessage(id + " -> " + name);
        }
    }

    private void testPackages() {
        var rewards = THRegistry.getRewardRegistry();
        var keys = rewards.getAllBlueprintPackages();
        Console.showMessage("\n--- Blueprint Packages (" + keys.size() + ") ---");

        for (String key : keys) {
            THBlueprintPackage pkg = rewards.getBlueprintPackage(key);
            int contents = pkg.getShips().size() + pkg.getWeapons().size() + pkg.getFighters().size();
            String form = pkg.usesTagExpression() ? "tags=" + pkg.getEffectiveTags() : contents + " listed";
            String pooled = pkg.isOneTime() ? "one-time pool" : "not pooled";
            if (!pkg.usesTagExpression() && contents == 0) {
                Console.showMessage("!! EMPTY package: " + key);
                continue;
            }
            Console.showMessage(key + " -> " + form + ", " + pooled);
        }
    }

    private void testPool(String title, java.util.Set<String> pool) {
        Console.showMessage("\n--- " + title + " (" + pool.size() + ") ---");
        for (String token : pool) {
            THRewardItem reward = THRewardItem.parse(token);
            if (!reward.isValid()) {
                Console.showMessage("!! UNRESOLVABLE pool entry: " + token);
                continue;
            }
            Console.showMessage(token + " -> " + reward.getDisplayName());
        }
    }
}
