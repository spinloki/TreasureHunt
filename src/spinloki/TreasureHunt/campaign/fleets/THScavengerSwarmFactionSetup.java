package spinloki.TreasureHunt.campaign.fleets;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;

import java.util.Random;

public class THScavengerSwarmFactionSetup
{
    public static void setupScavengerSwarmVanillaFactionBehaviors() {
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.PIRATES,
                THBaseScavengerSwarmFleetAssignmentAI::new,
                (system, route, sourceMarket, random1) ->
                        THScavengerSwarmFactionSetup.createGenericFleet(system, route, sourceMarket, random1, Factions.PIRATES)
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.INDEPENDENT,
                THBaseScavengerSwarmFleetAssignmentAI::new,
                (system, route, sourceMarket, random1) ->
                        THScavengerSwarmFactionSetup.createGenericFleet(system, route, sourceMarket, random1, Factions.INDEPENDENT)
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.PERSEAN,
                (fleet, route) -> new THBaseScavengerSwarmFleetAssignmentAI(fleet, route, "on an enforcement expedition"),
                THScavengerSwarmFactionSetup::createPerseanFleet
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.HEGEMONY,
                (fleet, route) -> new THBaseScavengerSwarmFleetAssignmentAI(fleet, route, "headed to suspicious system"),
                THScavengerSwarmFactionSetup::createHegemonyFleet
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.TRITACHYON,
                THBaseScavengerSwarmFleetAssignmentAI::new,
                THScavengerSwarmFactionSetup::createTritachyonFleet
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.LUDDIC_PATH,
                (fleet, route) -> new THBaseScavengerSwarmFleetAssignmentAI(fleet, route, "on an expedition to destroy heretical technology"),
                THScavengerSwarmFactionSetup::createLuddicPathFleet
        );
        THScavengerSwarmIntel.addFactionWithAIAndFleetCreators(
                Factions.LUDDIC_CHURCH,
                (fleet, route) -> new THBaseScavengerSwarmFleetAssignmentAI(fleet, route, "headed to suspicious system"),
                THScavengerSwarmFactionSetup::createLuddicChurchFleet
        );
    }

    private static CampaignFleetAPI createGenericFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random, String factionId){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        factionId,
                        null,
                        FleetTypes.SCAVENGER_LARGE,
                        40f + new Random().nextFloat() * 60,  // combat
                        20f,  // freighter
                        10f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        return fleet;
    }

    private static CampaignFleetAPI createPerseanFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        Factions.PERSEAN,
                        null,
                        FleetTypes.LEAGUE_ENFORCER,
                        40f + new Random().nextFloat() * 60,  // combat
                        20f,  // freighter
                        10f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        return fleet;
    }

    private static CampaignFleetAPI createHegemonyFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        Factions.HEGEMONY,
                        null,
                        FleetTypes.INSPECTION_FLEET,
                        40f + new Random().nextFloat() * 60,  // combat
                        20f,  // freighter
                        10f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        fleet.setName("Inspection Fleet");
        return fleet;
    }

    private static CampaignFleetAPI createTritachyonFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        Factions.TRITACHYON,
                        null,
                        FleetTypes.MERC_PRIVATEER,
                        40f + new Random().nextFloat() * 60,   // combat
                        10f,  // freighter
                        5f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        fleet.setName("Special Procurement Detachment");
        return fleet;
    }

    private static CampaignFleetAPI createLuddicPathFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        Factions.LUDDIC_PATH,
                        null,
                        FleetTypes.RAIDER,
                        40f + new Random().nextFloat() * 60,   // combat
                        10f,  // freighter
                        5f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        fleet.setName("Holy Purge Fleet");
        return fleet;
    }

    private static CampaignFleetAPI createLuddicChurchFleet(StarSystemAPI system, RouteData route, MarketAPI source, Random random){
        var fleet = FleetFactoryV3.createFleet(
                new FleetParamsV3(
                        source,
                        system.getLocation(),
                        Factions.LUDDIC_CHURCH,
                        null,
                        FleetTypes.RAIDER,
                        40f + new Random().nextFloat() * 60,   // combat
                        10f,  // freighter
                        5f,   // tanker
                        0f, 0f, 0f, 0f
                )
        );
        fleet.setName("Inquisition Fleet");
        return fleet;
    }
}
