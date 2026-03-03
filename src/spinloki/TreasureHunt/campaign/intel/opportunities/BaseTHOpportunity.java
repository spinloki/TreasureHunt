package spinloki.TreasureHunt.campaign.intel.opportunities;

public abstract class BaseTHOpportunity implements THOpportunity{
    protected int timesTriggered = 0;
    protected final float probabilityWeight = 1f; // Standard likelihood

    // Opportunities grow less likely the more they are triggered by default
    // So for example, getting the same Opportunity where all is equal three times in a row between 2 is 1 in 8
    // but for this distribution, it's a little over 1 in 100
    @Override
    public float getProbabilityWeight() {
        float denominator = (1 + timesTriggered);
        return probabilityWeight / (denominator * denominator);
    }

    @Override
    public void trigger() {
        timesTriggered++;
    }
}
