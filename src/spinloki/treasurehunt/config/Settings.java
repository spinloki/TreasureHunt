package spinloki.treasurehunt.config;

import com.fs.starfarer.api.Global;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Settings {
    public static Float TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = 100F;
    public static Float TH_TREASURE_HUNT_PACKAGE_MAX_MULT = 3F;
    public static boolean TH_DEBUG_USE_TIME_FACTOR = false;
    public static JSONObject TH_BLUEPRINT_PRIORITY_QUEUE;
    public static JSONObject TH_RUINS_EXPLORATION_VALUES;

    public static void loadSettingsFromJson() throws JSONException, IOException {
        JSONObject json = Global.getSettings().loadJSON("treasurehunt_settings.json", "spinloki_treasurehunt");
        TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS = (float) json.getDouble("th_treasure_hunt_package_smod_percent_bonus");
        TH_TREASURE_HUNT_PACKAGE_MAX_MULT = (float) json.getDouble("th_treasure_hunt_package_mult_clamp");
        TH_DEBUG_USE_TIME_FACTOR = json.getBoolean("th_debug_use_time_factor");
        TH_BLUEPRINT_PRIORITY_QUEUE = json.getJSONObject("th_ship_blueprint_priority_queue");
        TH_RUINS_EXPLORATION_VALUES = json.getJSONObject("th_ruins_exploration_values");
    }
}
