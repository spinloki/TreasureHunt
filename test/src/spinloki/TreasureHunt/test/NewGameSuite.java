package spinloki.TreasureHunt.test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.impl.items.MultiBlueprintItemPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.events.THChooseLeadScript;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.events.THLeadPickDialogPlugin;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.factors.THClanRivalryFactor;
import spinloki.TreasureHunt.internal.factors.THColonyRuinFactor;
import spinloki.TreasureHunt.internal.factors.THSalvageFactor;
import spinloki.TreasureHunt.internal.factors.THSectorSprintFactor;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;
import spinloki.TreasureHunt.internal.intel.THClanRivalryIntel;
import spinloki.TreasureHunt.internal.intel.THExcavationRaidListener;
import spinloki.TreasureHunt.internal.intel.THFoundTreasureIntel;
import spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel;
import spinloki.TreasureHunt.internal.intel.THScavengerSwarmIntel;
import spinloki.TreasureHunt.internal.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.internal.intel.THStationLeadIntel;
import spinloki.TreasureHunt.internal.opportunities.THClanRivalryOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THRuinExcavationOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THScavengerSwarmOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THSectorSprintOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THStationLeadOpportunity;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.internal.registry.THRewardRegistry;
import spinloki.TreasureHunt.util.THRewardItem;
import spinloki.TreasureHunt.util.THUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static spinloki.TreasureHunt.test.World.*;

public class NewGameSuite extends Suite {
    static final String EXPECT_KEY = "th_test_expect";
    static final String PACKAGE_HULLMOD = "spinloki.TreasureHunt.data.hullmods.THTreasureHuntPackage";

    private final Map<String, Object> expect = new HashMap<>();
    private MarketAPI colony;

