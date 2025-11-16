package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RuinsFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ScavengerFleetAssignmentAI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.THFoundTreasureIntel;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;
import spinloki.TreasureHunt.campaign.intel.opportunities.THScavengerSwarmOpportunity;
import spinloki.TreasureHunt.campaign.intel.opportunities.THStationLeadOpportunity;
import spinloki.TreasureHunt.campaign.intel.opportunities.THSectorSprintOpportunity;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.EnumSet;

public class TreasureHuntEventIntel extends BaseEventIntel {
    private THTreasurePicker treasurePicker;
    private THOpportunityPicker opportunityPicker;
    private String treasure;
    private String opportunityIcon;

    private static final String category = "treasure_hunt_events";

    public static Color BAR_COLOR = Global.getSettings().getColor("progressBarFleetPointsColor");
    public static int PROGRESS_MAX = 500;
    public static int PROGRESS_1 = 100;
    public static int PROGRESS_2 = 300;

    public static String ABANDON_LEAD = "th_abandon_lead";

    public static String KEY = "$treasure_hunt_event_ref";

    public static enum Stage {
        START,
        CHOOSE,
        OPPORTUNITY,
        FOUND
    }

    public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
        if (get() == null) {
            new TreasureHuntEventIntel(null, false);
        }
        if (get() != null) {
            get().addFactor(factor, dialog);
        }
    }

    public static TreasureHuntEventIntel get() {
        return (TreasureHuntEventIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }

    public TreasureHuntEventIntel(TextPanelAPI text, boolean withIntelNotification) {
        super();

        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);

        setup();

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
    }

    protected void setup() {
        factors.clear();
        stages.clear();

        setMaxProgress(PROGRESS_MAX);

        addStage(Stage.START, 0);
        addStage(Stage.CHOOSE, PROGRESS_1, StageIconSize.MEDIUM);
        addStage(Stage.OPPORTUNITY, PROGRESS_2, StageIconSize.MEDIUM);
        addStage(Stage.FOUND, PROGRESS_MAX, StageIconSize.MEDIUM);

        getDataFor(Stage.START).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.CHOOSE).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.OPPORTUNITY).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.FOUND).keepIconBrightWhenLaterStageReached = true;

        treasurePicker = new THTreasurePicker();
        treasure = "";

        opportunityPicker = new THOpportunityPicker();
        opportunityPicker.addCandidate(new THSectorSprintOpportunity());
        opportunityPicker.addCandidate(new THStationLeadOpportunity());
        opportunityPicker.addCandidate(new THScavengerSwarmOpportunity());

        if (!Global.getSector().getPlayerFleet().hasAbility(ABANDON_LEAD)){
            Global.getSector().getPlayerFleet().addAbility(ABANDON_LEAD);
        }
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName(category, "hunt_begins");
    }

    @Override
    protected String getStageIconImpl(Object stageId) {
        EventStageData esd = getDataFor(stageId);
        if (esd == null) return null;
        if (stageId == Stage.CHOOSE){
            var spec = Global.getSettings().getSpecialItemSpec(treasure);
            if (spec != null){
                return spec.getIconName();
            }
            return Global.getSettings().getSpriteName(category, "found_lead");
        }
        if (stageId == Stage.OPPORTUNITY){
            return opportunityIcon;
        }
        if (stageId == Stage.FOUND){
            return Global.getSettings().getSpriteName(category, "found_treasure");
        }
        return Global.getSettings().getSpriteName(category, "hunt_begins");
    }

    @Override
    public Color getBarColor() {
        Color color = BAR_COLOR;
        color = Misc.interpolateColor(color, Color.black, 0.25f);
        return color;
    }

    @Override
    public Color getBarProgressIndicatorColor() {
        return super.getBarProgressIndicatorColor();
    }

    @Override
    protected int getStageImportance(Object stageId) {
        return super.getStageImportance(stageId);
    }

    @Override
    protected String getName() {
        return "Treasure Hunt";
    }

    @Override
    public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
        float opad = 10f;
        float small = 0f;
        Color h = Misc.getHighlightColor();

        EventStageData stage = getDataFor(stageId);
        if (stage == null) return;
        if (isStageActive(stageId)) {
            addStageDesc(info, stageId, small, false);
        }
    }

    public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (stageId == Stage.START) {
            info.addPara(
                    "Rumors, half-deciphered logs, and sensor fragments all point toward valuable relics of the Domain "
                            + "still hidden throughout the sector.",
                    opad
            );
            info.addPara(
                    "Salvaging derelicts and ruins, interacting with other scavengers, and seizing the occasional opportunity "
                            + "will narrow down your search.",
                    opad
            );
            info.addPara(
                    "Outfitting your ships with the special Treasure Hunt Package hullmod can also greatly boost progress.",
                    opad
            );
            info.addPara("The Hunt awaits!", initPad);
        }
        else if (stageId == Stage.CHOOSE){
            String displayName = null;
            displayName = THUtils.getSpecialItemDisplayName(treasure);
            info.addPara(String.format("You have a lead on a %s", displayName), initPad);
        }
        else if (stageId == Stage.OPPORTUNITY){
            info.addPara("An opportunity has presented itself", initPad);
        }
    }

    @Override
    public float getImageSizeForStageDesc(Object stageId) {
        if (stageId == Stage.START) {
            return 64f;
        }
        return 48f;
    }

    @Override
    public float getImageIndentForStageDesc(Object stageId) {
        if (stageId == Stage.START) {
            return 0f;
        }
        return 16f;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(Object stageId) {
        TooltipMakerAPI.TooltipCreator result = null;
        final EventStageData esd = getDataFor(stageId);

        if ((esd != null) && EnumSet.of(Stage.CHOOSE, Stage.OPPORTUNITY, Stage.FOUND).contains(esd.id)) {
            result = new BaseFactorTooltip() {
                @Override
                public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                    float opad = 10f;

                    if (esd.id == Stage.CHOOSE) {
                        String displayName = null;
                        displayName = THUtils.getSpecialItemDisplayName(treasure);
                        tooltip.addTitle(String.format("Found a lead on a %s", displayName));
                    } else if (esd.id == Stage.OPPORTUNITY) {
                        tooltip.addTitle("Found an opportunity");
                    } else if (esd.id == Stage.FOUND) {
                        tooltip.addTitle("Location discovered");
                    }

                    addStageDesc(tooltip, esd.id, opad, true);

                    esd.addProgressReq(tooltip, opad);
                }
            };
        }

        return result;
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode, boolean isUpdate,
                                   Color tc, float initPad) {

        if (addEventFactorBulletPoints(info, mode, isUpdate, tc, initPad)) {
            return;
        }

        Color h = Misc.getHighlightColor();
        if (isUpdate && getListInfoParam() instanceof EventStageData) {
            EventStageData esd = (EventStageData) getListInfoParam();
            if (esd.id == Stage.CHOOSE) {
                info.addPara("You have a new lead", tc, initPad);
            }
            if (esd.id == Stage.OPPORTUNITY) {
                info.addPara("Opportunity found", tc, initPad);
            }
            if (esd.id == Stage.FOUND) {
                String message = "Treasure found";
                message += ": " + THUtils.getSpecialItemDisplayName(treasure) + " added to inventory.";
                info.addPara(message, tc, initPad);
            }
        }
    }

    protected void notifyStageReached(EventStageData stage){
        if (stage.id == Stage.CHOOSE) {
            treasure = treasurePicker.getRandomUnseenItem();
        }
        if (stage.id == Stage.OPPORTUNITY){
            var opportunity = opportunityPicker.pickCandidate();
            opportunity.trigger();
            opportunityIcon = opportunity.getIcon();
        }
        if (stage.id == Stage.FOUND){
            setProgress(0);
            new THFoundTreasureIntel(treasure);
            treasure = "";
        }
    }
}
