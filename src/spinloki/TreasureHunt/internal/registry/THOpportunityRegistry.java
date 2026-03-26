package spinloki.TreasureHunt.internal.registry;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.api.ITHOpportunity;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for treasure hunt opportunities.
 * Replaces hard-coded opportunity setup in TreasureHuntEventIntel.
 */
public class THOpportunityRegistry {
    private static final Logger log = Global.getLogger(THOpportunityRegistry.class);
    private final List<ITHOpportunity> opportunities = new ArrayList<>();

    public void register(ITHOpportunity opportunity) {
        opportunities.add(opportunity);
        log.info("Registered opportunity: " + opportunity.getClass().getSimpleName());
    }

    public List<ITHOpportunity> getAll() {
        return Collections.unmodifiableList(opportunities);
    }

    /**
     * Pick a random opportunity weighted by {@link ITHOpportunity#getProbabilityWeight()}.
     * Returns null if no opportunities are registered or all have zero weight.
     */
    public ITHOpportunity pickCandidate() {
        WeightedRandomPicker<ITHOpportunity> picker = new WeightedRandomPicker<>();
        for (ITHOpportunity opp : opportunities) {
            float weight = opp.getProbabilityWeight();
            if (weight > 0) {
                picker.add(opp, weight);
            }
        }
        return picker.pick();
    }
}
