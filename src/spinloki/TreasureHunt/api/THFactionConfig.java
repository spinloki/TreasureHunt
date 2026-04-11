package spinloki.TreasureHunt.api;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import spinloki.TreasureHunt.internal.fleets.THBaseScavengerSwarmFleetAssignmentAI;

import java.util.Random;

/**
 * Immutable configuration for registering a faction into the scavenger swarm system.
 * Use {@link #builder()} for full customization, or pass a {@link THFactionTemplate}
 * to {@link THApi#registerFaction(String, THFactionTemplate)} for common presets.
 */
public class THFactionConfig {
    private final THFactionTemplate template;
    private final String narrativeText;
    private final THSwarmAICreator aiCreator;
    private final THSwarmFleetCreator fleetCreator;
    private final String fleetType;
    private final float freighterPts;
    private final float tankerPts;
    private final String fleetName;
    private final String stationEntityType;
    private final String stationName;
    private final String marketFactionId;
    private final Float qualityOverride;
    private final float qualityMod;
    private final Boolean ignoreMarketFleetSizeMult;

    private THFactionConfig(Builder builder) {
        this.template = builder.template;
        this.narrativeText = builder.narrativeText;
        this.fleetType = builder.fleetType != null ? builder.fleetType : template.getDefaultFleetType();
        this.freighterPts = builder.freighterPts != null ? builder.freighterPts : template.getDefaultFreighterPts();
        this.tankerPts = builder.tankerPts != null ? builder.tankerPts : template.getDefaultTankerPts();
        this.fleetName = builder.fleetName != null ? builder.fleetName : template.getDefaultFleetName();
        this.stationEntityType = builder.stationEntityType;
        this.stationName = builder.stationName;
        this.marketFactionId = builder.marketFactionId;
        this.qualityOverride = builder.qualityOverride;
        this.qualityMod = builder.qualityMod != null ? builder.qualityMod : 0f;
        this.ignoreMarketFleetSizeMult = builder.ignoreMarketFleetSizeMult;
        this.aiCreator = builder.aiCreator != null ? builder.aiCreator : resolveDefaultAiCreator();
        this.fleetCreator = builder.fleetCreator != null ? builder.fleetCreator : null;
    }

    private THSwarmAICreator resolveDefaultAiCreator() {
        if (narrativeText != null) {
            return (fleet, route) -> new THBaseScavengerSwarmFleetAssignmentAI(fleet, route, narrativeText);
        }
        return THBaseScavengerSwarmFleetAssignmentAI::new;
    }

    public THFactionTemplate getTemplate() { return template; }
    public String getNarrativeText() { return narrativeText; }
    public THSwarmAICreator getAiCreator() { return aiCreator; }
    public THSwarmFleetCreator getFleetCreator() { return fleetCreator; }
    public String getFleetType() { return fleetType; }
    public float getFreighterPts() { return freighterPts; }
    public float getTankerPts() { return tankerPts; }
    public String getFleetName() { return fleetName; }
    public String getStationEntityType() { return stationEntityType; }
    public String getStationName() { return stationName; }
    public Float getQualityOverride() { return qualityOverride; }
    public float getQualityMod() { return qualityMod; }
    public Boolean getIgnoreMarketFleetSizeMult() { return ignoreMarketFleetSizeMult; }

    /**
     * Returns the faction ID to use when searching for source markets.
     * If {@code null}, the registered faction's own ID is used.
     */
    public String getMarketFactionId() { return marketFactionId; }

    /**
     * Returns {@link #getMarketFactionId()} if set, otherwise the given faction ID.
     */
    public String getMarketFactionIdOrDefault(String factionId) {
        return marketFactionId != null ? marketFactionId : factionId;
    }

    /**
     * Returns the station entity type to use when spawning a station for this faction.
     * If {@link #getStationEntityType()} is set, returns that. Otherwise picks a random
     * type from the template's defaults.
     */
    public String pickStationEntityType(java.util.Random random) {
        if (stationEntityType != null) return stationEntityType;
        String[] defaults = template.getDefaultStationTypes();
        return defaults[random.nextInt(defaults.length)];
    }

