package spinloki.TreasureHunt.api;

import com.fs.starfarer.api.Global;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenient base class for custom treasure hunt opportunities.
 * Provides automatic probability decay and trigger count persistence across save/load.
 *
 * <p>Subclasses should override {@link #trigger()} to set up their event, calling
 * {@code super.trigger()} to ensure the trigger count is tracked and persisted.</p>
 *
 * <p>Trigger counts are stored in sector persistent data, keyed by the opportunity's
 * fully qualified class name. This means counts survive save/load automatically.</p>
 */
public abstract class BaseTHOpportunity implements ITHOpportunity {
    private static final String PERSISTENCE_KEY = "th_opportunity_trigger_counts";

    protected int timesTriggered = 0;

    protected BaseTHOpportunity() {
        restoreTriggerCount();
    }

    /**
     * Returns a decaying probability weight: {@code 1 / (1 + n)²} where n is the
     * number of times this opportunity has been triggered. This prevents any single
     * opportunity from dominating the selection pool.
     */
    @Override
    public float getProbabilityWeight() {
        float denominator = (1 + timesTriggered);
        return 1f / (denominator * denominator);
    }

    /**
     * Increments the trigger count and persists it.
     * Subclasses must call {@code super.trigger()} to maintain correct decay behavior.
     */
    @Override
    public void trigger() {
        timesTriggered++;
        persistTriggerCount();
    }

    public int getTimesTriggered() {
        return timesTriggered;
    }

    @SuppressWarnings("unchecked")
    private void restoreTriggerCount() {
        try {
            var sector = Global.getSector();
            if (sector != null) {
                Map<String, Integer> counts = (Map<String, Integer>) sector.getPersistentData().get(PERSISTENCE_KEY);
                if (counts != null) {
                    Integer saved = counts.get(getClass().getName());
                    if (saved != null) {
                        timesTriggered = saved;
                    }
                }
            }
        } catch (Exception ignored) {
            // Sector not available yet - start fresh
        }
    }

    @SuppressWarnings("unchecked")
    private void persistTriggerCount() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Map<String, Integer> counts = (Map<String, Integer>) data.get(PERSISTENCE_KEY);
        if (counts == null) {
            counts = new HashMap<>();
            data.put(PERSISTENCE_KEY, counts);
        }
        counts.put(getClass().getName(), timesTriggered);
    }
}
