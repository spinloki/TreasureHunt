# Treasure Hunt -- Mod Integration Guide

This guide walks you through integrating your mod with Treasure Hunt, from simple data-driven additions to fully custom behaviors.

> **Dependency setup:** Add `spinloki_treasurehunt` to your `mod_info.json` dependencies and include `jars/TreasureHunt.jar` in your compilation classpath.

---

## Table of Contents

1. [Adding Reward Sources (JSON only)](#1-adding-reward-sources)
2. [Adding Items to the Reward Pool (JSON only)](#2-adding-items-to-the-reward-pool)
3. [Registering a Scavenger Swarm Faction (JSON only)](#3-registering-a-scavenger-swarm-faction)
4. [Registering a Faction with Custom Behavior (Java)](#4-registering-a-faction-with-custom-behavior)
5. [Creating a Custom Opportunity (Java)](#5-creating-a-custom-opportunity)
6. [Adding Custom Progress Factors (Java)](#6-adding-custom-progress-factors)
7. [Custom Hassle Dialog (rules.csv)](#7-custom-hassle-dialog)
8. [API Reference](#8-api-reference)

---

## How Integration Works

Treasure Hunt uses Starsector's **settings.json auto-merge** for data-driven integration. When your mod has a `data/config/settings.json`, Starsector merges its keys with every other mod's settings. This means you can add reward sources, item pools, and faction registrations without writing any Java code.

For behaviors that require code - custom fleet AI, custom opportunity types - use the `THApi` Java facade.

---

## 1. Adding Reward Sources

Reward sources are the salvageable entities (stations, caches, debris fields, etc.) that give the player progress toward the next treasure hunt stage. You can add your mod's custom entity types to the reward table.

Add entries to `th_rewards` in your own mod's `data/config/settings.json`:

```json
{
    "th_rewards": {
        "my_mod_ancient_vault": {
            "value": 50,
            "description": "exploring an ancient vault"
        },
        "my_mod_data_cache": {
            "value": 8,
            "description": "recovering encrypted data"
        }
    }
}
```

Each entry maps a **custom entity type ID** (as used in `SectorEntityToken.getCustomEntityType()`) to:
- `value` - progress points awarded when the player salvages it
- `description` - text shown in the progress factor tooltip (e.g. "gained 50 points from *exploring an ancient vault*")

### Aliases

If several of your entity types should give the same reward, you can use aliases to avoid repetition:

```json
{
    "th_rewards": {
        "my_mod_vault": {
            "value": 50,
            "description": "exploring an ancient vault"
        },
        "my_mod_vault_small": "my_mod_vault",
        "my_mod_vault_damaged": "my_mod_vault"
    }
}
```

Aliases resolve recursively. You can alias to another alias, and it will follow the chain until it finds a `{ "value", "description" }` object.

---

## 2. Adding Items to the Reward Pool

When the player completes a treasure hunt, they receive special items picked from two pools:

- **Repeatable items** - added to a pool that refreshes when all items are found, since players might want several of them (colony items)
- **One-time items** - added to a pool that never refreshes, since they are only useful for the player to find once. (e.g. blueprint packages)

Add your items' special item IDs to the appropriate array in your own mod's `data/config/settings.json`:

```json
{
    "th_one_time_items": [
        "my_mod_rare_blueprint_package"
    ],
    "th_repeat_items": [
        "my_mod_powerful_artifact",
        "my_mod_colony_upgrade"
    ]
}
```

The arrays merge across mods - your entries are added alongside the vanilla Treasure Hunt items. The IDs must match special item specs registered in your mod's `data/campaign/special_items.csv`.

### Blueprint Packages

If your mod adds a blueprint package special item, you also need to register its contents so the tagging system can mark the ships/weapons/fighters appropriately:

```json
{
    "th_blueprints_packages": {
        "my_mod_weapons_package": {
            "fighters": [],
            "ships": [],
            "weapons": ["my_weapon_1", "my_weapon_2", "my_weapon_3"]
        },
        "my_mod_ships_package": {
            "fighters": ["my_fighter_wing"],
            "ships": ["my_cruiser", "my_capital"],
            "weapons": []
        }
    }
}
```

The package name should match the convention `<package_id>_package` for the special item ID in `special_items.csv`.

---

## 3. Registering a Scavenger Swarm Faction

The Scavenger Swarm opportunity spawns fleets from multiple factions that converge on a system. You can add your mod's factions to the swarm using JSON alone.

Add an entry under `th_factions` in `data/config/settings.json`:

```json
{
    "th_factions": {
        "my_faction": {
            "template": "SCAVENGER"
        }
    }
}
```

The key is your **faction ID** (as used in `Global.getSector().getFaction("my_faction")`).

### Templates

The `template` field selects a behavior archetype:

| Template | Fleet Type | Behavior |
|----------|-----------|----------|
| `SCAVENGER` | Scavenger fleet | Explores only. No hassling. |
| `ENFORCER` | Inspection fleet | Explores and hassles the player with inspections. |

Fleet type, composition, and other parameters can be overridden per-faction (see below).

### Optional Overrides

You can override individual fleet parameters without writing Java:

```json
{
    "th_factions": {
        "my_faction": {
            "template": "ENFORCER",
            "narrativeText": "investigating reports of contraband",
            "fleetType": "taskForce",
            "fleetName": "Customs Patrol",
            "freighterPts": 15.0,
            "tankerPts": 8.0
        }
    }
}
```

| Field | Default | Description |
|-------|---------|-------------|
| `template` | *(required)* | Behavior archetype (see table above) |
| `narrativeText` | `"on a salvage expedition"` | Text shown while fleet is in transit |
| `fleetType` | From template | Fleet doctrine type (e.g. `"taskForce"`, `"patrolLarge"`) |
| `fleetName` | From template | Display name for spawned fleets |
| `freighterPts` | From template | Freighter fleet points in `FleetParamsV3` |
| `tankerPts` | From template | Tanker fleet points in `FleetParamsV3` |

Any field you omit falls back to the template's default.

---

## 4. Registering a Faction with Custom Behavior

When JSON templates aren't enough - you need custom fleet composition logic, custom AI, or dynamic behavior - use the Java API.

### Setup

Register your faction in your mod plugin's `onGameLoad()`:

```java
import spinloki.TreasureHunt.api.THApi;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.api.THFactionTemplate;

public class MyModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        THApi.registerFaction("my_faction",
            THFactionConfig.builder()
                .template(THFactionTemplate.ENFORCER)
                .narrativeText("hunting for stolen technology")
                .fleetName("Recovery Taskforce")
                .build()
        );
    }
}
```

### Custom Fleet Creator

Override how fleets are built. Note that you're not limited to just using `FleetFactoryV3` - you just need to return a `CampaignFleetAPI`.

If you don't set a fleet creator, the default behavior uses `FleetFactoryV3.createFleet()` with the template's fleet type, freighter points, tanker points, and fleet name. This is usually sufficient for factions that just need standard doctrine fleets.

```java
THApi.registerFaction("my_faction",
    THFactionConfig.builder()
        .template(THFactionTemplate.PRIVATEER)
        .fleetCreator((system, route, sourceMarket, random) -> {
            // Build your fleet however you want
            FleetParamsV3 params = new FleetParamsV3(
                sourceMarket,
                system.getLocation(),
                "my_faction",
                null,
                FleetTypes.MERC_PRIVATEER,
                80f,  // combat FP
                10f,  // freighter pts
                5f,   // tanker pts
                0f, 0f, 0f, 0f
            );
            CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
            if (fleet != null) {
                fleet.setName("Shadow Detachment");
                // Add special ships, modify loadout, etc.
            }
            return fleet;
        })
        .build()
);
```

### Custom Fleet AI

Override the fleet's in-system behavior.

If you don't set an AI creator, the default uses `THBaseScavengerSwarmFleetAssignmentAI`, which picks salvageable entities in-system to investigate and patrols between them. If you set `narrativeText`, the default AI uses that text for the fleet's transit tooltip automatically.

```java
THApi.registerFaction("my_faction",
    THFactionConfig.builder()
        .template(THFactionTemplate.SCAVENGER)
        .aiCreator((fleet, route) -> {
            // Return any RouteFleetAssignmentAI subclass
            return new MyCustomSwarmAI(fleet, route);
        })
        .build()
);
```

Your AI class should extend `RouteFleetAssignmentAI` (or `THBaseScavengerSwarmFleetAssignmentAI` if you want the default behaviors as a starting point). The key methods to override:

- `getTravelActionText(segment)` - text shown in the fleet tooltip while traveling
- `getInSystemActionText(segment)` - text shown while exploring
- `addLocalAssignment(segment, justSpawned)` - what the fleet does when it arrives in-system

### Simple Template Registration

If you just want a faction in the swarm with default behavior, you can skip the builder entirely:

```java
THApi.registerFaction("my_faction", THFactionTemplate.SCAVENGER);
```

---

## 5. Creating a Custom Opportunity

Opportunities are the mid-hunt events that fire when the player accumulates enough progress (at the 300-point mark). Creating one requires Java.

### Step 1: Extend BaseTHOpportunity

The `BaseTHOpportunity` class handles probability decay and trigger count persistence automatically. You just need to override `trigger()`, `getDisplayName()`, and `getIcon()`.

```java
import spinloki.TreasureHunt.api.BaseTHOpportunity;

public class MyOpportunity extends BaseTHOpportunity {

    @Override
    public void trigger() {
        super.trigger(); // Required - tracks trigger count and persists it across saves
        // Create your intel, spawn fleets, set up the event, etc.
        // Use getIconPath() to resolve the sprite ID to a full path for intel constructors
        new MyOpportunityIntel(pickTargetSystem(), getIconPath());
    }

    @Override
    public String getDisplayName() {
        return "My Opportunity";
    }

    @Override
    public String getIcon() {
        return "my_opportunity";
    }

    private StarSystemAPI pickTargetSystem() {
        // Your system selection logic
    }
}
```

`BaseTHOpportunity` gives you:
- **Automatic probability decay**: uses `1 / (1 + n)^2` where `n` is the number of times triggered, so no single opportunity dominates the pool
- **Persistence across save/load**: trigger counts are stored in sector persistent data, keyed by your class name - you don't need to manage this yourself

The `super.trigger()` call is important - it increments the count and persists it. If you skip it, your opportunity's weight won't decay and its count won't survive save/load.

> **Advanced:** If you need full control over weighting (e.g. opportunities that depend on game state), you can implement `ITHOpportunity` directly and manage your own persistence. But for most cases, extending `BaseTHOpportunity` is the recommended approach.

### Step 2: Register in onGameLoad

```java
import spinloki.TreasureHunt.api.THApi;

public class MyModPlugin extends BaseModPlugin {
    @Override
    public void onGameLoad(boolean newGame) {
        THApi.registerOpportunity(new MyOpportunity());
    }
}
```

Registration happens on every game load - opportunity objects are recreated fresh each time. `BaseTHOpportunity` automatically restores trigger counts from persistent data in its constructor, so the probability decay carries over across saves.

### Icon Setup

Opportunity icons are displayed in the Treasure Hunt event intel panel and in dialog text when the opportunity is found. Register your icon sprite under the `treasure_hunt_events` category in your mod's `data/config/settings.json`, using the same key returned by your `getIcon()` method:

```json
{
    "graphics": {
        "treasure_hunt_events": {
            "my_opportunity": "graphics/icons/my_opportunity.png"
        }
    }
}
```

The key must match what `getIcon()` returns (e.g. `"my_opportunity"`). If no matching sprite is found, a generic fallback icon is used.

---

## 6. Adding Custom Progress Factors

If your mod introduces new player activities that should contribute progress toward the treasure hunt, you can create custom factors and add them via the API.

### One-Time Factors

For discrete events (player did a thing, gets progress once):

```java
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import spinloki.TreasureHunt.api.THApi;

public class MyCustomFactor extends BaseOneTimeFactor {
    public MyCustomFactor(int points) {
        super(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Recovered data from a mysterious artifact";
    }
}
```

Then, wherever your mod detects the relevant player action:

```java
THApi.addProgress(new MyCustomFactor(25), dialog); // dialog can be null if not in a dialog
```

### Recurring Factors

For ongoing sources (contributes progress every month while active):

```java
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import spinloki.TreasureHunt.api.THApi;

public class MyRecurringFactor extends BaseEventFactor {
    private final String sourceName;
    private final int monthlyProgress;

    public MyRecurringFactor(String sourceName, int monthlyProgress) {
        super();
        this.sourceName = sourceName;
        this.monthlyProgress = monthlyProgress;
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Signal intercepts from " + sourceName;
    }

    @Override
    public int getProgress(BaseEventIntel intel) {
        return monthlyProgress;
    }

    @Override
    public boolean isExpired() {
        return false; // return true when this source should stop contributing
    }
}
```

Add it once when the source is established:

```java
THApi.addProgress(new MyRecurringFactor("Abandoned Relay", 10), null);
```

### Notes

- `BaseOneTimeFactor` applies its points immediately and then expires. `BaseEventFactor` contributes its `getProgress()` value every month until `isExpired()` returns true.
- Override `getMainRowTooltip()` to show a custom tooltip when the player hovers over your factor in the Treasure Hunt intel screen.
- The treasure hunt event is created automatically if it doesn't exist yet when you call `addProgress()`.

---

## 7. Custom Hassle Dialog

When a faction is registered with the `ENFORCER` template, its scavenger swarm fleets will hassle the player with a "restricted system inspection" dialog. By default, factions that don't have faction-specific dialog entries in `rules.csv` will use the generic fallback text.

To add custom dialog text for your faction, add entries to your own mod's `data/campaign/rules.csv`. This is safe even if TreasureHunt is not installed, because the rules gate on memory flags (`$entity.thRestrictConv`, `$entity.thRestrictFaction`) that are only set by TreasureHunt's fleet manager, so they can never fire without TreasureHunt present.

### Dialog Structure

The hassle dialog has three phases:

1. **Opening hail** (`OpenCommLink`) — The fleet commander contacts the player and demands inspection
2. **Player options** (`THRestrictedOptions`) — Comply, refuse, or cut comms (handled by built-in rules)
3. **Refuse response** (`DialogOptionSelected`) — Faction-specific reaction when the player refuses

You only need to provide text for phases 1 and 3. Phase 2 (the option buttons) is handled automatically.

### Opening Hail Rules

Each faction needs two opening rules — one for when the fleet is **stronger** than the player, one for when it's **weaker**. The distinction affects both dialog tone and gameplay: strong fleets force disengage prevention, weak fleets don't.

Use a `score` higher than the fallback (`50`/`40`) but lower than the built-in factions (`300`/`250`). A score of `200`/`150` works well.

```csv
thRestrictComms_MyFaction,OpenCommLink,"$entity.thRestrictConv score:200
$entity.relativeStrength >= 0
$entity.thRestrictFaction == my_faction","$entity.ignorePlayerCommRequests = true 7
FireAll THRestrictedOptions","Your opening hail text when the fleet is STRONGER than the player.",,
thRestrictComms_MyFactionWk,OpenCommLink,"$entity.thRestrictConv score:150
$entity.relativeStrength < 0
$entity.thRestrictFaction == my_faction","$entity.ignorePlayerCommRequests = true 7
MakeOtherFleetPreventDisengage hassle false
FireAll THRestrictedOptions","Your opening hail text when the fleet is WEAKER than the player.",,
```

Key points for opening rules:
- The **strong** variant omits `MakeOtherFleetPreventDisengage` (the initiator already set it to true)
- The **weak** variant includes `MakeOtherFleetPreventDisengage hassle false` to let the player leave
- Both must include `$entity.ignorePlayerCommRequests = true 7` and `FireAll THRestrictedOptions`
- The text column supports standard Starsector variables: `$post`, `$heOrShe`, `$hisOrHer`, `$himOrHer`, `$manOrWoman`, `$HeOrShe`, `$HisOrHer`

### Refuse Response Rules

Each faction needs two refuse rules — strong (escalates to hostility) and weak (backs down).

```csv
thRestrictRefuseSel_MyFaction_Strong,DialogOptionSelected,"$option == thRestrict_Refuse
$entity.thRestrictFaction == my_faction
$entity.relativeStrength >= 0","RemoveOption thRestrict_Refuse
$thRestrictFailedToRefuse = true 0
$thRestrictMadeHostile = true 0
MakeOtherFleetHostile thRestrictedInspection true 3
MakeOtherFleetAggressive thRestrictedInspection true 3
FireAll THRestrictedOptions","Your refusal text when the fleet is STRONGER (they escalate).",,
thRestrictRefuseSel_MyFaction_Weak,DialogOptionSelected,"$option == thRestrict_Refuse
$entity.thRestrictFaction == my_faction
$entity.relativeStrength < 0","RemoveOption thRestrict_Refuse
$thRestrictFailedToRefuse = true 0
MakeOtherFleetPreventDisengage hassle false
MakeOtherFleetAggressive thRestrictedInspection false 2
MakeOtherFleetHostile thRestrictedInspection false 2","Your refusal text when the fleet is WEAKER (they back down).",cutCommLinkNoText:Cut the comm link,
```

Key points for refuse rules:
- The **strong** variant sets `$thRestrictMadeHostile = true 0` and makes the fleet hostile+aggressive, then calls `FireAll THRestrictedOptions` to re-present options (the refuse button is removed, so the player must comply or cut comms)
- The **weak** variant releases the disengage lock and de-escalates, then provides the `cutCommLinkNoText` option
- Both must set `$thRestrictFailedToRefuse = true 0` and `RemoveOption thRestrict_Refuse`

### Complete Example

A four-rule set for a hypothetical "Shadowguard" faction:

```csv
thRestrictComms_Shadow,OpenCommLink,"$entity.thRestrictConv score:200
$entity.relativeStrength >= 0
$entity.thRestrictFaction == shadowguard","$entity.ignorePlayerCommRequests = true 7
FireAll THRestrictedOptions","A masked $post materializes on screen.

""You have entered a restricted zone. Submit to scan protocol immediately. Non-compliance will be met with lethal force.""",,
thRestrictComms_ShadowWk,OpenCommLink,"$entity.thRestrictConv score:150
$entity.relativeStrength < 0
$entity.thRestrictFaction == shadowguard","$entity.ignorePlayerCommRequests = true 7
MakeOtherFleetPreventDisengage hassle false
FireAll THRestrictedOptions","A masked $post appears, voice tense.

""Captain. This zone is restricted. We... request your cooperation with a scan.""",,
thRestrictRefuseSel_Shadow_Strong,DialogOptionSelected,"$option == thRestrict_Refuse
$entity.thRestrictFaction == shadowguard
$entity.relativeStrength >= 0","RemoveOption thRestrict_Refuse
$thRestrictFailedToRefuse = true 0
$thRestrictMadeHostile = true 0
MakeOtherFleetHostile thRestrictedInspection true 3
MakeOtherFleetAggressive thRestrictedInspection true 3
FireAll THRestrictedOptions","The $post tilts $hisOrHer head. ""Unfortunate."" A pause.

""All units: weapons free.""",,
thRestrictRefuseSel_Shadow_Weak,DialogOptionSelected,"$option == thRestrict_Refuse
$entity.thRestrictFaction == shadowguard
$entity.relativeStrength < 0","RemoveOption thRestrict_Refuse
$thRestrictFailedToRefuse = true 0
MakeOtherFleetPreventDisengage hassle false
MakeOtherFleetAggressive thRestrictedInspection false 2
MakeOtherFleetHostile thRestrictedInspection false 2","A long silence. Then: ""...Acknowledged. But know that you have been logged.""

The comm link drops.",cutCommLinkNoText:Cut the comm link,
```

### What You Don't Need to Add

The following are handled by built-in rules and work for all factions automatically:
- The comply flow (inspection, CR damage, dismissal text)
- The "cut comm link" option (initiates combat)
- The hassle initiator (`thRestrictInitial`) and its `BeginFleetEncounter` trigger
- Option population (`THRestrictedOptions`)

---

## 8. API Reference

### THApi

The public entry point. All methods are static.

| Method | Description |
|--------|-------------|
| `addProgress(EventFactor, InteractionDialogAPI)` | Add a progress factor to the treasure hunt. Creates the event if needed. |
| `registerOpportunity(ITHOpportunity)` | Add a custom opportunity to the weighted pool. Call in `onGameLoad()`. |
| `registerFaction(String, THFactionConfig)` | Register a faction with full custom config. Call in `onGameLoad()`. |
| `registerFaction(String, THFactionTemplate)` | Register a faction with a preset template. Call in `onGameLoad()`. |
| `getSettings()` | Returns the `THSettings` instance (tuning knobs). |
| `getRewards()` | Returns the `THRewardRegistry` (reward/item/blueprint lookups). |

### THFactionTemplate

Enum of behavior archetypes: `SCAVENGER` (explores only), `ENFORCER` (explores and hassles).

### THFactionConfig.Builder

Builder for custom faction configs. All setters are optional except template (defaults to `SCAVENGER`).

| Method | Description |
|--------|-------------|
| `template(THFactionTemplate)` | Base behavior archetype |
| `narrativeText(String)` | Fleet transit tooltip text |
| `fleetType(String)` | Fleet doctrine type override |
| `fleetName(String)` | Display name override |
| `freighterPts(float)` | Freighter fleet points override |
| `tankerPts(float)` | Tanker fleet points override |
| `fleetCreator(THSwarmFleetCreator)` | Custom fleet factory |
| `aiCreator(THSwarmAICreator)` | Custom fleet AI factory |
| `build()` | Returns immutable `THFactionConfig` |

### BaseTHOpportunity

Recommended base class for custom opportunities. Handles decay and persistence automatically.

| Method | Returns | Description |
|--------|---------|-------------|
| `getProbabilityWeight()` | `float` | Returns `1 / (1 + n)^2` decay weight. Override for custom weighting. |
| `trigger()` | `void` | Increments count and persists. Call `super.trigger()` in overrides. |
| `getDisplayName()` | `String` | *(abstract)* Short name shown to the player (e.g. "Scavenger Swarm"). |
| `getIcon()` | `String` | *(abstract)* Sprite ID registered under `treasure_hunt_events` in settings.json. |
| `getIconPath()` | `String` | Resolves `getIcon()` to a full sprite path via `getSpriteName()`. |
| `getTimesTriggered()` | `int` | Current trigger count (restored from save data). |

### ITHOpportunity

Low-level interface for full control. Use `BaseTHOpportunity` instead unless you need custom persistence or weighting logic.

| Method | Returns | Description |
|--------|---------|-------------|
| `getProbabilityWeight()` | `float` | Selection weight. Must decay to avoid dominating the pool. |
| `trigger()` | `void` | Called when selected. You are responsible for your own persistence. |
| `getDisplayName()` | `String` | Short name shown to the player when the opportunity is found. |
| `getIcon()` | `String` | Sprite ID registered under `treasure_hunt_events` in settings.json. |
| `ICON_CATEGORY` | `String` | Constant: `"treasure_hunt_events"`. The graphics category for opportunity icons. |

### Settings.json Keys

All keys auto-merge across mods.

| Key | Type | Description |
|-----|------|-------------|
| `th_rewards` | JSONObject | Entity type → `{ value, description }` or alias string |
| `th_one_time_items` | JSONArray | Special item IDs consumed on first find |
| `th_repeat_items` | JSONArray | Special item IDs that persist in the pool |
| `th_blueprints_packages` | JSONObject | Package name → `{ fighters, ships, weapons }` |
| `th_factions` | JSONObject | Faction ID → `{ template, narrativeText?, fleetType?, fleetName?, freighterPts?, tankerPts? }` |
