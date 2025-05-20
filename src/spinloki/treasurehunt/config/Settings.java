package spinloki.treasurehunt.config;

import com.fs.starfarer.api.Global;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static Logger log = Global.getLogger(Settings.class);

    public static Float TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = 100F;
    public static Float TH_TREASURE_HUNT_PACKAGE_MAX_MULT = 3F;
    public static boolean TH_DEBUG_USE_TIME_FACTOR = false;
    public static int TH_DEBUG_TIME_FACTOR_POINTS = 50;
    public static JSONObject TH_EXPLORATION_VALUES;
    public static Integer TH_COLONY_RUINS_BASE_PROGRESS_DIVISOR;
    public static Integer TH_COLONY_TECH_MINING_PROGRESS_MULTIPLIER;
    public static JSONObject TH_BLUEPRINTS_PACKAGES;

    public static void loadSettingsFromJson() throws JSONException, IOException {
        JSONObject json = Global.getSettings().loadJSON("treasurehunt_settings.json", "spinloki_treasurehunt");
        TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = (float) json.getDouble("th_treasure_hunt_package_smod_percent_bonus");
        TH_TREASURE_HUNT_PACKAGE_MAX_MULT = (float) json.getDouble("th_treasure_hunt_package_mult_clamp");
        TH_DEBUG_USE_TIME_FACTOR = json.getBoolean("th_debug_use_time_factor");
        TH_DEBUG_TIME_FACTOR_POINTS = json.getInt("th_debug_time_factor_points");
        TH_EXPLORATION_VALUES = json.getJSONObject("th_exploration_values");
        TH_COLONY_RUINS_BASE_PROGRESS_DIVISOR = json.getInt("th_colony_ruins_base_progress_divisor");
        TH_COLONY_TECH_MINING_PROGRESS_MULTIPLIER = json.getInt("th_colony_tech_mining_progress_multiplier");
        TH_BLUEPRINTS_PACKAGES = json.getJSONObject("th_blueprints_packages");
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
}
