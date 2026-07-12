package spinloki.TreasureHunt.internal.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.factors.THRaidLossFactor;
import spinloki.TreasureHunt.util.THUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class THClanRaidFGI extends GenericRaidFGI implements FleetEventListener {

    public static final String MEM_ROLE = "$th_raid_role";
    public static final String ROLE_MAIN = "main";
    public static final String ROLE_SUPPORT = "support";
    public static final String ROLE_SUPPLY = "supply";

    private Set<String> supplyFleetIds = new HashSet<>();
    private Set<String> mainFleetIds = new HashSet<>();
    private Set<String> supplyKilledIds = new HashSet<>();
    private boolean rewarded = false;

    public THClanRaidFGI(GenericRaidParams params) {
        super(params);
    }

    @Override
    protected void configureFleet(int size, CampaignFleetAPI fleet) {
        super.configureFleet(size, fleet);
        if (fleet == null) return;

        fleet.getMemoryWithoutUpdate().set(THUtils.MEMORY_KEY_TH_SCAVENGER, true);

        String role = roleForSize(size);
        fleet.getMemoryWithoutUpdate().set(MEM_ROLE, role);
        if (ROLE_SUPPLY.equals(role)) {
            supplyFleetIds.add(fleet.getId());
            fleet.getEventListeners().add(this);
            fleet.setName("Supply Detachment");
        } else if (ROLE_MAIN.equals(role)) {
            mainFleetIds.add(fleet.getId());
            fleet.getEventListeners().add(this);
            fleet.setName("Raider Detachment");
        } else {
            fleet.setName("Escort Detachment");
        }
    }

    private String roleForSize(int size) {
        List<Integer> sizes = getParams().fleetSizes;
        if (sizes == null || sizes.isEmpty()) return ROLE_SUPPORT;
        int max = Collections.max(sizes);
        int min = Collections.min(sizes);
        if (size >= max) return ROLE_MAIN;
        if (size <= min) return ROLE_SUPPLY;
        return ROLE_SUPPORT;
    }

    @Override
    protected boolean shouldAbort() {
        if (super.shouldAbort()) return true;
        return !supplyFleetIds.isEmpty() && supplyKilledIds.containsAll(supplyFleetIds);
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (fleet == null || reason != FleetDespawnReason.DESTROYED_BY_BATTLE) return;
        String id = fleet.getId();
        if (mainFleetIds.contains(id) && !rewarded) {
            grantReward();
            rewarded = true;
        }
        if (supplyFleetIds.contains(id)) {
            supplyKilledIds.add(id);
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    }

    private void grantReward() {
        TreasureHuntEventIntel intel = TreasureHuntEventIntel.get();
        if (intel == null) return;
        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
        for (String itemId : intel.getRandomRewardItems(1)) {
            pf.getCargo().addSpecial(new SpecialItemData(itemId, null), 1);
            intel.removeRewardItemFromPool(itemId);
            Global.getSector().getCampaignUI().addMessage(
                    "Recovered " + THUtils.getSpecialItemDisplayName(itemId)
                            + " from the defeated clan raiders.", Misc.getHighlightColor());
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
