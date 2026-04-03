package spinloki.TreasureHunt.internal.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.Random;
import java.util.Set;

public class THRuinExcavationIntel extends BaseIntelPlugin {
    private final String icon;
    private final String factionId;
    private final PlanetAPI planet;
    private CampaignFleetAPI stationFleet;
    private CampaignFleetAPI defenderFleet;
    private boolean stationCleared = false;

    public THRuinExcavationIntel(PlanetAPI planet, String factionId, String icon) {
        this.planet = planet;
        this.factionId = factionId;
        this.icon = icon;
        spawnStation();
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
    }

    private void spawnStation() {
        StarSystemAPI system = planet.getStarSystem();
        THFactionConfig config = THRegistry.getFactionRegistry().get(factionId);
        Random random = new Random();

        stationFleet = createStationFleet(config, system, random);
        configureStationInteraction(stationFleet);

        system.addEntity(stationFleet);
        stationFleet.clearAbilities();
        stationFleet.addAbility("transponder");
        stationFleet.getAbility("transponder").activate();
        stationFleet.getDetectedRangeMod().modifyFlat("th_excavation", 1000f);

        float orbitRadius = planet.getRadius() + 200f;
        float angle = random.nextFloat() * 360f;
        stationFleet.setCircularOrbit(planet, angle, orbitRadius, 45f);

        FleetMemberAPI member = stationFleet.getFleetData().getMembersListCopy().get(0);
        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

        spawnDefendingFleet(config, system, random);

        stationFleet.getMemoryWithoutUpdate().set(THUtils.MEMORY_KEY_TH_SCAVENGER, true);

        String factionName = Misc.ucFirst(Global.getSector().getFaction(factionId).getDisplayName());
        planet.getMemoryWithoutUpdate().set("$th_excavation_faction", factionName);
        planet.getMemoryWithoutUpdate().set("$th_excavation_blocked", true);
    }

    private CampaignFleetAPI createStationFleet(THFactionConfig config, StarSystemAPI system, Random random) {
        String variantId = config.pickStationEntityType(random);
        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(factionId, "battlestation", null);
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
        fleet.getFleetData().addFleetMember(member);
        fleet.setName("Orbital Station");
        fleet.setStationMode(true);
        fleet.setAI(null);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
        fleet.getMemoryWithoutUpdate().set("$cfai_noJump", true);
        fleet.getMemoryWithoutUpdate().set("$th_excavation_station", true);
        return fleet;
    }

