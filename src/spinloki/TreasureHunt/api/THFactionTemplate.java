package spinloki.TreasureHunt.api;

import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

/**
 * Preset behavior archetypes for scavenger swarm factions.
 * <p>
 * There are two archetypes: {@link #SCAVENGER} (explores only) and {@link #ENFORCER}
 * (explores and hassles the player). Each provides default fleet parameters that can
 * be overridden via {@link THFactionConfig.Builder} or JSON.
 * <p>
 * Use with {@link THApi#registerFaction(String, THFactionTemplate)} for simple registration,
 * or as a starting point in {@link THFactionConfig.Builder#template(THFactionTemplate)}.
 */
public enum THFactionTemplate {
    /** Explores only, no hassling. */
    SCAVENGER(FleetTypes.SCAVENGER_LARGE, 20f, 10f, null,
            new String[]{"station1_Standard"}),
    /** Explores and hassles the player with inspections. */
    ENFORCER(FleetTypes.INSPECTION_FLEET, 20f, 10f, null,
            new String[]{"station1_midline_Standard"});

    private final String defaultFleetType;
    private final float defaultFreighterPts;
    private final float defaultTankerPts;
    private final String defaultFleetName;
    private final String[] defaultStationTypes;

    THFactionTemplate(String defaultFleetType, float defaultFreighterPts, float defaultTankerPts,
                      String defaultFleetName, String[] defaultStationTypes) {
        this.defaultFleetType = defaultFleetType;
        this.defaultFreighterPts = defaultFreighterPts;
        this.defaultTankerPts = defaultTankerPts;
        this.defaultFleetName = defaultFleetName;
        this.defaultStationTypes = defaultStationTypes;
    }

    public String getDefaultFleetType() { return defaultFleetType; }
    public float getDefaultFreighterPts() { return defaultFreighterPts; }
    public float getDefaultTankerPts() { return defaultTankerPts; }
    public String getDefaultFleetName() { return defaultFleetName; }
    public String[] getDefaultStationTypes() { return defaultStationTypes; }
}
