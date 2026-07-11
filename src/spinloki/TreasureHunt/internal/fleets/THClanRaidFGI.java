package spinloki.TreasureHunt.internal.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.factors.THRaidLossFactor;
import spinloki.TreasureHunt.util.THUtils;

public class THClanRaidFGI extends GenericRaidFGI {

    public THClanRaidFGI(GenericRaidParams params) {
        super(params);
    }

    @Override
    protected void configureFleet(int index, CampaignFleetAPI fleet) {
        super.configureFleet(index, fleet);
        if (fleet != null) {
            fleet.getMemoryWithoutUpdate().set(THUtils.MEMORY_KEY_TH_SCAVENGER, true);
        }
    }

    @Override
    public boolean hasCustomRaidAction() {
        return true;
    }

    @Override
    public void doCustomRaidAction(CampaignFleetAPI fleet, MarketAPI market, float raidStr) {
        Industry techmining = market.getIndustry(Industries.TECHMINING);
        if (techmining != null) {
            float durMult = Global.getSettings().getFloat("punitiveExpeditionDisruptDurationMult");
            new MarketCMD(market.getPrimaryEntity()).doIndustryRaid(getFaction(), raidStr, techmining, durMult);
        } else {
            new MarketCMD(market.getPrimaryEntity()).doGenericRaid(getFaction(), raidStr,
                    getParams().raidParams.maxStabilityLostPerRaid, false);
        }

        int penalty = market.getSize() * 10;
        TreasureHuntEventIntel.addFactorCreateIfNecessary(new THRaidLossFactor(penalty, market.getName()), null);
    }
}
