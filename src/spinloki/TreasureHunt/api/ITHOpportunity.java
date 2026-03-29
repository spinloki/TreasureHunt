package spinloki.TreasureHunt.api;

/**
 * Public interface for treasure hunt opportunities.
 * Implement this to create custom opportunity types that can be registered
 * via {@link THApi#registerOpportunity(ITHOpportunity)}.
 */
public interface ITHOpportunity {
    /**
     * Returns the current probability weight for this opportunity being selected.
     * Lower values mean less likely to be picked. Zero or negative means it won't be picked.
     * Standard behavior is 1/(n+1)^2 where n is the number of times the player has encountered this opportunity.
     * Beware that if your opportunity does not decay similarly, it may end up dominating the selection pool
     * after the first few encounters.
     */
    float getProbabilityWeight();

    /**
     * Called when this opportunity is selected. Should create the associated intel,
     * spawn fleets, or perform whatever setup the opportunity requires.
     */
    void trigger();

    /**
     * Returns a short display name for this opportunity, shown to the player
     * when the opportunity is found (e.g. "Scavenger Swarm", "Ruin Excavation").
     */
    String getDisplayName();

    /**
     * Returns the sprite ID for this opportunity's icon, registered under the
     * {@link #ICON_CATEGORY} graphics category in settings.json.
     * For example, {@code "scavenger_swarm"} resolves via
     * {@code Global.getSettings().getSpriteName("treasure_hunt_events", "scavenger_swarm")}.
     */
    String getIcon();

    /** The graphics category under which all opportunity icons must be registered. */
    String ICON_CATEGORY = "treasure_hunt_events";
}
