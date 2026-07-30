package spinloki.TreasureHunt.internal.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.factors.THClanRivalryFactor;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class THClanRivalryIntel extends BaseIntelPlugin {

    private final String icon;

    public THClanRivalryIntel(String icon) {
        this.icon = icon;
        Global.getSector().getIntelManager().addIntel(this);
    }

    public static THClanRivalryIntel get() {
        for (var intel : Global.getSector().getIntelManager().getIntel(THClanRivalryIntel.class)) {
            return (THClanRivalryIntel) intel;
        }
        return null;
    }

    public static THClanRivalryIntel getOrCreate() {
        THClanRivalryIntel existing = get();
        if (existing != null) return existing;
        String icon = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, "clanner_rivalry");
        return new THClanRivalryIntel(icon);
    }

    private List<MarketAPI> getContestedColonies() {
        List<MarketAPI> result = new ArrayList<>();
        Set<String> cleared = THUtils.getClanClearedColonies();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!THUtils.isRivalryRuinsColony(market)) continue;
            if (cleared.contains(market.getId())) continue;
            result.add(market);
        }
        return result;
    }

    private List<MarketAPI> getClearedColonies() {
        List<MarketAPI> result = new ArrayList<>();
        Set<String> cleared = THUtils.getClanClearedColonies();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!THUtils.isRivalryRuinsColony(market)) continue;
            if (cleared.contains(market.getId())) result.add(market);
        }
        return result;
    }

    private int getBonusPercent() {
        return Math.round((THRegistry.getSettings().getClanRivalryCapMult() - 1f) * 100f);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        info.addPara(getName(), getTitleColor(mode), 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate,
                                   Color tc, float initPad) {
        Color h = Misc.getHighlightColor();

        if (mode == ListInfoMode.MESSAGES) {
            info.addPara("Salvor clans covet your ruins", initPad);
            return;
        }

        info.addPara("Monthly progress limit raised by %s", initPad, tc, h, getBonusPercent() + "%");

        int contested = getContestedColonies().size();
        if (contested > 0) {
            info.addPara("%s colonies targeted by the clans", 0f, tc, h, String.valueOf(contested));
        } else {
            info.addPara("No colonies currently targeted", 0f);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color p = Misc.getBasePlayerColor();
        Color pDark = Misc.getDarkPlayerColor();

        info.addPara("Word has spread through the salvor clans that you are stripping the great ruins "
                + "for yourself. They consider that haul theirs by right of the wandering, and they are "
                + "willing to come and collect. Your own agents, unwilling to be outpaced by nomads, "
                + "have redoubled their efforts.", 0f);

        info.addPara("Your treasure hunt can now exceed its usual monthly progress limit by %s.",
                opad, h, getBonusPercent() + "%");

        info.addPara("Each of your colonies on extensive or vast ruins draws clan attention as a "
                + "colony crisis. Defeating a clan raid ends their interest in that colony permanently.", opad);

        List<MarketAPI> contested = getContestedColonies();
        if (!contested.isEmpty()) {
            info.addSectionHeading("Contested Colonies", p, pDark, Alignment.MID, opad);
            for (MarketAPI market : contested) {
                info.addPara("• %s (%s)", 3f, h,
                        market.getName(), describeRuins(market));
            }
        }

        List<MarketAPI> cleared = getClearedColonies();
        if (!cleared.isEmpty()) {
            info.addSectionHeading("Clans Driven Off", p, pDark, Alignment.MID, opad);
            for (MarketAPI market : cleared) {
                info.addPara("• %s", 3f, h, market.getName());
            }
        }
    }

    private String describeRuins(MarketAPI market) {
        return THUtils.isVastRuins(market) ? "vast ruins" : "extensive ruins";
    }

    @Override
    public String getName() {
        return "Clanner Rivalry";
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String getSortString() {
        return getName();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(THUtils.TH_TAG);
        var clan = Global.getSector().getFaction(THClanRivalryFactor.FACTION_ID);
        if (clan != null) tags.add(clan.getId());
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        List<MarketAPI> contested = getContestedColonies();
        if (contested.isEmpty()) return null;
        return contested.get(0).getPrimaryEntity();
    }
}
