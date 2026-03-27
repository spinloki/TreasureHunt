package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.api.THFactionTemplate;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for factions participating in scavenger swarms.
 * Loads faction definitions from the auto-merged {@code th_factions} key
 * in {@code data/config/settings.json}, and allows programmatic registration
 * via {@link #register(String, THFactionConfig)}.
 */
public class THFactionRegistry {
    private static final Logger log = Global.getLogger(THFactionRegistry.class);
    private final Map<String, THFactionConfig> factions = new LinkedHashMap<>();

    public void register(String factionId, THFactionConfig config) {
        factions.put(factionId, config);
        log.info("Registered faction for scavenger swarm: " + factionId
                + " (template: " + config.getTemplate() + ")");
    }

    /**
     * Loads faction entries from the auto-merged {@code th_factions} key in settings.json.
     * Each entry maps a faction ID to a JSON object with optional fields:
     * <ul>
     *   <li>{@code template} — one of SCAVENGER, ENFORCER, INQUISITOR, PRIVATEER (default: SCAVENGER)</li>
     *   <li>{@code narrativeText} — custom fleet activity text</li>
     *   <li>{@code fleetType} — Starsector fleet type ID override</li>
     *   <li>{@code freighterPts} — freighter fleet points override</li>
     *   <li>{@code tankerPts} — tanker fleet points override</li>
     *   <li>{@code fleetName} — custom fleet name override</li>
     * </ul>
     * Unspecified fields fall back to the template's defaults.
     */
    @SuppressWarnings("unchecked")
    public void loadFromSettings() {
        try {
            JSONObject thFactions = Global.getSettings().getJSONObject("th_factions");
            Iterator<String> keys = thFactions.keys();
            while (keys.hasNext()) {
                String factionId = keys.next();
                JSONObject entry = thFactions.getJSONObject(factionId);
                THFactionConfig config = parseEntry(factionId, entry);
                if (config != null) {
                    register(factionId, config);
                }
            }
        } catch (JSONException e) {
            log.warn("No th_factions found in settings.json (or parse error), skipping JSON faction loading", e);
        }
    }

    private THFactionConfig parseEntry(String factionId, JSONObject entry) {
        try {
            THFactionConfig.Builder builder = THFactionConfig.builder();

            if (entry.has("template")) {
                builder.template(THFactionTemplate.valueOf(entry.getString("template")));
            }
            if (entry.has("narrativeText")) {
                builder.narrativeText(entry.getString("narrativeText"));
            }
            if (entry.has("fleetType")) {
                builder.fleetType(entry.getString("fleetType"));
            }
            if (entry.has("freighterPts")) {
                builder.freighterPts((float) entry.getDouble("freighterPts"));
            }
            if (entry.has("tankerPts")) {
                builder.tankerPts((float) entry.getDouble("tankerPts"));
            }
            if (entry.has("fleetName")) {
                builder.fleetName(entry.getString("fleetName"));
            }
            if (entry.has("stationEntityType")) {
                builder.stationEntityType(entry.getString("stationEntityType"));
            }
            if (entry.has("stationName")) {
                builder.stationName(entry.getString("stationName"));
            }

            return builder.build();
        } catch (JSONException e) {
            log.error("Failed to parse th_factions entry for faction: " + factionId, e);
            return null;
        }
    }

    public THFactionConfig get(String factionId) {
        return factions.get(factionId);
    }

    public Map<String, THFactionConfig> getAll() {
        return Collections.unmodifiableMap(factions);
    }

    public boolean isRegistered(String factionId) {
        return factions.containsKey(factionId);
    }

    public void clear() {
        factions.clear();
    }
}
