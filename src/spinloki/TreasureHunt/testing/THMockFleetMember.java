package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.*;
import org.lwjgl.util.vector.Vector2f;

public class THMockFleetMember implements FleetMemberAPI {
    private final ShipHullSpecAPI spec;

    public THMockFleetMember(float baseValue) {
        spec = new THMockHullSpec(baseValue);
    }

    @Override
    public PersonAPI getCaptain() {
        return null;
    }

    @Override
    public MutableShipStatsAPI getStats() {
        return null;
    }

    @Override
    public String getShipName() {
        return "";
    }

    @Override
    public void setShipName(String name) {

    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getSpecId() {
        return "";
    }

    @Override
    public String getHullId() {
        return "";
    }

    @Override
    public FleetMemberType getType() {
        return null;
    }

    @Override
    public boolean isFlagship() {
        return false;
    }

    @Override
    public int getNumFlightDecks() {
        return 0;
    }

    @Override
    public boolean isCarrier() {
        return false;
    }

    @Override
    public boolean isCivilian() {
        return false;
    }

    @Override
    public void setFlagship(boolean isFlagship) {

    }

    @Override
    public int getFleetPointCost() {
        return 0;
    }

    @Override
    public boolean isFighterWing() {
        return false;
    }

    @Override
    public boolean isFrigate() {
        return false;
    }

    @Override
    public boolean isDestroyer() {
        return false;
    }

    @Override
    public boolean isCruiser() {
        return false;
    }

    @Override
    public boolean isCapital() {
        return false;
    }

    @Override
    public int getNumFightersInWing() {
        return 0;
    }

    @Override
    public float getFuelCapacity() {
        return 0;
    }

    @Override
    public float getCargoCapacity() {
        return 0;
    }

    @Override
    public float getMinCrew() {
        return 0;
    }

    @Override
    public float getNeededCrew() {
        return 0;
    }

    @Override
    public float getMaxCrew() {
        return 0;
    }

    @Override
    public float getFuelUse() {
        return 0;
    }

    @Override
    public RepairTrackerAPI getRepairTracker() {
        return null;
    }

    @Override
    public ShipHullSpecAPI getHullSpec() {
        return spec;
    }

    @Override
    public PersonAPI getFleetCommander() {
        return null;
    }

    @Override
    public boolean canBeDeployedForCombat() {
        return false;
    }

    @Override
    public ShipVariantAPI getVariant() {
        return null;
    }

    @Override
    public FleetDataAPI getFleetData() {
        return null;
    }

    @Override
    public void setVariant(ShipVariantAPI variant, boolean withRefit, boolean withStatsUpdate) {

    }

    @Override
    public CrewCompositionAPI getCrewComposition() {
        return null;
    }

    @Override
    public FleetMemberStatusAPI getStatus() {
        return null;
    }

    @Override
    public float getCrewFraction() {
        return 0;
    }

    @Override
    public int getReplacementChassisCount() {
        return 0;
    }

    @Override
    public void setStatUpdateNeeded(boolean statUpdateNeeded) {

    }

    @Override
    public BuffManagerAPI getBuffManager() {
        return null;
    }

    @Override
    public boolean isMothballed() {
        return false;
    }

    @Override
    public float getDeployCost() {
        return 0;
    }

    @Override
    public void setCaptain(PersonAPI commander) {

    }

    @Override
    public float getMemberStrength() {
        return 0;
    }

    @Override
    public int getOwner() {
        return 0;
    }

    @Override
    public void setOwner(int owner) {

    }

    @Override
    public float getBaseSellValue() {
        return 0;
    }

    @Override
    public float getBaseBuyValue() {
        return 0;
    }

    @Override
    public boolean needsRepairs() {
        return false;
    }

    @Override
    public boolean canBeRepaired() {
        return false;
    }

    @Override
    public float getDeploymentPointsCost() {
        return 0;
    }

    @Override
    public float getDeploymentCostSupplies() {
        return 0;
    }

    @Override
    public float getBaseDeployCost() {
        return 0;
    }

    @Override
    public boolean isAlly() {
        return false;
    }

    @Override
    public void setAlly(boolean isAlly) {

    }

    @Override
    public void setFleetCommanderForStats(PersonAPI alternateFleetCommander, FleetDataAPI fleetForStats) {

    }

    @Override
    public FleetDataAPI getFleetDataForStats() {
        return null;
    }

    @Override
    public PersonAPI getFleetCommanderForStats() {
        return null;
    }

    @Override
    public void updateStats() {

    }

    @Override
    public boolean isStation() {
        return false;
    }

    @Override
    public float getBaseDeploymentCostSupplies() {
        return 0;
    }

    @Override
    public float getBaseValue() {
        return 0;
    }

    @Override
    public void setSpriteOverride(String spriteOverride) {

    }

    @Override
    public String getSpriteOverride() {
        return "";
    }

    @Override
    public Vector2f getOverrideSpriteSize() {
        return null;
    }

    @Override
    public void setOverrideSpriteSize(Vector2f overrideSpriteSize) {

    }

    @Override
    public boolean isPhaseShip() {
        return false;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public float getUnmodifiedDeploymentPointsCost() {
        return 0;
    }

    @Override
    public void setFlagship(boolean isFlagship, boolean withCaptainSet) {

    }

    @Override
    public String getPersonalityOverride() {
        return "";
    }

    @Override
    public void setPersonalityOverride(String personalityOverride) {

    }

    @Override
    public ModSpecAPI getSourceMod() {
        return null;
    }

    // stub rest
}
