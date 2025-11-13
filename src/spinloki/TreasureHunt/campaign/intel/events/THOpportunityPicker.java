package spinloki.TreasureHunt.campaign.intel.events;

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

        float totalWeight = 0f;
        for (THOpportunity opp : opportunityCandidates) {
            totalWeight += Math.max(0, opp.getProbabilityWeight()); // ignore negatives
        }

        float random = (float) (Math.random() * totalWeight);
        float cumulative = 0f;

        for (THOpportunity opp : opportunityCandidates) {
            float probabilityWeight = opp.getProbabilityWeight();
            if (probabilityWeight <= 0){
                continue; // take no chances!
            }
            cumulative += probabilityWeight;
            if (random <= cumulative) {
                return opp;
            }
        }

        // fallback (shouldn’t happen unless rounding issues)
        return opportunityCandidates.get(opportunityCandidates.size() - 1);
    }
}