    @Override
    protected void tests() {
        test("factor tracker and raid listener are registered exactly once", () -> {
            eq(1, countScripts(THFactorTracker.class), "THFactorTracker sector scripts");
            eq(1, Global.getSector().getListenerManager().getListeners(THFactorTracker.class).size(), "THFactorTracker listeners");
            eq(1, Global.getSector().getListenerManager().getListeners(THExcavationRaidListener.class).size(), "THExcavationRaidListener listeners");
        });

        test("the five built-in opportunities are registered", () -> {
            Set<String> names = new TreeSet<>();
            for (ITHOpportunity o : THRegistry.getOpportunityRegistry().getAll()) names.add(o.getDisplayName());
            eq(new TreeSet<>(List.of("Clanner Rivalry", "Ruin Excavation", "Scavenger Swarm", "Sector Sprint", "Station Lead")),
                    names, "registered opportunities");
        });

        test("every pooled reward item names a real special item", () -> {
            THRewardRegistry rewards = THRegistry.getRewardRegistry();
            List<String> missing = new ArrayList<>();
            for (String id : rewards.getOneTimeItems()) if (!exists(() -> Global.getSettings().getSpecialItemSpec(id))) missing.add(id);
            for (String id : rewards.getRepeatItems()) if (!exists(() -> Global.getSettings().getSpecialItemSpec(id))) missing.add(id);
            check(missing.isEmpty(), "missing special items: %s", missing);
            check(!rewards.getRepeatItems().isEmpty(), "no repeatable items configured");
        });

        test("blueprint packages name only real hulls, weapons and wings", () -> {
            THRewardRegistry rewards = THRegistry.getRewardRegistry();
            List<String> problems = new ArrayList<>();
            for (String key : rewards.getAllBlueprintPackages()) {
                THBlueprintPackage pkg = rewards.getBlueprintPackage(key);
                for (String id : pkg.getShips()) if (!exists(() -> Global.getSettings().getHullSpec(id))) problems.add(key + ": hull " + id);
                for (String id : pkg.getWeapons()) if (!exists(() -> Global.getSettings().getWeaponSpec(id))) problems.add(key + ": weapon " + id);
                for (String id : pkg.getFighters()) if (!exists(() -> Global.getSettings().getFighterWingSpec(id))) problems.add(key + ": wing " + id);
                Set<String> tags = pkg.getEffectiveTags();
                int resolved = MultiBlueprintItemPlugin.getShipIds(tags).size() + MultiBlueprintItemPlugin.getWeaponIds(tags).size()
                        + MultiBlueprintItemPlugin.getWingIds(tags).size();
                if (resolved == 0) problems.add(key + ": resolves to nothing for tags " + tags);
                if (!pkg.usesTagExpression() && !MultiBlueprintItemPlugin.getShipIds(tags).containsAll(pkg.getShips())) {
                    problems.add(key + ": listed hulls not all tagged with the package key");
                }
            }
            check(problems.isEmpty(), "%d problems:\n%s", problems.size(), String.join("\n", problems));
        });

        test("reward table keys match real entity types and ruins conditions", () -> {
            JSONObject table = Global.getSettings().getJSONObject("th_rewards");
            Set<String> groups = new HashSet<>();
            for (Iterator<?> it = table.keys(); it.hasNext(); ) {
                Object v = table.get((String) it.next());
                if (v instanceof String s) groups.add(s);
            }
            List<String> missing = new ArrayList<>();
            for (Iterator<?> it = table.keys(); it.hasNext(); ) {
                String key = (String) it.next();
                if (groups.contains(key) || key.equals("raid")) continue;
                if (key.startsWith("ruins_")) {
                    if (!exists(() -> Global.getSettings().getMarketConditionSpec(key))) missing.add("condition " + key);
                } else if (!exists(() -> Global.getSettings().getCustomEntitySpec(key))) {
                    missing.add("entity " + key);
                }
            }
            check(missing.isEmpty(), "unknown reward keys: %s", missing);
        });

        test("salvaging each kind of rewarded entity grants its configured progress", () -> {
            THRewardRegistry rewards = THRegistry.getRewardRegistry();
            JSONObject table = Global.getSettings().getJSONObject("th_rewards");
            List<String> tested = new ArrayList<>();
            List<String> wrong = new ArrayList<>();
            for (Iterator<?> it = table.keys(); it.hasNext(); ) {
                String type = (String) it.next();
                if (!exists(() -> Global.getSettings().getCustomEntitySpec(type))) continue;
                List<SectorEntityToken> found = entitiesOfType(type);
                if (found.isEmpty()) continue;
                resetProgress();
                loot(found.get(0), null);
                EventFactor f = newestFactor();
                int want = rewards.getRewardValue(type);
                if (!(f instanceof THSalvageFactor) || f.getProgress(hunt()) != want || progress() != want) {
                    wrong.add(type + " expected " + want + " got factor " + describe(f) + ", progress " + progress());
                }
                tested.add(type);
            }
            check(wrong.isEmpty(), "%s", wrong);
            check(tested.size() >= 5, "only %d rewarded entity types were found in the sector: %s", tested.size(), tested);
        });

        test("exploring ruins grants progress by ruins size", () -> {
            List<String> wrong = new ArrayList<>();
            int tested = 0;
            for (String ruins : List.of("ruins_scattered", "ruins_widespread", "ruins_extensive", "ruins_vast")) {
                PlanetAPI planet = uncolonizedRuinsPlanet(ruins);
                if (planet == null) continue;
                tested++;
                resetProgress();
                loot(planet, null);
                int want = THRegistry.getRewardRegistry().getRewardValue(ruins);
                if (progress() != want) wrong.add(ruins + " on " + planet.getName() + ": expected " + want + ", got " + progress());
            }
            check(wrong.isEmpty(), "%s", wrong);
            check(tested >= 3, "only %d ruins sizes present in the sector", tested);
        });

        test("raiding a market grants progress with diminishing returns on repeat raids", () -> {
            MarketAPI market = null;
            for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
                if (!m.isPlayerOwned() && !m.isHidden() && m.getPrimaryEntity() != null && !Misc.hasRuins(m)) {
                    market = m;
                    break;
                }
            }
            notNull(market, "raidable market");
            int base = THRegistry.getRewardRegistry().getRewardValue("raid");
            float factor = THRegistry.getSettings().getRaidDiminishingReturnsFactor();
            resetProgress();
            market.getMemoryWithoutUpdate().set(Misc.LAST_RAIDED_AT, Global.getSector().getClock().getTimestamp());
            loot(market.getPrimaryEntity(), null);
            eq(base, progress(), "first raid on " + market.getName());
            resetProgress();
            market.getMemoryWithoutUpdate().set(Misc.LAST_RAIDED_AT, Global.getSector().getClock().getTimestamp());
            loot(market.getPrimaryEntity(), null);
            eq((int) (base * factor), progress(), "immediate second raid");
        });