    /**
     * Creates a fleet using the configured fleet params. Used as the default
     * when no custom {@link THSwarmFleetCreator} is provided.
     */
    public CampaignFleetAPI createDefaultFleet(StarSystemAPI system, RouteData route,
                                                MarketAPI sourceMarket, Random random, String factionId) {
        var params = new FleetParamsV3(
                sourceMarket,
                system.getLocation(),
                factionId,
                qualityOverride,
                fleetType,
                40f + random.nextFloat() * 60,
                freighterPts,
                tankerPts,
                0f, 0f, 0f, qualityMod
        );
        if (ignoreMarketFleetSizeMult != null) {
            params.ignoreMarketFleetSizeMult = ignoreMarketFleetSizeMult;
        }
        var fleet = FleetFactoryV3.createFleet(params);
        if (fleet != null && fleetName != null) {
            fleet.setName(fleetName);
        }
        return fleet;
    }

    public static Builder builder() {
        return new Builder();
    }

    @FunctionalInterface
    public interface THSwarmAICreator {
        RouteFleetAssignmentAI create(CampaignFleetAPI fleet, RouteData route);
    }

    @FunctionalInterface
    public interface THSwarmFleetCreator {
        CampaignFleetAPI createFleet(
                StarSystemAPI system,
                RouteData route,
                MarketAPI sourceMarket,
                Random random
        );
    }

    public static class Builder {
        private THFactionTemplate template = THFactionTemplate.SCAVENGER;
        private String narrativeText;
        private THSwarmAICreator aiCreator;
        private THSwarmFleetCreator fleetCreator;
        private String fleetType;
        private Float freighterPts;
        private Float tankerPts;
        private String fleetName;
        private String stationEntityType;
        private String stationName;
        private String marketFactionId;
        private Float qualityOverride;
        private Float qualityMod;
        private Boolean ignoreMarketFleetSizeMult;

        private Builder() {}

        public Builder template(THFactionTemplate template) {
            this.template = template;
            return this;
        }

        public Builder narrativeText(String narrativeText) {
            this.narrativeText = narrativeText;
            return this;
        }

        public Builder aiCreator(THSwarmAICreator aiCreator) {
            this.aiCreator = aiCreator;
            return this;
        }

        public Builder fleetCreator(THSwarmFleetCreator fleetCreator) {
            this.fleetCreator = fleetCreator;
            return this;
        }

        public Builder fleetType(String fleetType) {
            this.fleetType = fleetType;
            return this;
        }

        public Builder freighterPts(float freighterPts) {
            this.freighterPts = freighterPts;
            return this;
        }

        public Builder tankerPts(float tankerPts) {
            this.tankerPts = tankerPts;
            return this;
        }

        public Builder fleetName(String fleetName) {
            this.fleetName = fleetName;
            return this;
        }

        public Builder stationEntityType(String stationEntityType) {
            this.stationEntityType = stationEntityType;
            return this;
        }

        public Builder stationName(String stationName) {
            this.stationName = stationName;
            return this;
        }

        /**
         * Sets a proxy faction ID whose markets are used as fleet source/destination.
         * Use this when the registered faction has no markets of its own.
         */
        public Builder marketFactionId(String marketFactionId) {
            this.marketFactionId = marketFactionId;
            return this;
        }

        /**
         * Overrides the market-derived ship quality with a fixed value.
         * Typical range: 0.0 (2+ d-mods) to 1.0 (pristine). {@code null} uses market quality.
         */
        public Builder qualityOverride(float qualityOverride) {
            this.qualityOverride = qualityOverride;
            return this;
        }

        /**
         * Additive modifier applied on top of market-derived quality.
         */
        public Builder qualityMod(float qualityMod) {
            this.qualityMod = qualityMod;
            return this;
        }

        /**
         * If {@code true}, fleet size is not scaled by the source market's fleet size multiplier.
         */
        public Builder ignoreMarketFleetSizeMult(boolean ignore) {
            this.ignoreMarketFleetSizeMult = ignore;
            return this;
        }

        public THFactionConfig build() {
            return new THFactionConfig(this);
        }
    }
}
