package spinloki.treasurehunt.data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.treasurehunt.config.Settings;
import spinloki.treasurehunt.util.THUtils;

public class THTreasureHuntPackage extends BaseLogisticsHullMod {

    String TH_TREASURE_HUNT_BOOST = "th_treasure_hunt_boost";

    private static Map mag = new HashMap();
    static {
        mag.put(HullSize.FRIGATE, 1f);
        mag.put(HullSize.DESTROYER, 2f);
        mag.put(HullSize.CRUISER, 4f);
        mag.put(HullSize.CAPITAL_SHIP, 8f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        boolean sMod = isSMod(stats);

        float mod = (Float) mag.get(hullSize);
        if (sMod) {
            mod *= 1f + (Settings.TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS / 100f);
        }

        stats.getDynamic().getMod(THUtils.TH_TREASURE_HUNT_BOOST).modifyFlat(id, mod);
    }

    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) Settings.TH_TREASURE_HUNT_PACKAGE_SMOD_PERCENT_BONUS.intValue() + "%";
        return null;
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
        if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
        if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
        if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
        if (index == 4) return "" + Settings.TH_TREASURE_HUNT_PACKAGE_MAX_MULT;

        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (isForModSpec || ship == null) return;
        if (Global.getSettings().getCurrentState() == GameState.TITLE) return;

        float opad = 10f;
        Color h = Misc.getHighlightColor();

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int boost = (int) Misc.getFleetwideTotalMod(fleet, THUtils.TH_TREASURE_HUNT_BOOST, 0, ship);

        tooltip.addPara("The treasure hunt equipment in your fleet increases event progress by %s.",
                opad, h,
                "" + boost
        );
    }
}

