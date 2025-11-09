package spinloki.TreasureHunt.config;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class THSettings {
    public static Logger log = Global.getLogger(THSettings.class);

    public static Float TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = 100F;
    public static Float TH_TREASURE_HUNT_PACKAGE_MAX_MULT = 3F;
    public static boolean TH_DEBUG_USE_TIME_FACTOR = false;
    public static int TH_DEBUG_TIME_FACTOR_POINTS = 50;
    public static JSONObject TH_EXPLORATION_VALUES;
    public static Integer TH_COLONY_RUINS_BASE_PROGRESS_DIVISOR;
    public static Integer TH_COLONY_TECH_MINING_PROGRESS_MULTIPLIER;
    public static JSONObject TH_BLUEPRINTS_PACKAGES;
    public static JSONObject TH_REWARDS;
    public static Float TH_PICK_BLUEPRINT_WEIGHT;
    public static Integer TH_SECTOR_SPRINT_REWARD;

    public static void loadSettingsFromJson() throws JSONException, IOException {
        JSONObject json = Global.getSettings().loadJSON("treasurehunt_settings.json", "spinloki_treasurehunt");
        TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = (float) json.getDouble("th_treasure_hunt_package_smod_percent_bonus");
        TH_TREASURE_HUNT_PACKAGE_MAX_MULT = (float) json.getDouble("th_treasure_hunt_package_mult_clamp");
        TH_DEBUG_USE_TIME_FACTOR = json.getBoolean("th_debug_use_time_factor");
        TH_DEBUG_TIME_FACTOR_POINTS = json.getInt("th_debug_time_factor_points");
        TH_EXPLORATION_VALUES = json.getJSONObject("th_exploration_values");
        TH_COLONY_RUINS_BASE_PROGRESS_DIVISOR = json.getInt("th_colony_ruins_base_progress_divisor");
        TH_COLONY_TECH_MINING_PROGRESS_MULTIPLIER = json.getInt("th_colony_tech_mining_progress_multiplier");
        TH_SECTOR_SPRINT_REWARD = json.getInt("th_sector_sprint_reward");
        TH_BLUEPRINTS_PACKAGES = Global.getSettings().getJSONObject("th_blueprints_packages");
        TH_PICK_BLUEPRINT_WEIGHT = (float) json.getDouble("th_pick_blueprint_weight");
    }

    public static void loadTHRewards() throws JSONException, IOException {
        TH_REWARDS = Global.getSettings().getJSONObject("th_rewards");
    }

    public static List<String> getIdsFromPackage(String packageName, String type) {
        List<String> weapons = new ArrayList<>();

        try {
            if (TH_BLUEPRINTS_PACKAGES.has(packageName)) {
                JSONObject pkg = TH_BLUEPRINTS_PACKAGES.getJSONObject(packageName);
                JSONArray typeArray = pkg.getJSONArray(type);

                for (int i = 0; i < typeArray.length(); i++) {
                    weapons.add(typeArray.getString(i));
                }
            } else {
                log.error("Package not found: " + packageName);
            }
        } catch (Exception e) {
            log.error("Error reading weapons from package: " + e.getMessage());
            e.printStackTrace();
        }

        return weapons;
    }

    public static List<String> getFightersFromPackage(String packageName){
        return getIdsFromPackage(packageName, "fighters");
    }

    public static List<String> getShipsFromPackage(String packageName){
        return getIdsFromPackage(packageName, "ships");
    }

    public static List<String> getWeaponsFromPackage(String packageName){
        return getIdsFromPackage(packageName, "weapons");
    }

    public static List<String> getAllBlueprintPackages(){
        var keys = TH_BLUEPRINTS_PACKAGES.keys();
        List<String> blueprintPackages = new ArrayList<>();
        while(keys.hasNext()){
            blueprintPackages.add(keys.next().toString());
        }
        return blueprintPackages;
    }

    public static boolean customDescriptionIdHasTHReward(String customDescriptionId){
        return TH_REWARDS.has(customDescriptionId);
    }

    public static JSONObject resolveTHAliases(String customDescriptionId) throws JSONException {
        String currentKey = customDescriptionId;

        while (TH_REWARDS.has(currentKey)) {
            Object value = TH_REWARDS.get(currentKey);

            if (value instanceof JSONObject) {
                return (JSONObject) value;
            } else if (value instanceof String) {
                currentKey = (String) value;  // Follow alias
            } else {
                break;  // Unexpected type
            }
        }

        log.error("Failed to resolve alias for " + customDescriptionId);
        return null;
    }

    public static int getTHRewardValue(String customDescriptionId){
        var val = 5; // default fallback value
        try{
            var obj = resolveTHAliases(customDescriptionId);
            if (obj != null){
                val = obj.getInt("value");
            }
        }
        catch(JSONException e){
            log.error("Failed to get treasure hunt reward value for " + customDescriptionId);
        }
        return val;
    }

    public static String getTHRewardDescription(String customDescriptionId){
        var desc = "exploration"; // default fallback value
        try{
            var obj = resolveTHAliases(customDescriptionId);
            if (obj != null){
                desc = obj.getString("description");
            }
        }
        catch(JSONException e){
            log.error("Failed to get treasure hunt reward value for " + customDescriptionId);
        }
        return desc;
    }
}