    private void configureStationInteraction(CampaignFleetAPI fleet) {
        // Note: "$fidConifgGen" is vanilla's spelling (typo in the Starsector API)
        fleet.getMemoryWithoutUpdate().set("$fidConifgGen",
                (FleetInteractionDialogPluginImpl.FIDConfigGen) () -> {
                    var fidConfig = new FleetInteractionDialogPluginImpl.FIDConfig();
                    fidConfig.leaveAlwaysAvailable = true;
                    fidConfig.showFleetAttitude = false;
                    fidConfig.showTransponderStatus = false;
                    fidConfig.impactsAllyReputation = false;
                    fidConfig.impactsEnemyReputation = false;
                    fidConfig.pullInAllies = false;
                    fidConfig.pullInEnemies = false;
                    fidConfig.pullInStations = false;
                    fidConfig.firstTimeEngageOptionText = "Attack the station";
                    fidConfig.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
                        @Override
                        public void battleContextCreated(
                                com.fs.starfarer.api.campaign.InteractionDialogAPI dialog,
                                BattleCreationContext bcc) {
                            bcc.aiRetreatAllowed = false;
                            bcc.objectivesAllowed = false;
                        }
                    };
                    return fidConfig;
                });
    }

    private void spawnDefendingFleet(THFactionConfig config, StarSystemAPI system, Random random) {
        defenderFleet = config.createDefaultFleet(
                system, null, null, random, factionId);
        if (defenderFleet == null) return;

        // Set max CR on all defending ships
        for (FleetMemberAPI ship : defenderFleet.getFleetData().getMembersListCopy()) {
            ship.getRepairTracker().setCR(ship.getRepairTracker().getMaxCR());
        }

        // Reduced rep penalty when player attacks (not auto-hostile)
        defenderFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
        defenderFleet.getMemoryWithoutUpdate().set("$cfai_noJump", true);

        // Tag for identification in rules.csv (comm link SP option targets the defender)
        defenderFleet.getMemoryWithoutUpdate().set("$th_excavation_defender", true);

        // Add to system and orbit the station passively
        system.addEntity(defenderFleet);
        defenderFleet.setLocation(stationFleet.getLocation().x, stationFleet.getLocation().y);
        defenderFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, stationFleet, Float.MAX_VALUE);
    }

    @Override
    public void advanceImpl(float amount) {
        if (Global.getSector().isPaused()) return;

        if (stationFleet == null) {
            endAfterDelay();
            return;
        }

        // Only the station needs to be destroyed — defenders may flee
        if (!stationCleared) {
            boolean stationDead = !stationFleet.isAlive() || stationFleet.getFleetData().getMembersListCopy().isEmpty();
            if (stationDead) {
                stationCleared = true;

                // Station cleared — ground operatives remain
                planet.getMemoryWithoutUpdate().unset("$th_excavation_blocked");
                planet.getMemoryWithoutUpdate().set("$th_excavation_ground_ops", true);

                sendUpdateIfPlayerHasIntel(this, false);
            }
        }

        if (stationCleared) {
            endAfterDelay();
        }
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color titleColor = getTitleColor(mode);
        info.addPara(getName(), titleColor, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(
            TooltipMakerAPI info,
            ListInfoMode mode,
            boolean isUpdate,
            Color tc,
            float initPad
    ) {
        Color h = Misc.getHighlightColor();

        if (mode == ListInfoMode.MESSAGES) {
            info.addPara("Scavengers excavating ruins", initPad);
            return;
        }

        if (isEnded() || isEnding()) {
            info.addPara("Excavation station cleared", initPad);
            return;
        }

        String factionName = Misc.ucFirst(Global.getSector().getFaction(factionId).getDisplayName());
        info.addPara("%s excavation operation", initPad, tc, h, factionName);
        info.addPara("Located in %s", 0f, tc, h, planet.getStarSystem().getNameWithTypeIfNebula());
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        String factionName = Misc.ucFirst(Global.getSector().getFaction(factionId).getDisplayName());

        if (!(isEnded() || isEnding())) {
            info.addPara(factionName + " scavengers have discovered an uncharted planet with ruins "
                    + "in the " + planet.getStarSystem().getNameWithTypeIfNebula()
                    + " and have set up a fortified station to excavate them.", 0f);

            info.addPara("Destroy the excavation station's garrison to clear the way, "
                    + "then raid the planet's ruins for valuable salvage and treasure hunt progress.", opad);

            info.addPara("The station orbits %s.", opad, h, planet.getFullName());
        } else {
            info.addPara("The " + factionName + " excavation station has been cleared. "
                    + "The planet's ruins are now open for salvage.", 0f);
        }
    }

    @Override
    public String getName() {
        return "Ruin Excavation - " + planet.getStarSystem().getNameWithTypeIfNebula();
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(THUtils.TH_TAG);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (stationFleet != null && stationFleet.isAlive()) return stationFleet;
        return planet;
    }

    public CampaignFleetAPI getStationFleet() {
        return stationFleet;
    }

    public CampaignFleetAPI getDefenderFleet() {
        return defenderFleet;
    }

    /** Called when the player spends a story point to convince the defender fleet to leave. */
    public void convinceDefenderToLeave() {
        if (defenderFleet == null || !defenderFleet.isAlive()) return;

        // Find the nearest faction market as a return destination
        SectorEntityToken destination = null;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.isHidden() || !factionId.equals(m.getFactionId())) continue;
            if (destination == null ||
                    Misc.getDistanceLY(defenderFleet.getLocationInHyperspace(),
                            m.getLocationInHyperspace())
                    < Misc.getDistanceLY(defenderFleet.getLocationInHyperspace(),
                            destination.getLocationInHyperspace())) {
                destination = m.getPrimaryEntity();
            }
        }

        // Remove flags that prevent the fleet from traveling
        defenderFleet.getMemoryWithoutUpdate().unset("$cfai_noJump");
        defenderFleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_LOW_REP_IMPACT);
        defenderFleet.clearAssignments();
        defenderFleet.setOrbit(null);
        if (destination != null) {
            defenderFleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                    destination, 1000f, "returning to " + destination.getName());
        } else {
            Misc.fadeAndExpire(defenderFleet);
        }
    }

    @Override
    public void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }
}
