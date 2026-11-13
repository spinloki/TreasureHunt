# Headless test suite

Runs TreasureHunt inside the real campaign engine through `headless-sim/` (no window), and checks it
against real sectors, specs and save files rather than mocks.

```sh
test/run.sh              # build the mod, then run both phases on seed 1 (about a minute)
test/run.sh --seed 4     # another sector; tests depend on what each seed generates
test/run.sh --no-build   # reuse jars/TreasureHunt.jar
```

Exit code 0 means every test passed. Results go to `test/out/results.txt`, and each phase's full game log to
`test/out/new-game.log` and `test/out/load.log`. Mods come from `headless-sim/profiles/treasurehunt`
(LazyLib and this mod).

## Phases

1. `NewGameSuite` generates a new sector (with the 120-day pre-start time pass) and runs its tests in order
   against it. Later tests build on earlier ones (the colony, then the rivalry, then the raid). It then
   records what it expects in the sector's persistent data, and the harness saves the sector to
   `headless-sim/fixtures/th_test_seed<N>`.
2. `LoadSuite` loads that save through the game's own loader, so `onGameLoad` and XStream run as they do
   for a player, then checks the recorded state and simulates 35 more days.

## What is real and what is scripted

- Real: the engine, specs, procgen sector, economy, fleets, routes, intel, save/load, listener dispatch
  (`ListenerUtil`) and the mod's own code.
- Scripted: interaction dialogs. `Dialogs` replaces the harness's stub campaign UI so a plugin passed to
  `showInteractionDialog` gets `init` with a recording dialog, and a test picks options with
  `Dialogs.current().select(data)`. Loot events are dispatched with a dialog whose target is a real entity.
- The player fleet is hidden from AI fleets, as in the harness's own runs.
- Fleets on routes (raids, swarms) only become real fleets near the player, as in game, so those tests move
  the player next to the action first.

## Writing tests

Add a `test("name", () -> { ... })` to a suite. `World` advances time (`frames`, `days`, `until`), fires
loot events, colonizes planets and reads private fields. `Suite` has `check`, `eq` and `notNull`. A test that
throws fails without stopping the others.

Runs aren't exactly reproducible (runtime randomness is unseeded), so wait on conditions with budgets derived
from the game state (route segment lengths, for example) rather than fixed day counts.
