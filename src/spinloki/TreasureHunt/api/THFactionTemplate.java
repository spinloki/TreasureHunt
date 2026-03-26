package spinloki.TreasureHunt.api;

import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

/**
 * Preset behavior archetypes for scavenger swarm factions.
 * Each template defines a default fleet type, AI behavior, hassle style, and fleet composition.
 * Use with {@link THApi#registerFaction(String, THFactionTemplate)} for simple registration,
 * or as a starting point in {@link THFactionConfig.Builder#template(THFactionTemplate)}.
 */
public enum THFactionTemplate {
    /** Generic salvage fleet, no hassling. (Pirates/Independent style) */
    SCAVENGER(FleetTypes.SCAVENGER_LARGE, 20f, 10f, null),
    /** Hassle + inspection behavior. (Hegemony/Persean style) */
    ENFORCER(FleetTypes.INSPECTION_FLEET, 20f, 10f, null),
    /** Hassle + purge behavior. (Luddic Path/Church style) */
    INQUISITOR(FleetTypes.RAIDER, 10f, 5f, null),
    /** Covert ops / privateer behavior. (Tri-Tachyon style) */
    PRIVATEER(FleetTypes.MERC_PRIVATEER, 10f, 5f, null);

    private final String defaultFleetType;
    private final float defaultFreighterPts;
    private final float defaultTankerPts;
    private final String defaultFleetName;

    THFactionTemplate(String defaultFleetType, float defaultFreighterPts, float defaultTankerPts, String defaultFleetName) {
        this.defaultFleetType = defaultFleetType;
        this.defaultFreighterPts = defaultFreighterPts;
        this.defaultTankerPts = defaultTankerPts;
        this.defaultFleetName = defaultFleetName;
    }

    public String getDefaultFleetType() { return defaultFleetType; }
    public float getDefaultFreighterPts() { return defaultFreighterPts; }
    public float getDefaultTankerPts() { return defaultTankerPts; }
    public String getDefaultFleetName() { return defaultFleetName; }
}
