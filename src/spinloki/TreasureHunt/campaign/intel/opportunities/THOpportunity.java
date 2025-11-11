package spinloki.TreasureHunt.campaign.intel.opportunities;

public interface THOpportunity {
    float getProbabilityWeight();
    void trigger();
}
