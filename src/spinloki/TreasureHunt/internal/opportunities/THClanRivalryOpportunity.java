package spinloki.TreasureHunt.internal.opportunities;

import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.intel.THClanRivalryIntel;
import spinloki.TreasureHunt.util.THUtils;

public class THClanRivalryOpportunity extends BaseTHOpportunity {

    @Override
    public float getProbabilityWeight() {
        if (THUtils.isClanRivalryActive()) return 0;
        if (!THUtils.isClanRivalryGateMet()) return 0;
        return super.getProbabilityWeight();
    }

    @Override
    public void trigger() {
        super.trigger();
        if (THUtils.isClanRivalryActive()) return;
        THUtils.setClanRivalryActive();
        THClanRivalryIntel.getOrCreate();
        THFactorTracker.syncClanRivalryFactors();
    }

    @Override
    public String getDisplayName() {
        return "Clanner Rivalry";
    }

    @Override
    public String getIcon() {
        return "clanner_rivalry";
    }
}