        test("destroying scavenger ships in a battle grants progress by hull value", () -> {
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            CampaignFleetAPI scav = RuinsFleetRouteManager.createScavenger(FleetTypes.SCAVENGER_LARGE,
                    pf.getLocationInHyperspace(), null, false, new Random(7));
            pf.getContainingLocation().addEntity(scav);
            scav.setLocation(pf.getLocation().x + 50, pf.getLocation().y);
            check(THUtils.isScavenger(scav), "vanilla scavenger fleet not recognised as a scavenger");
            var battle = Global.getFactory().createBattle(pf, scav);
            scav.getFleetData().takeSnapshot();
            List<FleetMemberAPI> members = scav.getFleetData().getMembersListCopy();
            float value = 0;
            for (int i = 0; i < 4; i++) {
                value += members.get(i).getHullSpec().getBaseValue();
                scav.getFleetData().removeFleetMember(members.get(i));
            }
            int want = THUtils.clamp(Math.round(value / 10000f), 1, 50);
            resetProgress();
            loot(scav, null);
            battle.leave(pf, false);
            battle.leave(scav, false);
            scav.despawn();
            eq(want, progress(), "progress for " + value + " credits of hulls");
        });

        test("salvaging something without a reward grants nothing", () -> {
            resetProgress();
            int factors = hunt().getFactors().size();
            SectorEntityToken jump = Global.getSector().getHyperspace().getJumpPoints().get(0);
            loot((SectorEntityToken) jump, null);
            eq(factors, hunt().getFactors().size(), "factor count");
            eq(0, progress(), "progress");
        });

        test("the Treasure Hunt Package hullmod boosts one-time progress", () -> {
            FleetMemberAPI flagship = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy().get(0);
            String type = firstRewardedEntityType();
            SectorEntityToken target = entitiesOfType(type).get(0);
            int base = THRegistry.getRewardRegistry().getRewardValue(type);
            flagship.getVariant().addMod(PACKAGE_HULLMOD);
            flagship.updateStats();
            try {
                resetProgress();
                loot(target, null);
                int max = (int) (base * THRegistry.getSettings().getTreasureHuntPackageMaxMult());
                eq(Math.min(max, base + 1), progress(), "boosted progress with a frigate carrying the package");
            } finally {
                flagship.getVariant().removeMod(PACKAGE_HULLMOD);
                flagship.updateStats();
            }
        });

        test("every pooled token resolves to a named item", () -> {
            List<String> bad = new ArrayList<>();
            Set<String> all = new HashSet<>(hunt().getOneTimePool());
            all.addAll(hunt().getRepeatablePool());
            for (String token : all) {
                THRewardItem item = THRewardItem.parse(token);
                if (!item.isValid() || item.getDisplayName() == null || item.getDisplayName().isBlank()) bad.add(token);
            }
            check(bad.isEmpty(), "unresolvable pool entries: %s", bad);
            check(!hunt().getOneTimePool().isEmpty(), "one-time pool is empty on a new game");
        });

