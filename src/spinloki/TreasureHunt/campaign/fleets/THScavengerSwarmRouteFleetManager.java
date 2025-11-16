package spinloki.TreasureHunt.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class THScavengerSwarmRouteFleetManager extends BaseRouteFleetManager {

    private final StarSystemAPI system;
    private final Deque<FactionAPI> factionAPIDeque = new ArrayDeque<>();

    public THScavengerSwarmRouteFleetManager(StarSystemAPI system) {
        super(1, 4);
        this.system = system;
        for (var faction: THScavengerSwarmIntel.getFactionsWithAIAndFleetCreators().keySet()){
            factionAPIDeque.addLast(faction);
        }
    }

    @Override
    protected String getRouteSourceId() {
        return "th_scavswarm_" + system.getId();
    }

    @Override
    protected int getMaxFleets() {
        return 20;
    }

    @Override
    protected void addRouteFleetIfPossible() {
        if (factionAPIDeque.isEmpty()) return;

        var faction = factionAPIDeque.removeFirst();
        factionAPIDeque.addLast(faction);

        MarketAPI source = pickSourceMarket(faction);
        long seed = new Random().nextLong();
        RouteData route = RouteManager.getInstance().addRoute(getRouteSourceId(),
                source,
                seed,
                new OptionalFleetData(source),
                this);

        float distLY = Misc.getDistanceLY(source.getLocationInHyperspace(), system.getLocation());
        float travel = distLY * 1.25f;
        float prep = 1f + (float) Math.random() * 3f;
        float stay = 10f + (float) Math.random() * 15f;
        float end = 3f + (float) Math.random() * 2f;
        route.addSegment(new RouteSegment(prep, source.getPrimaryEntity()));
        route.addSegment(new RouteSegment(travel, source.getPrimaryEntity(), system.getCenter()));
        route.addSegment(new RouteSegment(stay, system.getCenter()));
        route.addSegment(new RouteSegment(travel, system.getCenter(), source.getPrimaryEntity()));
        route.addSegment(new RouteSegment(end, source.getPrimaryEntity()));
    }


    private MarketAPI pickSourceMarket(FactionAPI faction) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {

            if (market.isHidden()) continue;
            if (market.getContainingLocation() == null) continue;

            if (!faction.getId().equals(market.getFactionId()))
                continue;

            float distLY = Misc.getDistanceLY(system.getLocation(), market.getLocationInHyperspace());
            float weight = market.getSize();

            float f = Math.max(0.1f, 1f - Math.min(1f, distLY / 25f));
            weight *= (f * f);

            picker.add(market, weight);
        }

        return picker.pick();
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {
        // Determine faction from route market
        String factionId = route.getMarket().getFactionId();
        FactionAPI faction = Global.getSector().getFaction(factionId);

        var fleetDelegate = THScavengerSwarmIntel.getFactionsWithAIAndFleetCreators().get(faction).two;
        CampaignFleetAPI fleet;
        if (fleetDelegate != null){
            fleet = fleetDelegate.createFleet(system, route, route.getMarket(), route.getRandom());
        }
        else{
            fleet = RuinsFleetRouteManager.createScavenger(
                            null,
                            system.getLocation(),
                            route,
                            route.getMarket(),
                            false,
                            route.getRandom()
                    );
        }

        if (fleet == null) return null;

        var aiDelegate = THScavengerSwarmIntel.getFactionsWithAIAndFleetCreators().get(faction).one;
        if (aiDelegate != null) {
            fleet.addScript(aiDelegate.create(fleet, route));
        } else {
            fleet.addScript(new ScavengerFleetAssignmentAI(fleet, route, false));
        }

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);
        return fleet;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData route) {
        return false;
    }

    @Override
    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) {

    }
}
