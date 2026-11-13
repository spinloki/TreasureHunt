package spinloki.TreasureHunt.test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignEngine;
import headless.Engine;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.ArrayList;
import java.util.List;

public final class World {
    private static final float FRAME = 1f / 60f;

    private World() {}

    static void prepare() {
        Dialogs.install();
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        pf.setTransponderOn(false);
        pf.getStats().getSensorProfileMod().modifyMult("th_test", 0f);
        pf.getStats().getDetectedRangeMod().modifyMult("th_test", 0f);
        pf.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        pf.setNoEngaging(Float.MAX_VALUE);
        CampaignEngine.getInstance().setPaused(false);
    }

    public static void frames(int n) {
        CampaignEngine engine = CampaignEngine.getInstance();
        for (int i = 0; i < n; i++) Engine.advance(engine, FRAME, null);
    }

    public static void days(float days) {
        long start = Global.getSector().getClock().getTimestamp();
        while (Global.getSector().getClock().getElapsedDaysSince(start) < days) frames(1);
    }

    public static void until(float maxDays, java.util.function.BooleanSupplier done) {
        long start = Global.getSector().getClock().getTimestamp();
        while (!done.getAsBoolean() && Global.getSector().getClock().getElapsedDaysSince(start) < maxDays) frames(10);
    }

    public static TreasureHuntEventIntel hunt() {
        return TreasureHuntEventIntel.get();
    }

    public static int progress() {
        return hunt() == null ? 0 : hunt().getProgress();
    }

    public static void resetProgress() {
        if (hunt() != null) hunt().setProgress(0);
    }

    public static EventFactor newestFactor() {
        if (hunt() == null || hunt().getFactors().isEmpty()) return null;
        return hunt().getFactors().get(hunt().getFactors().size() - 1);
    }

    public static CargoAPI loot(SectorEntityToken target, CargoAPI cargo) {
        if (cargo == null) cargo = Global.getFactory().createCargo(true);
        ListenerUtil.reportAboutToShowLootToPlayer(cargo, Dialogs.targeting(target));
        frames(2);
        return cargo;
    }

    public static List<SectorEntityToken> entitiesOfType(String customEntityType) {
        List<SectorEntityToken> result = new ArrayList<>();
        for (LocationAPI loc : allLocations()) {
            for (SectorEntityToken e : loc.getAllEntities()) {
                if (customEntityType.equals(e.getCustomEntityType())) result.add(e);
            }
        }
        return result;
    }

    public static List<LocationAPI> allLocations() {
        List<LocationAPI> locs = new ArrayList<>(Global.getSector().getStarSystems());
        locs.add(Global.getSector().getHyperspace());
        return locs;
    }

    public static PlanetAPI uncolonizedRuinsPlanet(String ruinsType) {
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar() || planet.isGasGiant()) continue;
                MarketAPI m = planet.getMarket();
                if (!THUtils.isUncolonizedRuinsWorld(m)) continue;
                if (ruinsType == null || ruinsType.equals(Misc.getRuinsType(m))) return planet;
            }
        }
        return null;
    }

    public static MarketAPI colonize(PlanetAPI planet, int size, boolean techMining) {
        MarketAPI old = planet.getMarket();
        List<String> conditions = new ArrayList<>();
        for (MarketConditionAPI c : old.getConditions()) conditions.add(c.getId());
        Global.getSector().getEconomy().removeMarket(old);

        MarketAPI market = Global.getFactory().createMarket("th_test_colony_" + planet.getId(), planet.getName(), size);
        market.setFactionId(Factions.PLAYER);
        market.setPlayerOwned(true);
        for (String id : conditions) market.addCondition(id);
        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(Industries.GROUNDDEFENSES);
        if (techMining) market.addIndustry(Industries.TECHMINING);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.LOCAL_RESOURCES);
        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
        market.setPrimaryEntity(planet);
        planet.setMarket(market);
        planet.setFaction(Factions.PLAYER);
        market.reapplyIndustries();
        Global.getSector().getEconomy().addMarket(market, true);
        market.addIndustry(Industries.ORBITALSTATION);
        for (Industry ind : market.getIndustries()) ind.finishBuildingOrUpgrading();
        Misc.setFullySurveyed(market, null, false);
        ListenerUtil.reportPlayerColonizedPlanet(planet);
        return market;
    }

    @SuppressWarnings("unchecked")
    public static <T> T field(Object owner, String name) {
        for (Class<?> c = owner.getClass(); c != null; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return (T) f.get(owner);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("No field " + name + " on " + owner.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> intel(Class<T> type) {
        return (List<T>) (List<?>) Global.getSector().getIntelManager().getIntel(type);
    }

    public static void moveObserver(LocationAPI to, float x, float y) {
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        if (pf.getContainingLocation() != to) {
            pf.getContainingLocation().removeEntity(pf);
            to.addEntity(pf);
        }
        pf.setLocation(x, y);
        Global.getSector().setCurrentLocation(to);
    }
}