        test("looting a pooled item takes it out of the pool", () -> {
            String token = hunt().getOneTimePool().iterator().next();
            THRewardItem item = THRewardItem.parse(token);
            CargoAPI cargo = Global.getFactory().createCargo(true);
            cargo.addSpecial(item.toSpecialItemData(), 1);
            loot(entitiesOfType(firstRewardedEntityType()).get(0), cargo);
            check(!hunt().getOneTimePool().contains(token), "%s still pooled after being looted", token);
        });

        test("reaching 100 progress offers leads, and declining them resets progress", () -> {
            resetProgress();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(100, "a test"), null);
            Dialogs.Open open = awaitLeadDialog();
            int expected = Math.min(THRegistry.getSettings().getNumLeadCandidates(),
                    hunt().getOneTimePool().size() + hunt().getRepeatablePool().size());
            eq(expected + 1, open.options.size(), "lead options plus 'None of these'");
            Set<Object> offered = new HashSet<>(open.options.keySet());
            offered.remove("cancel");
            open.select("cancel");
            until(2, () -> countScripts(THChooseLeadScript.class) == 0);
            eq(0, progress(), "progress after declining");
            for (Object token : offered) {
                check(!hunt().getOneTimePool().contains(token) && !hunt().getRepeatablePool().contains(token),
                        "declined lead %s is still pooled", token);
            }
        });

        test("choosing a lead makes it the hunt's treasure", () -> {
            resetProgress();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(100, "a test"), null);
            Dialogs.Open open = awaitLeadDialog();
            Object pick = open.options.keySet().iterator().next();
            open.select(pick);
            until(2, () -> countScripts(THChooseLeadScript.class) == 0);
            eq(pick, field(hunt(), "treasure"), "hunt treasure");
            eq(100, progress(), "progress after choosing");
        });

        test("reaching 300 progress triggers an opportunity", () -> {
            int before = totalTriggers();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(200, "a test"), null);
            frames(2);
            eq(before + 1, totalTriggers(), "opportunity trigger count");
            notNull(field(hunt(), "opportunityDisplayName"), "opportunity name on the hunt");
        });

        test("reaching 500 progress delivers the chosen treasure on the next loot screen", () -> {
            String treasure = field(hunt(), "treasure");
            check(treasure != null && !treasure.isEmpty(), "no treasure chosen");
            int intelBefore = intel(THFoundTreasureIntel.class).size();
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(200, "a test"), null);
            frames(2);
            eq(0, progress(), "progress after the hunt completes");
            eq("", field(hunt(), "treasure"), "treasure after the hunt completes");
            eq(intelBefore + 1, intel(THFoundTreasureIntel.class).size(), "found-treasure intel count");
            CargoAPI cargo = loot(entitiesOfType(firstRewardedEntityType()).get(0), null);
            SpecialItemData want = THRewardItem.parse(treasure).toSpecialItemData();
            eq(1f, cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, want), "treasure in loot");
            CargoAPI again = loot(entitiesOfType(firstRewardedEntityType()).get(0), null);
            eq(0f, again.getQuantity(CargoAPI.CargoItemType.SPECIAL, want), "treasure in a second loot screen");
        });

        test("every station name Station Lead searches for matches real derelict stations", () -> {
            List<String> findables = field(opportunity(THStationLeadOpportunity.class), "findables");
            List<String> unmatched = new ArrayList<>();
            for (String name : findables) {
                if (THUtils.getNearestEntitiesWithName(Global.getSector().getPlayerFleet(), 1000, name, true).isEmpty()) unmatched.add(name);
            }
            check(unmatched.isEmpty(), "no entities named %s in the sector", unmatched);
        });

        test("Station Lead reveals three undiscovered derelict stations", () -> {
            int before = intel(THStationLeadIntel.class).size();
            opportunity(THStationLeadOpportunity.class).trigger();
            List<THStationLeadIntel> leads = intel(THStationLeadIntel.class);
            eq(before + 3, leads.size(), "station lead intel count");
            for (THStationLeadIntel lead : leads.subList(before, leads.size())) {
                SectorEntityToken station = field(lead, "station");
                check(List.of("Research Station", "Mining Station", "Orbital Habitat").contains(station.getName()),
                        "lead points at %s", station.getName());
                check(station.isDiscoverable(), "lead points at the already-discovered %s", station.getName());
            }
            check(opportunity(THStationLeadOpportunity.class).getProbabilityWeight() > 0, "Station Lead ran out of stations");
        });

        test("Sector Sprint pays out once the player owns a comm relay in a resonant system", () -> {
            int before = intel(THSectorSprintIntel.class).size();
            opportunity(THSectorSprintOpportunity.class).trigger();
            List<THSectorSprintIntel> sprints = intel(THSectorSprintIntel.class);
            eq(before + 3, sprints.size(), "sector sprint intel count");
            StarSystemAPI system = field(sprints.get(sprints.size() - 1), "targetSystem");
            List<SectorEntityToken> relays = system.getEntitiesWithTag(Tags.COMM_RELAY);
            if (relays.isEmpty()) {
                SectorEntityToken stable = system.getEntitiesWithTag(Tags.STABLE_LOCATION).get(0);
                SectorEntityToken relay = system.addCustomEntity(null, "Comm Relay", "comm_relay_makeshift", "player");
                relay.setLocation(stable.getLocation().x, stable.getLocation().y);
            } else {
                relays.get(0).setFaction("player");
            }
            until(1, () -> sprintFactor() != null);
            EventFactor f = notNull(sprintFactor(), "sector sprint factor");
            check(f.getProgress(hunt()) > 0, "sector sprint factor gives %d", f.getProgress(hunt()));
        });

        test("Ruin Excavation stations a defended excavation at a ruins world", () -> {
            THRuinExcavationOpportunity opp = opportunity(THRuinExcavationOpportunity.class);
            check(opp.getProbabilityWeight() > 0, "no valid excavation targets in a new sector");
            int before = intel(THRuinExcavationIntel.class).size();
            opp.trigger();
            List<THRuinExcavationIntel> all = intel(THRuinExcavationIntel.class);
            eq(before + 1, all.size(), "excavation intel count");
            THRuinExcavationIntel ex = all.get(all.size() - 1);
            CampaignFleetAPI station = notNull(ex.getStationFleet(), "station fleet");
            PlanetAPI planet = field(ex, "planet");
            check(station.isAlive() && station.getContainingLocation() == planet.getContainingLocation(), "station not at %s", planet.getName());
            notNull(station.getMemoryWithoutUpdate().get("$fidConifgGen"), "station interaction config");
            check(planet.getMemoryWithoutUpdate().getBoolean("$th_excavation_blocked"), "planet not marked as under excavation");
            expect.put("excavationStation", station.getId());
            expect.put("excavationPlanet", planet.getId());
        });

        test("Scavenger Swarm sends scavenger fleets to its target system", () -> {
            opportunity(THScavengerSwarmOpportunity.class).trigger();
            List<THScavengerSwarmIntel> swarms = intel(THScavengerSwarmIntel.class);
            StarSystemAPI target = field(swarms.get(swarms.size() - 1), "target");
            moveObserver(target, -20000, -20000);
            String source = "th_scavswarm_" + target.getId();
            until(10, () -> !RouteManager.getInstance().getRoutesForSource(source).isEmpty());
            float arrival = Float.MAX_VALUE;
            for (RouteManager.RouteData route : RouteManager.getInstance().getRoutesForSource(source)) {
                arrival = Math.min(arrival, route.getSegments().get(0).daysMax + route.getSegments().get(1).daysMax);
            }
            check(arrival < Float.MAX_VALUE, "swarm created no routes to %s", target.getName());
            final float budget = arrival + 5;
            until(budget, () -> swarmFleetsIn(target) >= 1);
            check(swarmFleetsIn(target) >= 1, "no swarm fleet reached %s within %.0f days", target.getName(), budget);
        });

        test("colonizing a ruins world adds a monthly colony ruins factor", () -> {
            PlanetAPI planet = uncolonizedRuinsPlanet("ruins_vast");
            if (planet == null) planet = uncolonizedRuinsPlanet("ruins_extensive");
            notNull(planet, "vast or extensive ruins world");
            String ruins = Misc.getRuinsType(planet.getMarket());
            colony = colonize(planet, 5, true);
            THColonyRuinFactor f = null;
            for (EventFactor curr : hunt().getFactors()) if (curr instanceof THColonyRuinFactor c) f = c;
            notNull(f, "colony ruins factor");
            int want = THRegistry.getSettings().getColonyRuinsBaseProgress(ruins) * THRegistry.getSettings().getColonyTechMiningProgressMultiplier();
            eq(want, f.getProgress(hunt()), "monthly progress for a tech-mined " + ruins + " colony");
            expect.put("colony", colony.getId());
        });

        test("Clanner Rivalry adds a hostile activity factor for the ruins colony", () -> {
            notNull(colony, "colony from the previous test");
            if (!THUtils.isClanRivalryGateMet()) {
                PlanetAPI second = uncolonizedRuinsPlanet("ruins_extensive");
                notNull(second, "second extensive ruins world for the rivalry gate");
                colonize(second, 4, true);
            }
            THClanRivalryOpportunity opp = opportunity(THClanRivalryOpportunity.class);
            check(opp.getProbabilityWeight() > 0, "rivalry unavailable with a ruins colony");
            opp.trigger();
            check(THUtils.isClanRivalryActive(), "rivalry not active");
            notNull(THClanRivalryIntel.get(), "rivalry intel");
            until(60, () -> HostileActivityEventIntel.get() != null && rivalryFactor() != null);
            THClanRivalryFactor f = notNull(rivalryFactor(), "rivalry factor in hostile activity");
            check(!f.getCauses().isEmpty(), "rivalry factor has no causes");
        });

        test("a clan raid against the colony launches and spawns raider fleets", () -> {
            notNull(colony, "colony");
            SectorEntityToken planet = colony.getPrimaryEntity();
            moveObserver(planet.getContainingLocation(), planet.getLocation().x + 3000, planet.getLocation().y);
            THClanRaidFGI raid = notNull(THClanRaidFGI.launchAgainst(colony, 1f), "raid");
            float days = 5f;
            for (var seg : raid.getRoute().getSegments()) {
                if (seg.custom instanceof FGRaidAction) break;
                days += seg.daysMax;
            }
            final float budget = days;
            until(budget, raid::isSpawnedFleets);
            check(raid.isSpawnedFleets(), "raid spawned no fleets before reaching the colony (%.0f days): %s", budget, describeRoute(raid));
            check(!raid.getFleets().isEmpty(), "raid has no fleets");
            for (CampaignFleetAPI f : raid.getFleets()) eq("salvor_clan", f.getFaction().getId(), "raider faction");
            expect.put("raid", true);
        });

        test("record state for the save/load phase", () -> {
            Dialogs.dismissAll();
            until(2, () -> countScripts(THChooseLeadScript.class) == 0);
            SectorEntityToken station = Global.getSector().getEntityById((String) expect.get("excavationStation"));
            expect.put("excavationStationAlive", station instanceof CampaignFleetAPI f && f.isAlive());
            expect.put("progress", progress());
            expect.put("oneTimePool", hunt().getOneTimePool().size());
            expect.put("repeatablePool", hunt().getRepeatablePool().size());
            expect.put("factors", hunt().getFactors().size());
            Map<String, Integer> triggers = new HashMap<>();
            for (ITHOpportunity o : THRegistry.getOpportunityRegistry().getAll()) {
                triggers.put(o.getClass().getName(), ((BaseTHOpportunity) o).getTimesTriggered());
            }
            expect.put("triggers", triggers);
            Global.getSector().getPersistentData().put(EXPECT_KEY, expect);
        });
    }

    static int countScripts(Class<?> type) {
        int n = 0;
        for (Object s : Global.getSector().getScripts()) if (type.isInstance(s)) n++;
        return n;
    }

    static <T extends ITHOpportunity> T opportunity(Class<T> type) {
        for (ITHOpportunity o : THRegistry.getOpportunityRegistry().getAll()) if (type.isInstance(o)) return type.cast(o);
        throw new AssertionError(type.getSimpleName() + " not registered");
    }

    static int totalTriggers() {
        int n = 0;
        for (ITHOpportunity o : THRegistry.getOpportunityRegistry().getAll()) n += ((BaseTHOpportunity) o).getTimesTriggered();
        return n;
    }

    static THClanRivalryFactor rivalryFactor() {
        HostileActivityEventIntel ha = HostileActivityEventIntel.get();
        if (ha == null) return null;
        for (EventFactor f : ha.getFactors()) if (f instanceof THClanRivalryFactor r) return r;
        return null;
    }

    static int swarmFleetsIn(LocationAPI loc) {
        int n = 0;
        for (CampaignFleetAPI f : loc.getFleets()) if (f.getMemoryWithoutUpdate().getBoolean(THUtils.MEMORY_KEY_TH_SCAVENGER)) n++;
        return n;
    }

    private static EventFactor sprintFactor() {
        for (EventFactor f : hunt().getFactors()) if (f instanceof THSectorSprintFactor) return f;
        return null;
    }

    private static String firstRewardedEntityType() {
        for (String type : List.of("derelict_probe", "derelict_survey_ship", "station_research_remnant", "weapons_cache",
                "supply_cache", "equipment_cache", "wreck")) {
            if (!entitiesOfType(type).isEmpty()) return type;
        }
        throw new AssertionError("no rewarded entity in the sector");
    }

    private static String describeRoute(THClanRaidFGI raid) {
        var route = raid.getRoute();
        if (route == null) return "no route, ended=" + raid.isEnded() + ", aborted=" + raid.isAborted();
        var curr = route.getCurrent();
        float dist = Misc.getDistanceLY(route.getInterpolatedHyperLocation(), Global.getSector().getPlayerFleet().getLocationInHyperspace());
        return "source " + raid.getParams().source.getName() + ", segment " + route.getCurrentSegmentId() + "/" + route.getSegments().size()
                + " (" + (curr == null ? "none" : curr.getClass().getSimpleName() + " " + curr.elapsed + "/" + curr.daysMax) + ")"
                + ", " + dist + " LY from the player, delay " + route.getDelay() + ", segments " + segments(route)
                + ", ended=" + raid.isEnded() + ", aborted=" + raid.isAborted();
    }

    private static String segments(com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData route) {
        StringBuilder sb = new StringBuilder();
        for (var seg : route.getSegments()) {
            sb.append(seg == route.getCurrent() ? "*" : "").append(String.format("[%.1f/%.1f %s]", seg.elapsed, seg.daysMax,
                    seg.custom == null ? "" : seg.custom.getClass().getSimpleName()));
        }
        return sb.toString();
    }

    private static Dialogs.Open awaitLeadDialog() {
        until(3, () -> Dialogs.current() != null);
        Dialogs.Open open = notNull(Dialogs.current(), "lead dialog");
        check(open.plugin instanceof THLeadPickDialogPlugin, "dialog shown was %s", open.plugin.getClass().getName());
        return open;
    }

    private static boolean exists(Supplier<Object> lookup) {
        try {
            return lookup.get() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String describe(EventFactor f) {
        return f == null ? "none" : f.getClass().getSimpleName() + "(" + f.getProgress(hunt()) + ")";
    }
}
