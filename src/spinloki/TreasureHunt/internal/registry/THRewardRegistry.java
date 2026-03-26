package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for reward definitions, item pools, and blueprint packages.
 * Loaded from the auto-merged settings.json keys: th_rewards, th_one_time_items,
 * th_repeat_items, th_blueprints_packages.
 */
public class THRewardRegistry {
    private static final Logger log = Global.getLogger(THRewardRegistry.class);

    private JSONObject rewards;
    private JSONArray oneTimeItems;
    private JSONArray repeatItems;
    private JSONObject blueprintPackages;
    private float pickBlueprintWeight;

    @SuppressWarnings("unchecked")
    public void loadFromSettings(float pickBlueprintWeight) {
        try {
            this.rewards = Global.getSettings().getJSONObject("th_rewards");
        } catch (JSONException e) {
            log.warn("No th_rewards found in settings.json", e);
            this.rewards = new JSONObject();
        }
        try {
            this.oneTimeItems = Global.getSettings().getJSONArray("th_one_time_items");
        } catch (JSONException e) {
            log.warn("No th_one_time_items found in settings.json", e);
            this.oneTimeItems = new JSONArray();
        }
        try {
            this.repeatItems = Global.getSettings().getJSONArray("th_repeat_items");
        } catch (JSONException e) {
            log.warn("No th_repeat_items found in settings.json", e);
            this.repeatItems = new JSONArray();
        }
        try {
            this.blueprintPackages = Global.getSettings().getJSONObject("th_blueprints_packages");
        } catch (JSONException e) {
            log.warn("No th_blueprints_packages found in settings.json", e);
            this.blueprintPackages = new JSONObject();
        }
        this.pickBlueprintWeight = pickBlueprintWeight;
    }

    // --- Reward lookups ---

    public boolean hasReward(String entityTypeId) {
        return rewards.has(entityTypeId);
    }

    public JSONObject resolveAliases(String entityTypeId) {
        String currentKey = entityTypeId;
        while (rewards.has(currentKey)) {
            try {
                Object value = rewards.get(currentKey);
                if (value instanceof JSONObject) {
                    return (JSONObject) value;
                } else if (value instanceof String) {
                    currentKey = (String) value;
                } else {
                    break;
                }
            } catch (JSONException e) {
                break;
            }
        }
        log.error("Failed to resolve alias for " + entityTypeId);
        return null;
    }

    public int getRewardValue(String entityTypeId) {
        int val = 5;
        try {
            var obj = resolveAliases(entityTypeId);
            if (obj != null) {
                val = obj.getInt("value");
            }
        } catch (JSONException e) {
            log.error("Failed to get treasure hunt reward value for " + entityTypeId);
        }
        return val;
    }

    public String getRewardDescription(String entityTypeId) {
        String desc = "exploration";
        try {
            var obj = resolveAliases(entityTypeId);
            if (obj != null) {
                desc = obj.getString("description");
            }
        } catch (JSONException e) {
            log.error("Failed to get treasure hunt reward description for " + entityTypeId);
        }
        return desc;
    }

    // --- Item pools ---

    public List<String> getOneTimeItems() {
        return jsonArrayToStringList(oneTimeItems, "th_one_time_items");
    }

    public List<String> getRepeatItems() {
        return jsonArrayToStringList(repeatItems, "th_repeat_items");
    }

    // --- Blueprint packages ---

    public float getPickBlueprintWeight() {
        return pickBlueprintWeight;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllBlueprintPackages() {
        var keys = blueprintPackages.keys();
        List<String> result = new ArrayList<>();
        while (keys.hasNext()) {
            result.add(keys.next().toString());
        }
        return result;
    }

    public List<String> getIdsFromPackage(String packageName, String type) {
        List<String> ids = new ArrayList<>();
        try {
            if (blueprintPackages.has(packageName)) {
                JSONObject pkg = blueprintPackages.getJSONObject(packageName);
                JSONArray typeArray = pkg.getJSONArray(type);
                for (int i = 0; i < typeArray.length(); i++) {
                    ids.add(typeArray.getString(i));
                }
            } else {
                log.error("Blueprint package not found: " + packageName);
            }
        } catch (Exception e) {
            log.error("Error reading from blueprint package: " + e.getMessage());
        }
        return ids;
    }

    public List<String> getFightersFromPackage(String packageName) {
        return getIdsFromPackage(packageName, "fighters");
    }

    public List<String> getShipsFromPackage(String packageName) {
        return getIdsFromPackage(packageName, "ships");
    }

    public List<String> getWeaponsFromPackage(String packageName) {
        return getIdsFromPackage(packageName, "weapons");
    }

    // --- Helpers ---

    private static List<String> jsonArrayToStringList(JSONArray jsonArray, String name) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                result.add(jsonArray.getString(i));
            } catch (JSONException e) {
                throw new RuntimeException("Index " + i + " in " + name + " was not a string");
            }
        }
        return result;
    }
}
