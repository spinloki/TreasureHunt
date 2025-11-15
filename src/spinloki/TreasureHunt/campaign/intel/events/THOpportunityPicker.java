package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.campaign.intel.opportunities.THOpportunity;

import java.util.ArrayList;
import java.util.List;

public class THOpportunityPicker {
    private final List<THOpportunity> opportunityCandidates = new ArrayList<>();

    public void addCandidate(THOpportunity opportunity){
        opportunityCandidates.add(opportunity);
    }

    public void removeCandidate(THOpportunity opportunity){
        opportunityCandidates.remove(opportunity);
    }

    public THOpportunity pickCandidate() {
        if (opportunityCandidates.isEmpty()) {
            return null;
        }

        var picker = new WeightedRandomPicker<THOpportunity>();

        for (THOpportunity opp : opportunityCandidates) {
            if (opp.getProbabilityWeight() != 0){
                picker.add(opp, opp.getProbabilityWeight());
            }
        }

        return picker.pick();
    }
}
