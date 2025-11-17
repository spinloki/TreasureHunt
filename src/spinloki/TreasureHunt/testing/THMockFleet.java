package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.InteractionDialogImageVisual;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.FleetLogisticsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class THMockFleet implements CampaignFleetAPI {
    private THMockBattle battle;
    private final THMockFleetData fleetData;
    private final MemoryAPI memory = new THMockMemory();

    public THMockFleet(THMockBattle battle, boolean isScavenger, THMockFleetData fleetData) {
        this.battle = battle;
        this.fleetData = fleetData;
        if (isScavenger){
            this.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SCAVENGER, true);
        }
    }

    @Override
    public boolean isInCurrentLocation() {
        return false;
    }

    @Override
    public boolean isInHyperspace() {
        return false;
    }

    @Override
    public void addScript(EveryFrameScript script) {

    }

    @Override
    public void removeScript(EveryFrameScript script) {

    }

    @Override
    public void removeScriptsOfClass(Class c) {

    }

    @Override
    public boolean isInOrNearSystem(StarSystemAPI system) {
        return false;
    }

    @Override
    public void setLocation(float x, float y) {

    }

    @Override
    public void autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(SectorEntityToken entity, float radius) {

    }

    @Override
    public void forceSensorContactFaderBrightness(float b) {

    }

    @Override
    public void forceSensorFaderOut() {

    }

    @Override
    public void setLightSource(SectorEntityToken star, Color color) {

    }

    @Override
    public List<EveryFrameScript> getScripts() {
        return List.of();
    }

    @Override
    public float getExtendedDetectedAtRange() {
        return 0;
    }

    @Override
    public void setExtendedDetectedAtRange(Float extendedDetectedAtRange) {

    }

    @Override
    public void despawn() {

    }

    @Override
    public void despawn(CampaignEventListener.FleetDespawnReason reason, Object param) {

    }

    @Override
    public boolean isFleet() {
        return false;
    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays) {

    }

    @Override
    public boolean isCurrentAssignment(FleetAssignment assignment) {
        return false;
    }

    @Override
    public void removeFirstAssignmentIfItIs(FleetAssignment assignment) {

    }

    @Override
    public void removeFirstAssignment() {

    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion) {

    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText) {

    }

    @Override
    public void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion) {

    }

    @Override
    public void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion) {

    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion) {

    }

    @Override
    public void clearAssignments() {

    }

    @Override
    public void setPreferredResupplyLocation(SectorEntityToken token) {

    }

    @Override
    public Vector2f getVelocity() {
        return null;
    }

    @Override
    public void setInteractionImage(String category, String key) {

    }

    @Override
    public Vector2f getLocation() {
        return null;
    }

    @Override
    public Vector2f getLocationInHyperspace() {
        return null;
    }

    @Override
    public OrbitAPI getOrbit() {
        return null;
    }

    @Override
    public void setOrbit(OrbitAPI orbit) {

    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public FleetAssignmentDataAPI getCurrentAssignment() {
        return null;
    }

    @Override
    public List<FleetAssignmentDataAPI> getAssignmentsCopy() {
        return List.of();
    }

    @Override
    public FleetLogisticsAPI getLogistics() {
        return null;
    }

    @Override
    public LocationAPI getContainingLocation() {
        return null;
    }

    @Override
    public float getRadius() {
        return 0;
    }

    @Override
    public FactionAPI getFaction() {
        return null;
    }

    @Override
    public String getCustomDescriptionId() {
        return "";
    }

    @Override
    public void setCustomDescriptionId(String customDescriptionId) {

    }

    @Override
    public void setCustomInteractionDialogImageVisual(InteractionDialogImageVisual visual) {

    }

    @Override
    public InteractionDialogImageVisual getCustomInteractionDialogImageVisual() {
        return null;
    }

    @Override
    public void setFreeTransfer(boolean freeTransfer) {

    }

    @Override
    public boolean isFreeTransfer() {
        return false;
    }

    @Override
    public boolean hasTag(String tag) {
        return false;
    }

    @Override
    public void addTag(String tag) {

    }

    @Override
    public void removeTag(String tag) {

    }

    @Override
    public Collection<String> getTags() {
        return List.of();
    }

    @Override
    public void clearTags() {

    }

    @Override
    public void setFixedLocation(float x, float y) {

    }

    @Override
    public void setCircularOrbit(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays) {

    }

    @Override
    public void setCircularOrbitPointingDown(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays) {

    }

    @Override
    public void setCircularOrbitWithSpin(SectorEntityToken focus, float angle, float orbitRadius, float orbitDays, float minSpin, float maxSpin) {

    }

    @Override
    public MemoryAPI getMemory() {
        return memory;
    }

    @Override
    public MemoryAPI getMemoryWithoutUpdate() {
        return memory;
    }

    @Override
    public float getFacing() {
        return 0;
    }

    @Override
    public void setFacing(float facing) {

    }

    @Override
    public PersonAPI getCommander() {
        return null;
    }

    @Override
    public MutableCharacterStatsAPI getCommanderStats() {
        return null;
    }

    @Override
    public FleetMemberAPI getFlagship() {
        return null;
    }

    @Override
    public boolean isPlayerFleet() {
        return false;
    }

    @Override
    public MarketAPI getMarket() {
        return null;
    }

    @Override
    public void setMarket(MarketAPI market) {

    }

    @Override
    public CargoAPI getCargo() {
        return null;
    }

    @Override
    public FleetDataAPI getFleetData() {
        return fleetData;
    }

    @Override
    public void removeFleetMemberWithDestructionFlash(FleetMemberAPI member) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public PersonAPI getActivePerson() {
        return null;
    }

    @Override
    public void setActivePerson(PersonAPI activePerson) {

    }

    @Override
    public boolean isVisibleToSensorsOf(SectorEntityToken other) {
        return false;
    }

    @Override
    public boolean isVisibleToPlayerFleet() {
        return false;
    }

    @Override
    public VisibilityLevel getVisibilityLevelToPlayerFleet() {
        return null;
    }

    @Override
    public VisibilityLevel getVisibilityLevelTo(SectorEntityToken other) {
        return null;
    }

    @Override
    public void addAbility(String id) {

    }

    @Override
    public void removeAbility(String id) {

    }

    @Override
    public AbilityPlugin getAbility(String id) {
        return null;
    }

    @Override
    public boolean hasAbility(String id) {
        return false;
    }

    @Override
    public Map<String, AbilityPlugin> getAbilities() {
        return Map.of();
    }

    @Override
    public boolean isTransponderOn() {
        return false;
    }

    @Override
    public void setTransponderOn(boolean transponderOn) {

    }

    @Override
    public void addFloatingText(String text, Color color, float duration) {

    }

    @Override
    public SectorEntityToken getLightSource() {
        return null;
    }

    @Override
    public Color getLightColor() {
        return null;
    }

    @Override
    public void setMemory(MemoryAPI memory) {

    }

    @Override
    public Map<String, Object> getCustomData() {
        return Map.of();
    }

    @Override
    public Color getIndicatorColor() {
        return null;
    }

    @Override
    public CustomCampaignEntityPlugin getCustomPlugin() {
        return null;
    }

    @Override
    public float getCircularOrbitRadius() {
        return 0;
    }

    @Override
    public float getCircularOrbitPeriod() {
        return 0;
    }

    @Override
    public SectorEntityToken getOrbitFocus() {
        return null;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public String getAutogenJumpPointNameInHyper() {
        return "";
    }

    @Override
    public void setAutogenJumpPointNameInHyper(String autogenJumpPointNameInHyper) {

    }

    @Override
    public boolean isSkipForJumpPointAutoGen() {
        return false;
    }

    @Override
    public void setSkipForJumpPointAutoGen(boolean skipForJumpPointAutoGen) {

    }

    @Override
    public float getCircularOrbitAngle() {
        return 0;
    }

    @Override
    public String getCustomEntityType() {
        return "";
    }

    @Override
    public float getSensorStrength() {
        return 0;
    }

    @Override
    public void setSensorStrength(Float sensorStrength) {

    }

    @Override
    public float getSensorProfile() {
        return 0;
    }

    @Override
    public void setSensorProfile(Float sensorProfile) {

    }

    @Override
    public StatBonus getDetectedRangeMod() {
        return null;
    }

    @Override
    public StatBonus getSensorRangeMod() {
        return null;
    }

    @Override
    public float getTotalSupplyCostPerDay() {
        return 0;
    }

    @Override
    public int getNumCapitals() {
        return 0;
    }

    @Override
    public int getNumCruisers() {
        return 0;
    }

    @Override
    public int getNumDestroyers() {
        return 0;
    }

    @Override
    public int getNumFrigates() {
        return 0;
    }

    @Override
    public int getNumFighters() {
        return 0;
    }

    @Override
    public void updateCounts() {

    }

    @Override
    public float getTravelSpeed() {
        return 0;
    }

    @Override
    public CampaignFleetAIAPI getAI() {
        return null;
    }

    @Override
    public int getFleetPoints() {
        return 0;
    }

    @Override
    public String getNameWithFaction() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getFullName() {
        return "";
    }

    @Override
    public void setFaction(String factionId) {

    }

    @Override
    public boolean isValidPlayerFleet() {
        return false;
    }

    @Override
    public void setNoEngaging(float seconds) {

    }

    @Override
    public MutableFleetStatsAPI getStats() {
        return null;
    }

    @Override
    public void setMoveDestination(float x, float y) {

    }

    @Override
    public void setMoveDestinationOverride(float x, float y) {

    }

    @Override
    public SectorEntityToken getInteractionTarget() {
        return null;
    }

    @Override
    public void setInteractionTarget(SectorEntityToken target) {

    }

    @Override
    public boolean isInHyperspaceTransition() {
        return false;
    }

    @Override
    public void setAIMode(boolean aiMode) {

    }

    @Override
    public boolean isAIMode() {
        return false;
    }

    @Override
    public int getFleetSizeCount() {
        return 0;
    }

    @Override
    public void setNoFactionInName(boolean noFactionInName) {

    }

    @Override
    public boolean isNoFactionInName() {
        return false;
    }

    @Override
    public void setCommander(PersonAPI commander) {

    }

    @Override
    public void forceSync() {

    }

    @Override
    public boolean knowsWhoPlayerIs() {
        return false;
    }

    @Override
    public boolean isHostileTo(SectorEntityToken other) {
        return false;
    }

    @Override
    public List<FleetMemberViewAPI> getViews() {
        return List.of();
    }

    @Override
    public FleetMemberViewAPI getViewForMember(FleetMemberAPI member) {
        return null;
    }

    @Override
    public float getCurrBurnLevel() {
        return 0;
    }

    @Override
    public void setVelocity(float x, float y) {

    }

    @Override
    public float getAcceleration() {
        return 0;
    }

    @Override
    public void setFaction(String factionId, boolean includeCaptains) {

    }

    @Override
    public BattleAPI getBattle() {
        return battle;
    }

    @Override
    public void setBattle(BattleAPI battle) {
        this.battle = (THMockBattle) battle;
    }

    @Override
    public void setAI(CampaignFleetAIAPI campaignFleetAI) {

    }

    @Override
    public String getNameWithFactionKeepCase() {
        return "";
    }

    @Override
    public boolean isFriendlyTo(SectorEntityToken other) {
        return false;
    }

    @Override
    public float getBaseSensorRangeToDetect(float sensorProfile) {
        return 0;
    }

    @Override
    public boolean hasSensorStrength() {
        return false;
    }

    @Override
    public boolean hasSensorProfile() {
        return false;
    }

    @Override
    public float getMaxSensorRangeToDetect(SectorEntityToken other) {
        return 0;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public void setDiscoverable(Boolean discoverable) {

    }

    @Override
    public CustomEntitySpecAPI getCustomEntitySpec() {
        return null;
    }

    @Override
    public List<SalvageEntityGenDataSpec.DropData> getDropValue() {
        return List.of();
    }

    @Override
    public List<SalvageEntityGenDataSpec.DropData> getDropRandom() {
        return List.of();
    }

    @Override
    public void addDropValue(String group, int value) {

    }

    @Override
    public void addDropRandom(String group, int chances) {

    }

    @Override
    public void addDropRandom(String group, int chances, int value) {

    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public void setExpired(boolean expired) {

    }

    @Override
    public float getSensorFaderBrightness() {
        return 0;
    }

    @Override
    public float getSensorContactFaderBrightness() {
        return 0;
    }

    @Override
    public void forceSensorFaderBrightness(float b) {

    }

    @Override
    public Float getDiscoveryXP() {
        return 0f;
    }

    @Override
    public void setDiscoveryXP(Float discoveryXP) {

    }

    @Override
    public boolean hasDiscoveryXP() {
        return false;
    }

    @Override
    public void addDropValue(SalvageEntityGenDataSpec.DropData data) {

    }

    @Override
    public void addDropRandom(SalvageEntityGenDataSpec.DropData data) {

    }

    @Override
    public void setAlwaysUseSensorFaderBrightness(Boolean alwaysUseSensorFaderBrightness) {

    }

    @Override
    public Boolean getAlwaysUseSensorFaderBrightness() {
        return null;
    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean hasScriptOfClass(Class c) {
        return false;
    }

    @Override
    public void setContainingLocation(LocationAPI location) {

    }

    @Override
    public void clearAbilities() {

    }

    @Override
    public Constellation getConstellation() {
        return null;
    }

    @Override
    public boolean isStar() {
        return false;
    }

    @Override
    public Float getSalvageXP() {
        return 0f;
    }

    @Override
    public void setSalvageXP(Float salvageXP) {

    }

    @Override
    public boolean hasSalvageXP() {
        return false;
    }

    @Override
    public void setDetectionRangeDetailsOverrideMult(Float detectionRangeDetailsOverrideMult) {

    }

    @Override
    public Float getDetectionRangeDetailsOverrideMult() {
        return 0f;
    }

    @Override
    public VisibilityLevel getVisibilityLevelOfPlayerFleet() {
        return null;
    }

    @Override
    public void setCircularOrbitAngle(float angle) {

    }

    @Override
    public void addFloatingText(String text, Color color, float duration, boolean showWhenOnlySensorContact) {

    }

    @Override
    public boolean isSystemCenter() {
        return false;
    }

    @Override
    public StarSystemAPI getStarSystem() {
        return null;
    }

    @Override
    public void clearFloatingText() {

    }

    @Override
    public Boolean isDoNotAdvanceAI() {
        return null;
    }

    @Override
    public void setDoNotAdvanceAI(Boolean doNotAdvanceAI) {

    }

    @Override
    public List<FleetMemberAPI> getMembersWithFightersCopy() {
        return List.of();
    }

    @Override
    public void setNullAIActionText(String nullAIActionText) {

    }

    @Override
    public String getNullAIActionText() {
        return "";
    }

    @Override
    public void setStationMode(Boolean stationMode) {

    }

    @Override
    public boolean isStationMode() {
        return false;
    }

    @Override
    public Boolean wasMousedOverByPlayer() {
        return null;
    }

    @Override
    public void setWasMousedOverByPlayer(Boolean wasMousedOverByPlayer) {

    }

    @Override
    public boolean isDespawning() {
        return false;
    }

    @Override
    public Vector2f getMoveDestination() {
        return null;
    }

    @Override
    public List<FleetEventListener> getEventListeners() {
        return List.of();
    }

    @Override
    public void addEventListener(FleetEventListener listener) {

    }

    @Override
    public void removeEventListener(FleetEventListener listener) {

    }

    @Override
    public FleetInflater getInflater() {
        return null;
    }

    @Override
    public void setInflater(FleetInflater inflater) {

    }

    @Override
    public void inflateIfNeeded() {

    }

    @Override
    public void deflate() {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Boolean getForceNoSensorProfileUpdate() {
        return null;
    }

    @Override
    public void setForceNoSensorProfileUpdate(Boolean forceNoSensorProfileUpdate) {

    }

    @Override
    public boolean isInflated() {
        return false;
    }

    @Override
    public void setInflated(Boolean inflated) {

    }

    @Override
    public Boolean isNoAutoDespawn() {
        return null;
    }

    @Override
    public void setNoAutoDespawn(Boolean noAutoDespawn) {

    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, boolean addTimeToNext, Script onStart, Script onCompletion) {

    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(Boolean hidden) {

    }

    @Override
    public Boolean getAbortDespawn() {
        return null;
    }

    @Override
    public void setAbortDespawn(Boolean abortDespawn) {

    }

    @Override
    public float getEffectiveStrength() {
        return 0;
    }

    @Override
    public int getNumMembersFast() {
        return 0;
    }

    @Override
    public void goSlowOneFrame(boolean stop) {

    }

    @Override
    public boolean wasSlowMoving() {
        return false;
    }

    @Override
    public int getNumShips() {
        return 0;
    }

    @Override
    public void updateFleetView() {

    }

    @Override
    public boolean hasShipsWithUniqueSig() {
        return false;
    }

    @Override
    public boolean getGoSlowStop() {
        return false;
    }

    @Override
    public void goSlowOneFrame() {

    }

    @Override
    public boolean getGoSlowOneFrame() {
        return false;
    }

    @Override
    public Vector2f getVelocityFromMovementModule() {
        return null;
    }

    @Override
    public void fadeOutIndicator() {

    }

    @Override
    public void fadeInIndicator() {

    }

    @Override
    public void forceOutIndicator() {

    }

    @Override
    public void setOrbitFocus(SectorEntityToken focus) {

    }
}
