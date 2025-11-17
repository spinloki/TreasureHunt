package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class THMockHullSpec implements ShipHullSpecAPI {
    private final float baseValue;

    public THMockHullSpec(float baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public float getBaseValue() {
        return baseValue;
    }

    @Override
    public int getOrdnancePoints(MutableCharacterStatsAPI stats) {
        return 0;
    }

    @Override
    public ShipAPI.HullSize getHullSize() {
        return null;
    }

    @Override
    public float getHitpoints() {
        return 0;
    }

    @Override
    public float getArmorRating() {
        return 0;
    }

    @Override
    public float getFluxCapacity() {
        return 0;
    }

    @Override
    public float getFluxDissipation() {
        return 0;
    }

    @Override
    public ShieldAPI.ShieldType getShieldType() {
        return null;
    }

    @Override
    public List<WeaponSlotAPI> getAllWeaponSlotsCopy() {
        return List.of();
    }

    @Override
    public String getSpriteName() {
        return "";
    }

    @Override
    public boolean isCompatibleWithBase() {
        return false;
    }

    @Override
    public String getBaseHullId() {
        return "";
    }

    @Override
    public float getBaseShieldFluxPerDamageAbsorbed() {
        return 0;
    }

    @Override
    public String getHullNameWithDashClass() {
        return "";
    }

    @Override
    public boolean hasHullName() {
        return false;
    }

    @Override
    public float getBreakProb() {
        return 0;
    }

    @Override
    public float getMinPieces() {
        return 0;
    }

    @Override
    public float getMaxPieces() {
        return 0;
    }

    @Override
    public int getFighterBays() {
        return 0;
    }

    @Override
    public float getMinCrew() {
        return 0;
    }

    @Override
    public float getMaxCrew() {
        return 0;
    }

    @Override
    public float getCargo() {
        return 0;
    }

    @Override
    public float getFuel() {
        return 0;
    }

    @Override
    public float getFuelPerLY() {
        return 0;
    }

    @Override
    public boolean isDHull() {
        return false;
    }

    @Override
    public boolean isDefaultDHull() {
        return false;
    }

    @Override
    public void setDParentHullId(String dParentHullId) {

    }

    @Override
    public String getDParentHullId() {
        return "";
    }

    @Override
    public ShipHullSpecAPI getDParentHull() {
        return null;
    }

    @Override
    public ShipHullSpecAPI getBaseHull() {
        return null;
    }

    @Override
    public List<String> getBuiltInWings() {
        return List.of();
    }

    @Override
    public boolean isBuiltInWing(int index) {
        return false;
    }

    @Override
    public String getDesignation() {
        return "";
    }

    @Override
    public boolean hasDesignation() {
        return false;
    }

    @Override
    public boolean isRestoreToBase() {
        return false;
    }

    @Override
    public void setRestoreToBase(boolean restoreToBase) {

    }

    @Override
    public Vector2f getModuleAnchor() {
        return null;
    }

    @Override
    public void setModuleAnchor(Vector2f moduleAnchor) {

    }

    @Override
    public void setCompatibleWithBase(boolean compatibleWithBase) {

    }

    @Override
    public Set<String> getTags() {
        return Set.of();
    }

    @Override
    public void addTag(String tag) {

    }

    @Override
    public boolean hasTag(String tag) {
        return false;
    }

    @Override
    public float getRarity() {
        return 0;
    }

    @Override
    public String getNameWithDesignationWithDashClass() {
        return "";
    }

    @Override
    public String getDescriptionId() {
        return "";
    }

    @Override
    public boolean isBaseHull() {
        return false;
    }

    @Override
    public void setManufacturer(String manufacturer) {

    }

    @Override
    public String getManufacturer() {
        return "";
    }

    @Override
    public int getFleetPoints() {
        return 0;
    }

    @Override
    public List<String> getBuiltInMods() {
        return List.of();
    }

    @Override
    public WeaponSlotAPI getWeaponSlotAPI(String slotId) {
        return null;
    }

    @Override
    public String getDescriptionPrefix() {
        return "";
    }

    @Override
    public boolean isBuiltInMod(String modId) {
        return false;
    }

    @Override
    public void addBuiltInMod(String modId) {

    }

    @Override
    public boolean isCivilianNonCarrier() {
        return false;
    }

    @Override
    public void setHullName(String hullName) {

    }

    @Override
    public void setDesignation(String designation) {

    }

    @Override
    public boolean isPhase() {
        return false;
    }

    @Override
    public String getShipFilePath() {
        return "";
    }

    @Override
    public String getTravelDriveId() {
        return "";
    }

    @Override
    public void setTravelDriveId(String travelDriveId) {

    }

    @Override
    public EngineSpecAPI getEngineSpec() {
        return null;
    }

    @Override
    public float getSuppliesToRecover() {
        return 0;
    }

    @Override
    public void setSuppliesToRecover(float suppliesToRecover) {

    }

    @Override
    public float getSuppliesPerMonth() {
        return 0;
    }

    @Override
    public void setSuppliesPerMonth(float suppliesPerMonth) {

    }

    @Override
    public void setRepairPercentPerDay(float repairPercentPerDay) {

    }

    @Override
    public void setCRToDeploy(float crToDeploy) {

    }

    @Override
    public float getNoCRLossSeconds() {
        return 0;
    }

    @Override
    public void setNoCRLossSeconds(float noCRLossSeconds) {

    }

    @Override
    public void setCRLossPerSecond(float crLossPerSecond) {

    }

    @Override
    public HashMap<String, String> getBuiltInWeapons() {
        return null;
    }

    @Override
    public boolean isBuiltIn(String slotId) {
        return false;
    }

    @Override
    public void addBuiltInWeapon(String slotId, String weaponId) {

    }

    @Override
    public String getShipDefenseId() {
        return "";
    }

    @Override
    public void setShipDefenseId(String shipDefenseId) {

    }

    @Override
    public String getShipSystemId() {
        return "";
    }

    @Override
    public void setShipSystemId(String shipSystemId) {

    }

    @Override
    public void setDescriptionPrefix(String descriptionPrefix) {

    }

    @Override
    public WeaponSlotAPI getWeaponSlot(String slotId) {
        return null;
    }

    @Override
    public void setFleetPoints(int fleetPoints) {

    }

    @Override
    public void setDescriptionId(String descriptionId) {

    }

    @Override
    public Color getHyperspaceJitterColor() {
        return null;
    }

    @Override
    public boolean isDHullOldMethod() {
        return false;
    }

    @Override
    public boolean isCarrier() {
        return false;
    }

    @Override
    public String getLogisticsNAReason() {
        return "";
    }

    @Override
    public void setLogisticsNAReason(String logisticsNAReason) {

    }

    @Override
    public float getCollisionRadius() {
        return 0;
    }

    @Override
    public String getCodexVariantId() {
        return "";
    }

    @Override
    public void setCodexVariantId(String codexVariantId) {

    }

    @Override
    public String getRestoredToHullId() {
        return "";
    }

    @Override
    public ShieldSpecAPI getShieldSpec() {
        return null;
    }

    @Override
    public ShieldAPI.ShieldType getDefenseType() {
        return null;
    }

    // ---- Stub everything else ----
    @Override public String getHullId() { return "mock_hull"; }

    @Override
    public String getHullName() {
        return "";
    }

    @Override
    public EnumSet<ShipTypeHints> getHints() {
        return null;
    }

    @Override
    public float getNoCRLossTime() {
        return 0;
    }

    @Override
    public float getCRToDeploy() {
        return 0;
    }

    @Override
    public float getCRLossPerSecond() {
        return 0;
    }

    @Override
    public float getCRLossPerSecond(MutableShipStatsAPI stats) {
        return 0;
    }

    @Override
    public ModSpecAPI getSourceMod() {
        return null;
    }
    // You can leave all other methods empty or return defaults.
}
