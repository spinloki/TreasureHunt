package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dev/testing command: instantly stands up a player colony on a random ruins world
 * so the clan-raid opportunity has a target of a chosen defensive strength, then
 * teleports the player to it.
 *
 * The "size" parameter drives the whole target strength: colony size plus the tier
 * of station, military HQ, ground defenses, and spaceport scale off it. Iterate by
 * reloading and re-running with a different size to see how the raid scales.
 *
 * Usage: th_maketestcolony [size=5] [notechmining]
 *   size         3-10 (default 5). Sets colony size and scales all defenses.
 *   notechmining omit tech mining (which boosts the hunt contribution / raid weight).
 *
 * Follows the vanilla colony-establish flow (createMarket -> econGroup -> addMarket ->
 * station-after), copying the planet's ruins/resource/hazard conditions onto a fresh
 * player market, then fires reportPlayerColonizedPlanet so the normal colonization
 * hooks (faction definition, colony intel, etc.) run.
 */
public class TH_MakeTestColony implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        int size = 5;
        boolean techMining = true;
        if (args != null && !args.trim().isEmpty()) {
            for (String tok : args.trim().split("\\s+")) {
                if (tok.equalsIgnoreCase("notechmining")) techMining = false;
                else if (tok.matches("\\d+")) size = Integer.parseInt(tok);
            }
        }
        size = THUtils.clamp(size, 1, 10);

        PlanetAPI planet = pickRuinsPlanet();
        if (planet == null) {
            Console.showMessage("Error: no uncolonized ruins planet found in the sector.");
            return CommandResult.ERROR;
        }

        MarketAPI oldMarket = planet.getMarket();
        if (!THUtils.isUncolonizedRuinsWorld(oldMarket)) {
            Console.showMessage("Error: " + planet.getName() + " is not an uninhabited ruins world "
                    + "(faction=" + oldMarket.getFactionId() + ", size=" + oldMarket.getSize()
                    + ", inEconomy=" + oldMarket.isInEconomy() + "). Refusing to replace its market.");
            return CommandResult.ERROR;
        }
        String ruinsType = Misc.getRuinsType(oldMarket);

        // Capture the planet's conditions (ruins, resources, hazard) to carry over.
        List<String> conditionIds = new ArrayList<>();
        if (oldMarket != null) {
            for (MarketConditionAPI c : oldMarket.getConditions()) {
                if (c.getId() != null) conditionIds.add(c.getId());
            }
            Global.getSector().getEconomy().removeMarket(oldMarket);
        }

        // ---- Build a fresh player market (vanilla-style) ----
        String marketId = "th_testcolony_" + planet.getId();
        MarketAPI market = Global.getFactory().createMarket(marketId, planet.getName(), size);
        market.setSize(size);
        market.setFactionId(Factions.PLAYER);
        market.setPlayerOwned(true);

        for (String cid : conditionIds) {
            try { market.addCondition(cid); } catch (Throwable t) { /* skip conditions that won't reapply */ }
        }

        String station  = size >= 6 ? Industries.STARFORTRESS : size >= 4 ? Industries.BATTLESTATION : Industries.ORBITALSTATION;
        String military  = size >= 6 ? Industries.HIGHCOMMAND  : size >= 4 ? Industries.MILITARYBASE  : Industries.PATROLHQ;
        String port      = size >= 5 ? Industries.MEGAPORT      : Industries.SPACEPORT;
        String groundDef = size >= 5 ? Industries.HEAVYBATTERIES : Industries.GROUNDDEFENSES;

        // Non-station industries before addMarket; station industry after (vanilla pattern).
        // No regular mining — tech mining is the ruins-relevant industry (and the hunt driver).
        List<String> industries = new ArrayList<>(List.of(
                Industries.POPULATION, port, military, groundDef));
        if (techMining) industries.add(Industries.TECHMINING);
        for (String ind : industries) market.addIndustry(ind);

        for (String sm : new String[]{
                Submarkets.SUBMARKET_STORAGE, Submarkets.LOCAL_RESOURCES,
                Submarkets.GENERIC_MILITARY, Submarkets.SUBMARKET_OPEN, Submarkets.SUBMARKET_BLACK}) {
            if (!market.hasSubmarket(sm)) market.addSubmarket(sm);
        }

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
        market.setPrimaryEntity(planet);
        planet.setMarket(market);
        planet.setFaction(Factions.PLAYER);
        market.reapplyIndustries();

        Global.getSector().getEconomy().addMarket(market, true);

        // Station industry AFTER addMarket, then instantly finish all builds.
        market.addIndustry(station);
        for (Industry ind : market.getIndustries()) ind.finishBuildingOrUpgrading();

        Misc.setFullySurveyed(market, null, false);
        ListenerUtil.reportPlayerColonizedPlanet(planet);

        // ---- Teleport the player to the new colony ----
        StarSystemAPI system = planet.getStarSystem();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI from = player.getContainingLocation();
        if (from != null && from != system) from.removeEntity(player);
        if (player.getContainingLocation() != system) system.addEntity(player);
        player.setLocation(planet.getLocation().x + 150f, planet.getLocation().y + 150f);
        Global.getSector().setCurrentLocation(system);

        Console.showMessage(String.format(
                "Colony established: %s (%s) size %d in %s.",
                market.getName(), ruinsType, size, system.getName()));
        Console.showMessage(String.format(
                "Industries: %s, station=%s, military=%s, ground=%s, techmining=%s",
                port, station, military, groundDef, techMining));
        Console.showMessage("Teleported to the colony. Income/defenses settle on the next economy update.");
        return CommandResult.SUCCESS;
    }

    private PlanetAPI pickRuinsPlanet() {
        List<PlanetAPI> vast = new ArrayList<>();
        List<PlanetAPI> anyRuins = new ArrayList<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.isStar() || planet.isGasGiant()) continue;
                MarketAPI m = planet.getMarket();
                if (!THUtils.isUncolonizedRuinsWorld(m)) continue;
                anyRuins.add(planet);
                if ("ruins_vast".equals(Misc.getRuinsType(m))) vast.add(planet);
            }
        }
        Random r = new Random();
        if (!vast.isEmpty()) return vast.get(r.nextInt(vast.size()));
        if (!anyRuins.isEmpty()) {
            Console.showMessage("No vast-ruins planet found; using a lesser ruins planet.");
            return anyRuins.get(r.nextInt(anyRuins.size()));
        }
        return null;
    }
}
