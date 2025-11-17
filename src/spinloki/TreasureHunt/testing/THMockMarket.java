package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableStatWithTempMods;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.fleet.MutableMarketStatsAPI;
import com.fs.starfarer.api.fleet.ShipFilter;
import com.fs.starfarer.api.fleet.ShipRolePick;
import com.fs.starfarer.api.impl.campaign.econ.impl.ConstructionQueue;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class THMockMarket implements MarketAPI {
    private final String condition;
    private final THMockMemory memory = new THMockMemory();

    @Override
    public boolean hasCondition(String id) {
        return condition.equals(id);
    }

    public THMockMarket(String condition) {
        this.condition = condition;
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
    public SectorEntityToken getPrimaryEntity() {
        return null;
    }

    @Override
    public Set<SectorEntityToken> getConnectedEntities() {
        return Set.of();
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void setSize(int size) {

    }

    @Override
    public List<CommodityOnMarketAPI> getAllCommodities() {
        return List.of();
    }

    @Override
    public CommodityOnMarketAPI getCommodityData(String commodityId) {
        return null;
    }

    @Override
    public List<CommodityOnMarketAPI> getCommoditiesWithTag(String tag) {
        return List.of();
    }

    @Override
    public List<CommodityOnMarketAPI> getCommoditiesWithTags(String... tags) {
        return List.of();
    }

    @Override
    public MarketDemandAPI getDemand(String demandClass) {
        return null;
    }

    @Override
    public List<MarketDemandAPI> getDemandWithTag(String tag) {
        return List.of();
    }

    @Override
    public List<MarketConditionAPI> getConditions() {
        return List.of();
    }

    @Override
    public String addCondition(String id) {
        return "";
    }

    @Override
    public String addCondition(String id, Object param) {
        return "";
    }

    @Override
    public void removeCondition(String id) {

    }

    @Override
    public void removeSpecificCondition(String token) {

    }

    @Override
    public boolean hasSpecificCondition(String token) {
        return false;
    }

    @Override
    public void reapplyConditions() {

    }

    @Override
    public void reapplyCondition(String token) {

    }

    @Override
    public MarketDemandDataAPI getDemandData() {
        return null;
    }

    @Override
    public MutableStat getTariff() {
        return null;
    }

    @Override
    public StatBonus getDemandPriceMod() {
        return null;
    }

    @Override
    public StatBonus getSupplyPriceMod() {
        return null;
    }

    @Override
    public float getSupplyPrice(String commodityId, double quantity, boolean isPlayerPrice) {
        return 0;
    }

    @Override
    public float getDemandPrice(String commodityId, double quantity, boolean isPlayerPrice) {
        return 0;
    }

    @Override
    public float getDemandPriceAssumingExistingTransaction(String commodityId, double quantity, double existingTransactionUtility, boolean isPlayerPrice) {
        return 0;
    }

    @Override
    public float getSupplyPriceAssumingExistingTransaction(String commodityId, double quantity, double existingTransactionUtility, boolean isPlayerPrice) {
        return 0;
    }

    @Override
    public boolean isIllegal(String commodityId) {
        return false;
    }

    @Override
    public boolean isIllegal(CommodityOnMarketAPI com) {
        return false;
    }

    @Override
    public MutableStatWithTempMods getStability() {
        return null;
    }

    @Override
    public float getStabilityValue() {
        return 0;
    }

    @Override
    public FactionAPI getFaction() {
        return null;
    }

    @Override
    public String getFactionId() {
        return "";
    }

    @Override
    public void addSubmarket(String specId) {

    }

    @Override
    public boolean hasSubmarket(String specId) {
        return false;
    }

    @Override
    public List<SubmarketAPI> getSubmarketsCopy() {
        return List.of();
    }

    @Override
    public void removeSubmarket(String specId) {

    }

    @Override
    public SubmarketAPI getSubmarket(String specId) {
        return null;
    }

    @Override
    public void setFactionId(String factionId) {

    }

    @Override
    public void updatePriceMult() {

    }

    @Override
    public float pickShipAndAddToFleet(String role, FactionAPI.ShipPickParams params, CampaignFleetAPI fleet) {
        return 0;
    }

    @Override
    public float pickShipAndAddToFleet(String role, String factionId, FactionAPI.ShipPickParams params, CampaignFleetAPI fleet) {
        return 0;
    }

    @Override
    public float getShipQualityFactor() {
        return 0;
    }

    @Override
    public StarSystemAPI getStarSystem() {
        return null;
    }

    @Override
    public LocationAPI getContainingLocation() {
        return null;
    }

    @Override
    public Vector2f getLocationInHyperspace() {
        return null;
    }

    @Override
    public void setPrimaryEntity(SectorEntityToken primaryEntity) {

    }

    @Override
    public CommDirectoryAPI getCommDirectory() {
        return null;
    }

    @Override
    public void addPerson(PersonAPI person) {

    }

    @Override
    public void removePerson(PersonAPI person) {

    }

    @Override
    public List<PersonAPI> getPeopleCopy() {
        return List.of();
    }

    @Override
    public MutableMarketStatsAPI getStats() {
        return null;
    }

    @Override
    public List<ShipRolePick> pickShipsForRole(String role, FactionAPI.ShipPickParams params, Random random, ShipFilter filter) {
        return List.of();
    }

    @Override
    public List<ShipRolePick> pickShipsForRole(String role, String factionId, FactionAPI.ShipPickParams params, Random random, ShipFilter filter) {
        return List.of();
    }

    @Override
    public boolean isPlanetConditionMarketOnly() {
        return false;
    }

    @Override
    public void setPlanetConditionMarketOnly(boolean isPlanetConditionMarketOnly) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public MutableStat getHazard() {
        return null;
    }

    @Override
    public float getHazardValue() {
        return 0;
    }

    @Override
    public PlanetAPI getPlanetEntity() {
        return null;
    }

    @Override
    public SurveyLevel getSurveyLevel() {
        return null;
    }

    @Override
    public void setSurveyLevel(SurveyLevel surveyLevel) {

    }

    @Override
    public void advance(float amount) {

    }

    @Override
    public boolean isForceNoConvertOnSave() {
        return false;
    }

    @Override
    public void setForceNoConvertOnSave(boolean forceNoConvertOnSave) {

    }

    @Override
    public void updatePrices() {

    }

    @Override
    public MarketConditionAPI getSpecificCondition(String token) {
        return null;
    }

    @Override
    public MarketConditionAPI getFirstCondition(String id) {
        return null;
    }

    @Override
    public boolean isInEconomy() {
        return false;
    }

    @Override
    public List<Industry> getIndustries() {
        return List.of();
    }

    @Override
    public void addIndustry(String id) {

    }

    @Override
    public void removeIndustry(String id, MarketInteractionMode mode, boolean forUpgrade) {

    }

    @Override
    public void reapplyIndustries() {

    }

    @Override
    public Vector2f getLocation() {
        return null;
    }

    @Override
    public Industry getIndustry(String id) {
        return null;
    }

    @Override
    public boolean hasIndustry(String id) {
        return false;
    }

    @Override
    public List<CommodityOnMarketAPI> getCommoditiesCopy() {
        return List.of();
    }

    @Override
    public MarketConditionAPI getCondition(String id) {
        return null;
    }

    @Override
    public float getIndustryUpkeep() {
        return 0;
    }

    @Override
    public float getIndustryIncome() {
        return 0;
    }

    @Override
    public boolean hasWaystation() {
        return false;
    }

    @Override
    public Industry instantiateIndustry(String id) {
        return null;
    }

    @Override
    public MarketAPI clone() {
        return null;
    }

    @Override
    public void clearCommodities() {

    }

    @Override
    public boolean isPlayerOwned() {
        return false;
    }

    @Override
    public void setPlayerOwned(boolean playerOwned) {

    }

    @Override
    public float getPrevStability() {
        return 0;
    }

    @Override
    public float getExportIncome(boolean withOverhead) {
        return 0;
    }

    @Override
    public float getNetIncome() {
        return 0;
    }

    @Override
    public MutableStat getIncomeMult() {
        return null;
    }

    @Override
    public MutableStat getUpkeepMult() {
        return null;
    }

    @Override
    public PopulationComposition getPopulation() {
        return null;
    }

    @Override
    public PopulationComposition getIncoming() {
        return null;
    }

    @Override
    public void setPopulation(PopulationComposition population) {

    }

    @Override
    public void setIncoming(PopulationComposition incoming) {

    }

    @Override
    public LinkedHashSet<MarketImmigrationModifier> getImmigrationModifiers() {
        return null;
    }

    @Override
    public LinkedHashSet<MarketImmigrationModifier> getTransientImmigrationModifiers() {
        return null;
    }

    @Override
    public void addImmigrationModifier(MarketImmigrationModifier mod) {

    }

    @Override
    public void removeImmigrationModifier(MarketImmigrationModifier mod) {

    }

    @Override
    public void addTransientImmigrationModifier(MarketImmigrationModifier mod) {

    }

    @Override
    public void removeTransientImmigrationModifier(MarketImmigrationModifier mod) {

    }

    @Override
    public List<MarketImmigrationModifier> getAllImmigrationModifiers() {
        return List.of();
    }

    @Override
    public float getIncentiveCredits() {
        return 0;
    }

    @Override
    public void setIncentiveCredits(float incentiveCredits) {

    }

    @Override
    public boolean isImmigrationIncentivesOn() {
        return false;
    }

    @Override
    public void setImmigrationIncentivesOn(Boolean incentivesOn) {

    }

    @Override
    public boolean isFreePort() {
        return false;
    }

    @Override
    public void setFreePort(boolean freePort) {

    }

    @Override
    public boolean isImmigrationClosed() {
        return false;
    }

    @Override
    public void setImmigrationClosed(boolean closed) {

    }

    @Override
    public boolean wasIncomingSetBefore() {
        return false;
    }

    @Override
    public void addCondition(MarketConditionAPI mc) {

    }

    @Override
    public PersonAPI getAdmin() {
        return null;
    }

    @Override
    public void setAdmin(PersonAPI admin) {

    }

    @Override
    public float getDaysInExistence() {
        return 0;
    }

    @Override
    public void setDaysInExistence(float daysInExistence) {

    }

    @Override
    public StatBonus getAccessibilityMod() {
        return null;
    }

    @Override
    public boolean hasSpaceport() {
        return false;
    }

    @Override
    public void setHasSpaceport(boolean hasSpaceport) {

    }

    @Override
    public void setHasWaystation(boolean hasWaystation) {

    }

    @Override
    public String getEconGroup() {
        return "";
    }

    @Override
    public void setEconGroup(String econGroup) {

    }

    @Override
    public void addIndustry(String id, List<String> params) {

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
    public String getOnOrAt() {
        return "";
    }

    @Override
    public Color getTextColorForFactionOrPlanet() {
        return null;
    }

    @Override
    public Color getDarkColorForFactionOrPlanet() {
        return null;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public void setHidden(Boolean hidden) {

    }

    @Override
    public boolean isUseStockpilesForShortages() {
        return false;
    }

    @Override
    public void setUseStockpilesForShortages(boolean useStockpilesForShortages) {

    }

    @Override
    public float getShortageCounteringCost() {
        return 0;
    }

    @Override
    public void addSubmarket(SubmarketAPI submarket) {

    }

    @Override
    public ConstructionQueue getConstructionQueue() {
        return null;
    }

    @Override
    public boolean isInHyperspace() {
        return false;
    }

    @Override
    public LinkedHashSet<String> getSuppressedConditions() {
        return null;
    }

    @Override
    public boolean isConditionSuppressed(String id) {
        return false;
    }

    @Override
    public void suppressCondition(String id) {

    }

    @Override
    public void unsuppressCondition(String id) {

    }

    @Override
    public float getImmigrationIncentivesCost() {
        return 0;
    }

    @Override
    public boolean isInvalidMissionTarget() {
        return false;
    }

    @Override
    public void setInvalidMissionTarget(Boolean invalidMissionTarget) {

    }

    @Override
    public void setSuppressedConditions(LinkedHashSet<String> suppressedConditions) {

    }

    @Override
    public void setRetainSuppressedConditionsSetWhenEmpty(Boolean retainSuppressedConditionsSetWhenEmpty) {

    }

    @Override
    public float getGrossIncome() {
        return 0;
    }

    @Override
    public boolean hasFunctionalIndustry(String id) {
        return false;
    }

    @Override
    public void setCachedFaction(FactionAPI faction) {

    }
}
