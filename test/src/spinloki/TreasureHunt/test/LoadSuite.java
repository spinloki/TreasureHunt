package spinloki.TreasureHunt.test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.factors.THColonyRuinFactor;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;
import spinloki.TreasureHunt.internal.intel.THClanRivalryIntel;
import spinloki.TreasureHunt.internal.intel.THExcavationRaidListener;
import spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.util.Map;

import static spinloki.TreasureHunt.test.NewGameSuite.*;
import static spinloki.TreasureHunt.test.World.*;

public class LoadSuite extends Suite {
    @Override
    @SuppressWarnings("unchecked")
    protected void tests() {
        Map<String, Object> expect = (Map<String, Object>) Global.getSector().getPersistentData().get(EXPECT_KEY);
        notNull(expect, "expectations saved by NewGameSuite");

        test("factor tracker and raid listener are registered exactly once after loading", () -> {
            eq(1, countScripts(THFactorTracker.class), "THFactorTracker sector scripts");
            eq(1, Global.getSector().getListenerManager().getListeners(THFactorTracker.class).size(), "THFactorTracker listeners");
            eq(1, Global.getSector().getListenerManager().getListeners(THExcavationRaidListener.class).size(), "THExcavationRaidListener listeners");
        });

        test("hunt progress, pools and factors survive the save", () -> {
            notNull(hunt(), "hunt intel");
            eq(expect.get("progress"), progress(), "progress");
            eq(expect.get("oneTimePool"), hunt().getOneTimePool().size(), "one-time pool size");
            eq(expect.get("repeatablePool"), hunt().getRepeatablePool().size(), "repeatable pool size");
            eq(expect.get("factors"), hunt().getFactors().size(), "factor count");
        });

        test("opportunity trigger counts are restored into the new registry", () -> {
            Map<String, Integer> saved = (Map<String, Integer>) expect.get("triggers");
            for (ITHOpportunity o : THRegistry.getOpportunityRegistry().getAll()) {
                eq(saved.get(o.getClass().getName()), ((BaseTHOpportunity) o).getTimesTriggered(), o.getDisplayName() + " trigger count");
            }
        });

        test("the excavation station keeps its interaction config, or its planet moved on to ground ops", () -> {
            SectorEntityToken planet = notNull(Global.getSector().getEntityById((String) expect.get("excavationPlanet")), "excavation planet");
            if (!(Boolean) expect.get("excavationStationAlive")) {
                check(planet.getMemoryWithoutUpdate().getBoolean("$th_excavation_ground_ops"), "station died before the save but the planet is not in ground ops");
                return;
            }
            CampaignFleetAPI station = null;
            for (THRuinExcavationIntel ex : intel(THRuinExcavationIntel.class)) {
                CampaignFleetAPI f = ex.getStationFleet();
                if (f != null && f.getId().equals(expect.get("excavationStation"))) station = f;
            }
            notNull(station, "excavation station " + expect.get("excavationStation"));
            check(station.isAlive(), "station fleet is dead");
            notNull(station.getMemoryWithoutUpdate().get("$fidConifgGen"), "station interaction config");
        });

        test("rivalry state and colony factor survive the save", () -> {
            check(THUtils.isClanRivalryActive(), "rivalry no longer active");
            notNull(THClanRivalryIntel.get(), "rivalry intel");
            check(rivalryFactor() != null, "rivalry factor missing from hostile activity");
            check(colonyFactor() != null, "colony ruins factor missing");
        });

        test("35 days pass with every feature active, and the colony keeps feeding the hunt", () -> {
            SectorEntityToken colony = Global.getSector().getEconomy().getMarket((String) expect.get("colony")).getPrimaryEntity();
            moveObserver(colony.getContainingLocation(), colony.getLocation().x + 3000, colony.getLocation().y);
            int before = progress();
            days(35);
            MarketAPI market = Global.getSector().getEconomy().getMarket((String) expect.get("colony"));
            check(market != null && market.isPlayerOwned(), "colony lost during the run");
            check(progress() > before, "progress went from %d to %d over a month with a ruins colony", before, progress());
        });

        test("the clan raid ran without leaving fleets in limbo", () -> {
            for (THClanRaidFGI raid : intel(THClanRaidFGI.class)) {
                for (CampaignFleetAPI f : raid.getFleets()) {
                    check(f.getContainingLocation() != null || !f.isAlive(), "raid fleet %s has no location", f.getName());
                }
            }
        });
    }

    private static EventFactor colonyFactor() {
        for (EventFactor f : hunt().getFactors()) if (f instanceof THColonyRuinFactor) return f;
        return null;
    }
}
