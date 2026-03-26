package spinloki.TreasureHunt.internal.opportunities;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.api.ITHOpportunity;

import java.util.ArrayList;
import java.util.List;

public class THOpportunityPicker {
    private final List<ITHOpportunity> opportunityCandidates = new ArrayList<>();

    public void addCandidate(ITHOpportunity opportunity){
        opportunityCandidates.add(opportunity);
    }

    public void removeCandidate(ITHOpportunity opportunity){
        opportunityCandidates.remove(opportunity);
    }

    public ITHOpportunity pickCandidate() {
        if (opportunityCandidates.isEmpty()) {
            return null;
        }

        var picker = new WeightedRandomPicker<ITHOpportunity>();

        for (ITHOpportunity opp : opportunityCandidates) {
            if (opp.getProbabilityWeight() != 0){
                picker.add(opp, opp.getProbabilityWeight());
            }
        }

        return picker.pick();
    }
}
