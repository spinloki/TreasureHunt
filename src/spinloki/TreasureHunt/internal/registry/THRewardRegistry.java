package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, THBlueprintPackage> blueprintPackages;
    private float pickOneTimeWeight;

    @SuppressWarnings("unchecked")
    public void loadFromSettings(float pickOneTimeWeight) {
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
        JSONObject packagesJson;
        try {
            packagesJson = Global.getSettings().getJSONObject("th_blueprints_packages");
        } catch (JSONException e) {
            log.warn("No th_blueprints_packages found in settings.json", e);
            packagesJson = new JSONObject();
        }
        this.blueprintPackages = parsePackages(packagesJson);
        this.pickOneTimeWeight = pickOneTimeWeight;
    }

    private static Map<String, THBlueprintPackage> parsePackages(JSONObject json) {
        Map<String, THBlueprintPackage> result = new LinkedHashMap<>();
        var keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            JSONObject entry = json.optJSONObject(key);
            if (entry == null) {
                log.warn("Blueprint package '" + key + "' is not an object, skipping it");
                continue;
            }
            THBlueprintPackage pkg = THBlueprintPackage.parse(key, entry);
            if (pkg != null) result.put(key, pkg);
        }
        log.info("Loaded " + result.size() + " blueprint packages");
        return result;
    }

    // --- Reward lookups ---

    public boolean hasReward(String entityTypeId) {
        return rewards.has(entityTypeId);
    }

    public JSONObject resolveAliases(String entityTypeId) {
        String currentKey = entityTypeId;
        Set<String> visited = new HashSet<>();
        while (rewards.has(currentKey)) {
            if (!visited.add(currentKey)) {
                log.error("Cyclic alias in th_rewards starting at " + entityTypeId + ", cycle reached " + currentKey);
                return null;
            }
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

    public float getPickOneTimeWeight() {
        return pickOneTimeWeight;
    }

    public List<String> getAllBlueprintPackages() {
        return new ArrayList<>(blueprintPackages.keySet());
    }

    public THBlueprintPackage getBlueprintPackage(String packageName) {
        return blueprintPackages.get(packageName);
    }

    public boolean hasBlueprintPackage(String packageName) {
        return blueprintPackages.containsKey(packageName);
    }

    public List<String> getFightersFromPackage(String packageName) {
        THBlueprintPackage pkg = blueprintPackages.get(packageName);
        return pkg == null ? new ArrayList<>() : pkg.getFighters();
    }

    public List<String> getShipsFromPackage(String packageName) {
        THBlueprintPackage pkg = blueprintPackages.get(packageName);
        return pkg == null ? new ArrayList<>() : pkg.getShips();
    }

    public List<String> getWeaponsFromPackage(String packageName) {
        THBlueprintPackage pkg = blueprintPackages.get(packageName);
        return pkg == null ? new ArrayList<>() : pkg.getWeapons();
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
