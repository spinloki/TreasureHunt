package spinloki.TreasureHunt.api;

/**
 * Public interface for treasure claim handlers.
 * A claim handler can intercept the normal treasure delivery (ShowLootListener)
 * and replace it with custom behavior — e.g. a faction fleet demanding the treasure.
 *
 * <p>Implement this to create custom claim handlers that can be registered
 * via {@link THApi#registerClaimHandler(ITHClaimHandler)}.</p>
 *
 * <p>When the hunt reaches the FOUND stage, the system picks a claim handler
 * via weighted random selection. If a handler is picked, it receives the treasure
 * via {@link #trigger(String)}. If no handler is picked (all weights are zero or
 * none are registered), the default behavior (inject into next loot) is used.</p>
 */
public interface ITHClaimHandler {
    /**
     * Returns the current probability weight for this handler being selected.
     * Zero or negative means this handler will be skipped.
     * Return zero when conditions aren't met (e.g. player is already friendly
     * with the relevant faction).
     */
    float getProbabilityWeight();

    /**
     * Called when this handler is selected to handle treasure delivery.
     * The handler is responsible for delivering the treasure to the player
     * in whatever way it sees fit (e.g. spawning a fleet that demands it,
     * creating a quest, etc.).
     *
     * @param treasureId the special item ID of the treasure to deliver
     */
    void trigger(String treasureId);

    /**
     * Returns a short display name for this claim handler,
     * used for logging and debugging.
     */
    String getDisplayName();
}
