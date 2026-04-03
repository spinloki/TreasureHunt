package spinloki.TreasureHunt.api;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import spinloki.TreasureHunt.internal.config.THSettings;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.internal.registry.THRewardRegistry;

/**
 * Public API facade for the TreasureHunt mod.
 * Other mods should use this class to register their content programmatically.
 *
 * <p>For simple cases (reward sources, items, factions with preset templates),
 * other mods can instead add keys to their own {@code data/config/settings.json}
 * and Starsector's auto-merge will handle registration. See the TreasureHunt
 * documentation for the supported JSON keys.</p>
 *
 * <p>This API is for cases requiring custom Java behavior, such as:</p>
 * <ul>
 *   <li>Custom opportunity types (require behavior code)</li>
 *   <li>Faction configs with custom fleet creators or AI</li>
 * </ul>
 *
 * <h3>Supported settings.json keys (auto-merged across mods):</h3>
 * <ul>
 *   <li>{@code th_rewards} (JSONObject) — maps entity type IDs to point values or aliases</li>
 *   <li>{@code th_one_time_items} (JSONArray) — special item IDs for one-time rewards</li>
 *   <li>{@code th_repeat_items} (JSONArray) — special item IDs for repeatable rewards</li>
 *   <li>{@code th_blueprints_packages} (JSONObject) — blueprint package definitions</li>
 *   <li>{@code th_factions} (JSONObject) — faction template registrations</li>
 * </ul>
 */
public class THApi {

    /**
     * Returns the read-only settings instance containing tuning knobs.
     */
    public static THSettings getSettings() {
        return THRegistry.getSettings();
    }

    /**
     * Returns the reward registry for reward/item/blueprint lookups.
     */
    public static THRewardRegistry getRewards() {
        return THRegistry.getRewardRegistry();
    }

    /**
     * Register a custom opportunity type. Opportunities require Java code
     * (unlike rewards/items which can be registered via settings.json).
     *
     * <p>Call this during {@code onGameLoad()}
     * in your mod plugin.</p>
     */
    public static void registerOpportunity(ITHOpportunity opportunity) {
        THRegistry.registerOpportunity(opportunity);
    }

    /**
     * Add a progress factor to the treasure hunt event.
     * Creates the event if it doesn't exist yet.
     *
     * <p>Use this when your mod detects a player action that should
     * contribute progress. Create a factor by extending
     * {@code BaseOneTimeFactor} (for instant progress) or
     * {@code BaseEventFactor} (for recurring progress).</p>
     *
     * @param factor the factor to add (e.g. a {@code BaseOneTimeFactor} subclass)
     * @param dialog the current interaction dialog, or {@code null} if not in a dialog
     */
    public static void addProgress(EventFactor factor, InteractionDialogAPI dialog) {
        TreasureHuntEventIntel.addFactorCreateIfNecessary(factor, dialog);
    }

    /**
     * Register a faction for scavenger swarm participation with full configuration.
     * Use this when you need custom fleet creators or AI behavior.
     *
     * <p>For simple cases, add an entry to {@code th_factions} in your
     * {@code data/config/settings.json} instead.</p>
     *
     * @param factionId the faction's string ID (e.g., "my_faction")
     * @param config    built via {@link THFactionConfig#builder()}
     */
    public static void registerFaction(String factionId, THFactionConfig config) {
        THRegistry.registerFaction(factionId, config);
    }

    /**
     * Register a faction for scavenger swarm participation using a preset template.
     * The template provides default fleet type, AI behavior, and hassle style.
     *
     * @param factionId the faction's string ID
     * @param template  one of {@link THFactionTemplate} presets
     */
    public static void registerFaction(String factionId, THFactionTemplate template) {
        THRegistry.registerFaction(factionId, template);
    }
}
