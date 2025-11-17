package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.TreasureHunt;
import spinloki.TreasureHunt.testing.*;

import java.util.ArrayList;
import java.util.List;

import static com.fs.starfarer.api.util.Misc.LAST_RAIDED_AT;

public class TH_TestFactorTracker implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String s, @NotNull CommandContext context) {
        testRaidingMarkets();
        testExploringRuins();
        testDestroyingScavengers();
        testCustomEntityRewards();
        showResults();
        return CommandResult.SUCCESS;
    }

    private static void testRaidingMarkets(){
        var tracker = TreasureHunt.getFactorTrackerForTestOnly();
        var market = new THMockMarket("");
        var currentTime = Global.getSector().getClock().getTimestamp();
        market.getMemoryWithoutUpdate().set(LAST_RAIDED_AT, currentTime);
        var entity = new THMockSectorEntityToken(market);
        var dialog = new THMockInteractionDialogAPI(entity);
        tracker.reportAboutToShowLootToPlayer(null, dialog);

        market.getMemoryWithoutUpdate().set(LAST_RAIDED_AT, currentTime - 90000000); // a lot of ticks that equals about one day
        entity = new THMockSectorEntityToken(market);
        dialog = new THMockInteractionDialogAPI(entity);
        tracker.reportAboutToShowLootToPlayer(null, dialog);
    }

    private static void testExploringRuins(){
        String[] conditions = {
                "ruins_scattered",
                "ruins_widespread",
                "ruins_extensive",
                "ruins_vast"
        };

        var tracker = TreasureHunt.getFactorTrackerForTestOnly();

        for (String condition : conditions) {
            var market = new THMockMarket(condition);
            var entity = new THMockSectorEntityToken(market);
            InteractionDialogAPI dialog = new THMockInteractionDialogAPI(entity);
            tracker.reportAboutToShowLootToPlayer(null, dialog);
        }
    }

    private static void testDestroyingScavengers() {
        FleetMemberAPI lost = new THMockFleetMember(100000f);
        FleetMemberAPI alive = new THMockFleetMember(50000f);
        ArrayList<FleetMemberAPI> snapshot = new ArrayList<>();
        snapshot.add(lost);
        snapshot.add(alive);
        List<FleetMemberAPI> current  = List.of(alive);
        THMockFleetData data = new THMockFleetData(current, snapshot);
        THMockFleet scavFleet = new THMockFleet(
                new THMockBattle(List.of()),
                true,
                data
        );
        THMockFleet scavFleet2 = new THMockFleet(
                new THMockBattle(List.of()),
                true,
                data
        );
        THMockFleet nonScavFleet = new THMockFleet(
                new THMockBattle(List.of()),
                false,
                data
        );
        THMockBattle battle = new THMockBattle(List.of(scavFleet, scavFleet2, nonScavFleet));
        scavFleet.setBattle(battle);
        InteractionDialogAPI dialog = new THMockInteractionDialogAPI(scavFleet);
        var tracker = TreasureHunt.getFactorTrackerForTestOnly();
        tracker.reportAboutToShowLootToPlayer(null, dialog);
    }

    private static void testCustomEntityRewards() {
        String[] entityIds = {
                "station_research_remnant",
                "station_mining_remnant",
                "orbital_habitat_remnant",
                "makeshift_station",
                "derelict_probe",
                "derelict_survey_ship",
                "derelict_mothership",
                "supply_cache",
                "supply_cache_small",
                "equipment_cache",
                "equipment_cache_small",
                "weapons_cache",
                "weapons_cache_low",
                "weapons_cache_high",
                "weapons_cache_remnant",
                "weapons_cache_small",
                "weapons_cache_small_low",
                "weapons_cache_small_high",
                "weapons_cache_small_remnant"
        };

        var tracker = TreasureHunt.getFactorTrackerForTestOnly();

        for (String id : entityIds) {
            var entity = new THMockSectorEntityToken(id);
            InteractionDialogAPI dialog = new THMockInteractionDialogAPI(entity);
            tracker.reportAboutToShowLootToPlayer(null, dialog);
        }
    }

    private static void showResults() {
        var mFactors = TreasureHunt.getFactorTrackerForTestOnly().getmFactors();
        for (var factor : mFactors){
            var value = factor.getProgress(null);
            var description = factor.getDesc(null);
            Console.showMessage(value + " points " + description);
        }
        TreasureHunt.getFactorTrackerForTestOnly().advance(0);
    }
}
