package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.api.THFactionConfig;
import spinloki.TreasureHunt.api.THFactionTemplate;
import spinloki.TreasureHunt.internal.config.THSettings;

/**
 * Central registry backing the THApi facade.
 * Holds references to all sub-registries and the settings instance.
 */
public class THRegistry {
    private static final Logger log = Global.getLogger(THRegistry.class);

    private static THSettings settings;
    private static THRewardRegistry rewardRegistry;
    private static THOpportunityRegistry opportunityRegistry;
    private static THFactionRegistry factionRegistry;

    public static void init() {
        opportunityRegistry = new THOpportunityRegistry();
        factionRegistry = new THFactionRegistry();
        factionRegistry.loadFromSettings();
        log.info("TreasureHunt registries initialized");
    }

    public static void initSettings() throws org.json.JSONException, java.io.IOException {
        settings = new THSettings();
        settings.load();
        rewardRegistry = new THRewardRegistry();
        rewardRegistry.loadFromSettings(settings.getPickOneTimeWeight());
    }

    public static void reset() {
        init();
    }

    public static THSettings getSettings() { return settings; }
    public static THRewardRegistry getRewardRegistry() { return rewardRegistry; }
    public static THOpportunityRegistry getOpportunityRegistry() { return opportunityRegistry; }
    public static THFactionRegistry getFactionRegistry() { return factionRegistry; }

    // --- Delegation methods used by THApi ---

    public static void registerOpportunity(ITHOpportunity opportunity) {
        opportunityRegistry.register(opportunity);
    }

    public static void registerFaction(String factionId, THFactionConfig config) {
        factionRegistry.register(factionId, config);
    }

    public static void registerFaction(String factionId, THFactionTemplate template) {
        factionRegistry.register(factionId, THFactionConfig.builder().template(template).build());
    }
}
