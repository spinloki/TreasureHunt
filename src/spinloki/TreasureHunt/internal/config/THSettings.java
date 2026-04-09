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
    private JSONObject colonyRuinsBaseProgress;
    private int colonyTechMiningProgressMultiplier;
    private int maxMonthlyProgress = 30;
    private int sectorSprintReward;
    private float pickOneTimeWeight;
    private boolean raidDiminishingReturnsEnabled;
    private float raidDiminishingReturnsFactor;
    private int raidDiminishingReturnsRecoveryDays;
    private int numLeadCandidates;
    private boolean scavengerSwarmHasslingEnabled;
    private float sectorSprintMeddlingMult;
    private int scavengerDataCreditsPerPoint = 2000;
    private float scavengerMinFp = 50f;
    private float scavengerMaxFp = 150f;
    private int excavationProgressPoints = 50;
    private int excavationBombardFuelCost = 100;
    private int scavengerDataMinPoints = 20;
    private int scavengerDataMaxPoints = 40;
    private boolean showFactorNotifications = true;

    private static final String MOD_ID = "spinloki_treasurehunt";

    public THSettings() {}

    public void load() throws JSONException, IOException {
        JSONObject json = Global.getSettings().loadJSON("treasurehunt_settings.json", MOD_ID);
        treasureHuntPackageSmodPercentBonus = (float) json.getDouble("th_treasure_hunt_package_smod_percent_bonus");
        treasureHuntPackageMaxMult = (float) json.getDouble("th_treasure_hunt_package_mult_clamp");
        debugUseTimeFactor = json.getBoolean("th_debug_use_time_factor");
        debugTimeFactorPoints = json.getInt("th_debug_time_factor_points");
        explorationValues = json.getJSONObject("th_exploration_values");
        colonyRuinsBaseProgress = json.getJSONObject("th_colony_ruins_base_progress");
        colonyTechMiningProgressMultiplier = json.getInt("th_colony_tech_mining_progress_multiplier");
        sectorSprintReward = json.getInt("th_sector_sprint_reward");
        pickOneTimeWeight = (float) json.getDouble("th_pick_one_time_weight");
        raidDiminishingReturnsEnabled = json.getBoolean("th_raid_diminishing_returns_enabled");
        raidDiminishingReturnsFactor = (float) json.getDouble("th_raid_diminishing_returns_factor");
        raidDiminishingReturnsRecoveryDays = json.getInt("th_raid_diminishing_returns_recovery_days");
        numLeadCandidates = json.getInt("th_num_lead_candidates");
        scavengerSwarmHasslingEnabled = json.getBoolean("th_scavenger_swarm_hassling_enabled");
        sectorSprintMeddlingMult = (float) json.getDouble("th_sector_sprint_meddling_mult");
        excavationProgressPoints = json.getInt("th_excavation_progress_points");
        excavationBombardFuelCost = json.getInt("th_excavation_bombard_fuel_cost");
        scavengerDataMinPoints = json.getInt("th_scavenger_data_min_points");
        scavengerDataMaxPoints = json.getInt("th_scavenger_data_max_points");
        maxMonthlyProgress = json.getInt("th_max_monthly_progress");
        showFactorNotifications = json.optBoolean("th_show_factor_notifications", true);

        detectLuna();
    }

    private boolean lunaEnabled = false;

    private void detectLuna() {
        try {
            Class.forName("lunalib.lunaSettings.LunaSettings");
            lunaEnabled = true;
        } catch (ClassNotFoundException e) {
            lunaEnabled = false;
        }
    }

    /** Reload all LunaSettings values. Called on initial load and when settings change. */
    public void loadFromLuna() {
        if (!lunaEnabled) return;
        try {
            Integer v;
            Double d;
            Boolean b;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_smod_bonus");
            if (v != null) treasureHuntPackageSmodPercentBonus = v;

            d = lunalib.lunaSettings.LunaSettings.getDouble(MOD_ID, "th_luna_max_mult");
            if (d != null) treasureHuntPackageMaxMult = d.floatValue();

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_max_monthly");
            if (v != null) maxMonthlyProgress = v;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_scav_kill_min");
            if (v != null) {
                try { explorationValues.put("scav_kill_min", v); } catch (JSONException ignored) {}
            }

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_scav_kill_max");
            if (v != null) {
                try { explorationValues.put("scav_kill_max", v); } catch (JSONException ignored) {}
            }

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_colony_tech_mult");
            if (v != null) colonyTechMiningProgressMultiplier = v;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_sector_sprint_reward");
            if (v != null) sectorSprintReward = v;

            d = lunalib.lunaSettings.LunaSettings.getDouble(MOD_ID, "th_luna_sector_sprint_meddling");
            if (d != null) sectorSprintMeddlingMult = d.floatValue();

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_excavation_points");
            if (v != null) excavationProgressPoints = v;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_scav_data_min");
            if (v != null) scavengerDataMinPoints = v;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_scav_data_max");
            if (v != null) scavengerDataMaxPoints = v;

            b = lunalib.lunaSettings.LunaSettings.getBoolean(MOD_ID, "th_luna_raid_dr_enabled");
            if (b != null) raidDiminishingReturnsEnabled = b;

            d = lunalib.lunaSettings.LunaSettings.getDouble(MOD_ID, "th_luna_raid_dr_factor");
            if (d != null) raidDiminishingReturnsFactor = d.floatValue();

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_raid_dr_recovery");
            if (v != null) raidDiminishingReturnsRecoveryDays = v;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_num_leads");
            if (v != null) numLeadCandidates = v;

            d = lunalib.lunaSettings.LunaSettings.getDouble(MOD_ID, "th_luna_one_time_weight");
            if (d != null) pickOneTimeWeight = d.floatValue();

            b = lunalib.lunaSettings.LunaSettings.getBoolean(MOD_ID, "th_luna_hassling");
            if (b != null) scavengerSwarmHasslingEnabled = b;

            v = lunalib.lunaSettings.LunaSettings.getInt(MOD_ID, "th_luna_bombard_fuel");
            if (v != null) excavationBombardFuelCost = v;

            b = lunalib.lunaSettings.LunaSettings.getBoolean(MOD_ID, "th_luna_factor_notifications");
            if (b != null) showFactorNotifications = b;

            log.info("LunaLib settings applied");
        } catch (Exception e) {
            log.warn("Failed to load LunaLib settings, using defaults", e);
        }
    }

    // --- Getters ---

    public float getTreasureHuntPackageSmodPercentBonus() { return treasureHuntPackageSmodPercentBonus; }
    public float getTreasureHuntPackageMaxMult() { return treasureHuntPackageMaxMult; }
    public boolean isDebugUseTimeFactor() { return debugUseTimeFactor; }
    public int getDebugTimeFactorPoints() { return debugTimeFactorPoints; }
    public JSONObject getExplorationValues() { return explorationValues; }
    public int getColonyRuinsBaseProgress(String ruinsType) {
        try {
            return colonyRuinsBaseProgress.getInt(ruinsType);
        } catch (JSONException e) {
            log.warn("Unknown ruins type for colony progress: " + ruinsType + ", defaulting to 1");
            return 1;
        }
    }
    public int getColonyTechMiningProgressMultiplier() { return colonyTechMiningProgressMultiplier; }
    public int getMaxMonthlyProgress() { return maxMonthlyProgress; }
    public int getSectorSprintReward() { return sectorSprintReward; }
    public float getPickOneTimeWeight() { return pickOneTimeWeight; }
    public boolean isRaidDiminishingReturnsEnabled() { return raidDiminishingReturnsEnabled; }
    public float getRaidDiminishingReturnsFactor() { return raidDiminishingReturnsFactor; }
    public int getRaidDiminishingReturnsRecoveryDays() { return raidDiminishingReturnsRecoveryDays; }
    public int getNumLeadCandidates() { return numLeadCandidates; }
    public boolean isScavengerSwarmHasslingEnabled() { return scavengerSwarmHasslingEnabled; }
    public float getSectorSprintMeddlingMult() { return sectorSprintMeddlingMult; }
    public int getScavengerDataCreditsPerPoint() { return scavengerDataCreditsPerPoint; }
    public float getScavengerMinFp() { return scavengerMinFp; }
    public float getScavengerMaxFp() { return scavengerMaxFp; }
    public int getExcavationProgressPoints() { return excavationProgressPoints; }
    public int getExcavationBombardFuelCost() { return excavationBombardFuelCost; }
    public int getScavengerDataMinPoints() { return scavengerDataMinPoints; }
    public int getScavengerDataMaxPoints() { return scavengerDataMaxPoints; }
    public boolean isShowFactorNotifications() { return showFactorNotifications; }
}