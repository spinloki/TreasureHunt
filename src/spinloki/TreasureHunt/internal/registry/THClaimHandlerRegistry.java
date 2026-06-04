package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.api.ITHClaimHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for treasure claim handlers.
 * Mirrors {@link THOpportunityRegistry} for claim interception.
 */
public class THClaimHandlerRegistry {
    private static final Logger log = Global.getLogger(THClaimHandlerRegistry.class);
    private final List<ITHClaimHandler> handlers = new ArrayList<>();

    public void register(ITHClaimHandler handler) {
        handlers.add(handler);
        log.info("Registered claim handler: " + handler.getClass().getSimpleName());
    }

    public List<ITHClaimHandler> getAll() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * Pick a random claim handler weighted by {@link ITHClaimHandler#getProbabilityWeight()}.
     * Returns null if no handlers are registered or all have zero weight.
     */
    public ITHClaimHandler pickCandidate() {
        WeightedRandomPicker<ITHClaimHandler> picker = new WeightedRandomPicker<>();
        for (ITHClaimHandler handler : handlers) {
            float weight = handler.getProbabilityWeight();
            if (weight > 0) {
                picker.add(handler, weight);
            }
        }
        return picker.pick();
    }
}
