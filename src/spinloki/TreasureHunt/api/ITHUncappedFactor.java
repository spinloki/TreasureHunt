package spinloki.TreasureHunt.api;

/**
 * Marker interface for monthly {@link com.fs.starfarer.api.impl.campaign.intel.events.EventFactor}
 * implementations whose progress should not be subject to the monthly cap.
 *
 * <p>Normal monthly factors are summed and capped at
 * {@link com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel#getMaxMonthlyProgress()}.
 * Factors implementing this interface are summed separately and added on top of the capped total,
 * allowing them to exceed the normal monthly limit.</p>
 */
public interface ITHUncappedFactor {
}
