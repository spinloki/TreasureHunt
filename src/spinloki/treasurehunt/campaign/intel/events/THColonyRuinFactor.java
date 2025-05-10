package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;

import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.util.Misc;

public class THColonyRuinFactor extends BaseEventFactor {
    MarketAPI colony;
    THColonyRuinFactor(MarketAPI colony){
        super();
        this.colony = colony;
    }

    @Override
    public boolean isExpired() {
        return (!Misc.hasRuins(colony));
    }
}
