package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityCause2;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.Stage;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.internal.fleets.THClanPiracyScript;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.Color;
import java.util.Random;

public class THClanRivalryFactor extends BaseHostileActivityFactor {

    public static final String FACTION_ID = "salvor_clan";

    public THClanRivalryFactor(HostileActivityEventIntel intel) {
        super(intel);
    }

    @Override
    public String getId() {
        return "th_clan_rivalry";
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "Clanner Rivalry";
    }

    @Override
    public String getProgressStr(BaseEventIntel intel) {
        return "";
    }

    @Override
    public Color getDescColor(BaseEventIntel intel) {
        if (getProgress(intel) <= 0) return Misc.getGrayColor();
        return Global.getSector().getFaction(Factions.INDEPENDENT).getBaseUIColor();
    }

    @Override
    public String getNameForThreatList(boolean first) {
        return first ? "Salvor clans" : "salvor clans";
    }

    @Override
    public Color getNameColorForThreatList() {
        return Global.getSector().getFaction(Factions.INDEPENDENT).getBaseUIColor();
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                tooltip.addPara("Salvor clans resent your excavation of the great ruins and are massing "
                        + "to take what they can carry.", 0f);
                tooltip.addPara("Each of your colonies on extensive or vast ruins draws them. Defeating "
                        + "a raid ends their interest in that colony for good.", opad);
            }
        };
    }

    @Override
    public int getMaxNumFleets(StarSystemAPI system) {
        return getEffectMagnitude(system) > 0f ? 2 : 0;
    }

    @Override
    public CampaignFleetAPI createFleet(StarSystemAPI system, Random random) {
        int difficulty = 1 + Math.round(getEffectMagnitude(system) * 5f) + random.nextInt(3);

        FleetCreatorMission m = new FleetCreatorMission(random);
        m.beginFleet();
        m.createStandardFleet(difficulty, FACTION_ID, system.getLocation());
        m.triggerSetFleetComposition(0.4f, 0.3f, 0.2f, 0f, 0.2f);
        m.triggerMakeLowRepImpact();

        CampaignFleetAPI fleet = m.createFleet();
        if (fleet == null) return null;

        fleet.getMemoryWithoutUpdate().set(THUtils.MEMORY_KEY_TH_SCAVENGER, true);
        fleet.addScript(new THClanPiracyScript(fleet));
        return fleet;
    }

    @Override
    public float getEventFrequency(HostileActivityEventIntel intel, EventStageData stage) {
        if (stage.id != Stage.HA_EVENT) return 0f;
        return pickTarget(getRandomizedStageRandom()) == null ? 0f : 10f;
    }

    @Override
    public void rollEvent(HostileActivityEventIntel intel, EventStageData stage) {
        MarketAPI target = pickTarget(getRandomizedStageRandom());
        if (target == null) return;

        HAERandomEventData data = new HAERandomEventData(this, stage);
        data.custom = target;
        stage.rollData = data;
        intel.sendUpdateIfPlayerHasIntel(data, false);
    }

    @Override
    public boolean fireEvent(HostileActivityEventIntel intel, EventStageData stage) {
        MarketAPI target = getRolledTarget(stage);
        stage.rollData = null;
        if (target == null || target.getStarSystem() == null) return false;
        return THClanRaidFGI.launchAgainst(target, 1f) != null;
    }

    private MarketAPI getRolledTarget(EventStageData stage) {
        if (!(stage.rollData instanceof HAERandomEventData data)) return null;
        if (!(data.custom instanceof MarketAPI target)) return null;
        if (!THUtils.isRivalryRuinsColony(target)) return null;
        if (THUtils.getClanClearedColonies().contains(target.getId())) return null;
        return target;
    }

    private MarketAPI pickTarget(Random random) {
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>(random);
        for (HostileActivityCause2 cause : getCauses()) {
            if (!(cause instanceof THClanRivalryCause curr)) continue;
            MarketAPI market = curr.getMarket();
            if (market == null || market.getStarSystem() == null) continue;
            picker.add(market, curr.getProgress());
        }
        return picker.pick();
    }

    @Override
    public String getEventStageIcon(HostileActivityEventIntel intel, EventStageData stage) {
        return Global.getSector().getFaction(FACTION_ID).getCrest();
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(HostileActivityEventIntel intel, EventStageData stage) {
        if (stage.id != Stage.HA_EVENT) return null;
        return getDefaultEventTooltip("Salvor clan raid", intel, stage);
    }

    @Override
    public void addBulletPointForEvent(HostileActivityEventIntel intel, EventStageData stage,
                                       TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate,
                                       Color tc, float initPad) {
        MarketAPI target = getRolledTarget(stage);
        info.addPara("Salvor clan raid massing"
                + (target == null ? "" : " against " + target.getName()), tc, initPad);
    }

    @Override
    public void addBulletPointForEventReset(HostileActivityEventIntel intel, EventStageData stage,
                                            TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate,
                                            Color tc, float initPad) {
        info.addPara("Salvor clan raid dispersed", tc, initPad);
    }

    @Override
    public void addStageDescriptionForEvent(HostileActivityEventIntel intel, EventStageData stage,
                                            TooltipMakerAPI info) {
        MarketAPI target = getRolledTarget(stage);
        String name = target == null ? "one of your colonies" : target.getName();
        info.addPara("Salvor clan forces are gathering to raid " + name
                + ". Destroy their supply detachment or break their lead fleet to drive them off.", 0f);
    }
}
