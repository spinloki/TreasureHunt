package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;

public class THClanRivalryCause extends BaseHostileActivityCause2 {

    private static final int PROGRESS_VAST = 4;
    private static final int PROGRESS_EXTENSIVE = 2;
    private static final float MAG_VAST = 0.6f;
    private static final float MAG_EXTENSIVE = 0.35f;

    private final String marketId;

    public THClanRivalryCause(HostileActivityEventIntel intel, MarketAPI market) {
        super(intel);
        this.marketId = market.getId();
    }

    public String getMarketId() {
        return marketId;
    }

    public MarketAPI getMarket() {
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (marketId.equals(market.getId())) return market;
        }
        return null;
    }

    public boolean isStillContested() {
        return getProgress() > 0;
    }

    @Override
    public int getProgress() {
        MarketAPI market = getMarket();
        if (!THUtils.isRivalryRuinsColony(market)) return 0;
        if (THUtils.getClanClearedColonies().contains(marketId)) return 0;
        return THUtils.isVastRuins(market) ? PROGRESS_VAST : PROGRESS_EXTENSIVE;
    }

    @Override
    public String getDesc() {
        MarketAPI market = getMarket();
        return "Ruins excavation at " + (market == null ? "a lost colony" : market.getName());
    }

    @Override
    public float getMagnitudeContribution(StarSystemAPI system) {
        MarketAPI market = getMarket();
        if (market == null || market.getStarSystem() != system) return 0f;
        if (getProgress() <= 0) return 0f;
        return THUtils.isVastRuins(market) ? MAG_VAST : MAG_EXTENSIVE;
    }

    @Override
    public TooltipCreator getTooltip() {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                MarketAPI market = getMarket();
                String name = market == null ? "your colony" : market.getName();
                String ruins = market != null && THUtils.isVastRuins(market) ? "vast" : "extensive";

                tooltip.addPara("The salvor clans consider the %s ruins at %s theirs by right of the "
                                + "wandering, and resent your digging into them.", 0f,
                        Misc.getHighlightColor(), ruins, name);
                tooltip.addPara("Driving off a clan raid against this colony ends their interest in it "
                        + "for good. Letting a raid succeed does not.", opad);
            }
        };
    }
}
