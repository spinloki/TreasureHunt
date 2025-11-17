package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.config.THSettings;

public class TH_TestItemRewards implements BaseCommand {

    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull CommandContext context) {

        Console.showMessage("=== Treasure Hunt Reward Item Validation ===");

        testGroup("One-time Items", THSettings.getOneTimeItems());
        testGroup("Repeatable Items", THSettings.getRepeatItems());

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
}
