package spinloki.TreasureHunt.campaign.intel.opportunities;

import spinloki.TreasureHunt.campaign.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.util.THUtils;

public class THSectorSprintOpportunity implements THOpportunity{
    private final float probabilityWeight = 1f; // Standard likelihood
    private final int numIntelsToCreate = 3;
    private int timesTriggered = 0;

    @Override
    public float getProbabilityWeight() {
        return probabilityWeight / (1 + timesTriggered);
    }

    @Override
    public void trigger() {
        timesTriggered++;
        for (var system : THUtils.getRandomUninhabitedSystemsWithStablePoints(numIntelsToCreate)){
            new THSectorSprintIntel(system);
        }
    }
}
