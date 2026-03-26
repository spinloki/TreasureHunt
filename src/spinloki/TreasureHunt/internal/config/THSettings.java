package spinloki.TreasureHunt.internal.config;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Holds tuning knobs loaded from treasurehunt_settings.json.
 * Reward/item/blueprint data has moved to {@link spinloki.TreasureHunt.internal.registry.THRewardRegistry}.
 * Access via {@code THRegistry.getSettings()}.
 */
public class THSettings {
    private static final Logger log = Global.getLogger(THSettings.class);

    private float treasureHuntPackageSmodPercentBonus = 100f;
    private float treasureHuntPackageMaxMult = 3f;
    private boolean debugUseTimeFactor = false;
    private int debugTimeFactorPoints = 50;
    private JSONObject explorationValues;
    private int colonyRuinsBaseProgressDivisor;
    private int colonyTechMiningProgressMultiplier;
    private int sectorSprintReward;
    private float pickBlueprintWeight;
    private float abandonCarryoverFactor;
    private boolean raidDiminishingReturnsEnabled;
    private float raidDiminishingReturnsFactor;
    private int raidDiminishingReturnsRecoveryDays;
    private int numLeadCandidates;
    private boolean scavengerSwarmHasslingEnabled;
    private float sectorSprintMeddlingMult;
    private int scavengerDataCreditsPerPoint = 2000;
    private float scavengerMinFp = 50f;
    private float scavengerMaxFp = 150f;

    public THSettings() {}

    public void load() throws JSONException, IOException {
        JSONObject json = Global.getSettings().loadJSON("treasurehunt_settings.json", "spinloki_treasurehunt");
        treasureHuntPackageSmodPercentBonus = (float) json.getDouble("th_treasure_hunt_package_smod_percent_bonus");
        treasureHuntPackageMaxMult = (float) json.getDouble("th_treasure_hunt_package_mult_clamp");
        debugUseTimeFactor = json.getBoolean("th_debug_use_time_factor");
        debugTimeFactorPoints = json.getInt("th_debug_time_factor_points");
        explorationValues = json.getJSONObject("th_exploration_values");
        colonyRuinsBaseProgressDivisor = json.getInt("th_colony_ruins_base_progress_divisor");
        colonyTechMiningProgressMultiplier = json.getInt("th_colony_tech_mining_progress_multiplier");
        sectorSprintReward = json.getInt("th_sector_sprint_reward");
        pickBlueprintWeight = (float) json.getDouble("th_pick_blueprint_weight");
        abandonCarryoverFactor = (float) json.getDouble("th_abandon_carryover_factor");
        raidDiminishingReturnsEnabled = json.getBoolean("th_raid_diminishing_returns_enabled");
        raidDiminishingReturnsFactor = (float) json.getDouble("th_raid_diminishing_returns_factor");
        raidDiminishingReturnsRecoveryDays = json.getInt("th_raid_diminishing_returns_recovery_days");
        numLeadCandidates = json.getInt("th_num_lead_candidates");
        scavengerSwarmHasslingEnabled = json.getBoolean("th_scavenger_swarm_hassling_enabled");
        sectorSprintMeddlingMult = (float) json.getDouble("th_sector_sprint_meddling_mult");
    }

    // --- Getters ---

    public float getTreasureHuntPackageSmodPercentBonus() { return treasureHuntPackageSmodPercentBonus; }
    public float getTreasureHuntPackageMaxMult() { return treasureHuntPackageMaxMult; }
    public boolean isDebugUseTimeFactor() { return debugUseTimeFactor; }
    public int getDebugTimeFactorPoints() { return debugTimeFactorPoints; }
    public JSONObject getExplorationValues() { return explorationValues; }
    public int getColonyRuinsBaseProgressDivisor() { return colonyRuinsBaseProgressDivisor; }
    public int getColonyTechMiningProgressMultiplier() { return colonyTechMiningProgressMultiplier; }
    public int getSectorSprintReward() { return sectorSprintReward; }
    public float getPickBlueprintWeight() { return pickBlueprintWeight; }
    public float getAbandonCarryoverFactor() { return abandonCarryoverFactor; }
    public boolean isRaidDiminishingReturnsEnabled() { return raidDiminishingReturnsEnabled; }
    public float getRaidDiminishingReturnsFactor() { return raidDiminishingReturnsFactor; }
    public int getRaidDiminishingReturnsRecoveryDays() { return raidDiminishingReturnsRecoveryDays; }
    public int getNumLeadCandidates() { return numLeadCandidates; }
    public boolean isScavengerSwarmHasslingEnabled() { return scavengerSwarmHasslingEnabled; }
    public float getSectorSprintMeddlingMult() { return sectorSprintMeddlingMult; }
    public int getScavengerDataCreditsPerPoint() { return scavengerDataCreditsPerPoint; }
    public float getScavengerMinFp() { return scavengerMinFp; }
    public float getScavengerMaxFp() { return scavengerMaxFp; }
}