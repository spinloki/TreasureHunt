package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One entry from the merged {@code th_blueprints_packages} settings block. Explicit content lists
 * get the package key stamped onto their specs by {@code THVanillaItemTagger}; a tag expression
 * instead selects specs tagged elsewhere and is left to {@code MultiBlueprintItemPlugin}.
 */
public class THBlueprintPackage {
    private static final Logger log = Global.getLogger(THBlueprintPackage.class);

    private final String key;
    private final String name;
    private final String desc;
    private final String icon;
    private final String iconEntry;
    private final String emblem;
    private final Color emblemColor;
    private final float price;
    private final boolean oneTime;
    private final List<String> ships;
    private final List<String> weapons;
    private final List<String> fighters;
    private final List<String> tagExpression;

    private THBlueprintPackage(String key, String name, String desc, String icon, String iconEntry,
                               String emblem, Color emblemColor,
                               float price, boolean oneTime, List<String> ships, List<String> weapons,
                               List<String> fighters, List<String> tagExpression) {
        this.key = key;
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.iconEntry = iconEntry;
        this.emblem = emblem;
        this.emblemColor = emblemColor;
        this.price = price;
        this.oneTime = oneTime;
        this.ships = ships;
        this.weapons = weapons;
        this.fighters = fighters;
        this.tagExpression = tagExpression;
    }

    /** Returns null if the entry is malformed; the caller logs and skips. */
    public static THBlueprintPackage parse(String key, JSONObject json) {
        List<String> tagExpression = optStringList(json, "tags", key);
        List<String> ships = optStringList(json, "ships", key);
        List<String> weapons = optStringList(json, "weapons", key);
        List<String> fighters = optStringList(json, "fighters", key);

        if (!tagExpression.isEmpty() && !(ships.isEmpty() && weapons.isEmpty() && fighters.isEmpty())) {
            log.warn("Blueprint package '" + key + "' declares both \"tags\" and explicit content lists; "
                    + "the explicit lists will be ignored");
        }
        if (tagExpression.isEmpty() && ships.isEmpty() && weapons.isEmpty() && fighters.isEmpty()) {
            log.warn("Blueprint package '" + key + "' has no contents and will resolve to nothing");
        }

        return new THBlueprintPackage(
                key,
                json.optString("name", null),
                json.optString("desc", null),
                json.optString("icon", null),
                json.optString("iconEntry", null),
                json.optString("emblem", null),
                optColor(json, "emblemColor", key),
                (float) json.optDouble("price", -1d),
                json.optBoolean("oneTime", false),
                ships, weapons, fighters, tagExpression);
    }

    private static Color optColor(JSONObject json, String field, String key) {
        JSONArray a = json.optJSONArray(field);
        if (a == null) return null;
        if (a.length() < 3) {
            log.warn("Blueprint package '" + key + "' has a malformed " + field + ", expected [r,g,b] or [r,g,b,a]");
            return null;
        }
        try {
            return new Color(a.getInt(0), a.getInt(1), a.getInt(2), a.length() > 3 ? a.getInt(3) : 255);
        } catch (JSONException | IllegalArgumentException e) {
            log.warn("Blueprint package '" + key + "' has an unreadable " + field + ": " + e.getMessage());
            return null;
        }
    }

    private static List<String> optStringList(JSONObject json, String field, String key) {
        JSONArray array = json.optJSONArray(field);
        if (array == null) return Collections.emptyList();
        List<String> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            try {
                result.add(array.getString(i));
            } catch (JSONException e) {
                log.warn("Blueprint package '" + key + "' has a non-string entry at " + field + "[" + i + "], skipping it");
            }
        }
        return result;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getIcon() { return icon; }
    public String getIconEntry() { return iconEntry; }
    public String getEmblem() { return emblem; }
    public Color getEmblemColor() { return emblemColor; }
    public boolean hasPrice() { return price >= 0f; }
    public float getPrice() { return price; }
    public boolean isOneTime() { return oneTime; }

    public boolean usesTagExpression() { return !tagExpression.isEmpty(); }

    public List<String> getShips() { return usesTagExpression() ? Collections.emptyList() : ships; }
    public List<String> getWeapons() { return usesTagExpression() ? Collections.emptyList() : weapons; }
    public List<String> getFighters() { return usesTagExpression() ? Collections.emptyList() : fighters; }

    /** The declared expression, or the package key that the tagger stamps onto listed specs. */
    public Set<String> getEffectiveTags() {
        Set<String> result = new LinkedHashSet<>();
        if (usesTagExpression()) {
            result.addAll(tagExpression);
        } else {
            result.add(key);
        }
        return result;
    }
}
