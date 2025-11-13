package spinloki.TreasureHunt.campaign.intel.opportunities;

import spinloki.TreasureHunt.campaign.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.Random;

public class THSectorSprintOpportunity extends BaseTHOpportunity{
    private final int numIntelsToCreate = 3;

    @Override
    public void trigger() {
        super.trigger();
        int time = 90;
        for (var system : THUtils.getRandomUninhabitedSystemsWithStablePoints(numIntelsToCreate)){
            new THSectorSprintIntel(system, time + new Random().nextInt(9) - 4);
            time -= 30;
        }
    }
}
