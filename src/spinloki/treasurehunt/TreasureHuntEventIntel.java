package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.EnumSet;

public class TreasureHuntEventIntel extends BaseEventIntel {
    public static Color BAR_COLOR = Global.getSettings().getColor("progressBarFleetPointsColor");
    public static int PROGRESS_MAX = 500;
    public static int PROGRESS_1 = 100;
    public static int PROGRESS_2 = 300;

    public static String KEY = "$treasure_hunt_event_ref";

    public static enum Stage {
        START,
        CHOOSE,
        OPPORTUNITY,
        FOUND
    }

    public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
        if (get() == null) {
            //TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
            //new HyperspaceTopographyEventIntel(text);
            // adding a factor anyway, so it'll show a message - don't need to double up
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

    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("treasure_hunt_events", "hunt_begins");
    }

    @Override
    protected String getStageIconImpl(Object stageId) {
        EventStageData esd = getDataFor(stageId);
        if (esd == null) return null;
        if (stageId == Stage.CHOOSE){
            return Global.getSettings().getSpriteName("treasure_hunt_events", "found_lead");
        }
        if (stageId == Stage.OPPORTUNITY){
            return Global.getSettings().getSpriteName("treasure_hunt_events", "found_opportunity");
        }
        if (stageId == Stage.FOUND){
            return Global.getSettings().getSpriteName("treasure_hunt_events", "found_treasure");
        }
        return Global.getSettings().getSpriteName("treasure_hunt_events", "hunt_begins");
    }

    @Override
    public Color getBarColor() {
        Color color = BAR_COLOR;
        //color = Misc.getBasePlayerColor();
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

//		if (isStageActiveAndLast(stageId) &&  stageId == Stage.START) {
//			addStageDesc(info, stageId, small, false);
//		} else if (isStageActive(stageId) && stageId != Stage.START) {
//			addStageDesc(info, stageId, small, false);
//		}

        if (isStageActive(stageId)) {
            addStageDesc(info, stageId, small, false);
        }
    }

    public void addStageDesc(TooltipMakerAPI info, Object stageId, float initPad, boolean forTooltip) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        if (stageId == Stage.START) {
            info.addPara("MOTHERFUCKING TREASURE HUNT!!!",
                    initPad);
        }
        else if (stageId == Stage.CHOOSE){
            info.addPara("YOU GET TO CHOOSE THE THING! DAMN!", initPad);
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
//		if (stageId == Stage.REVERSE_POLARITY || stageId == Stage.GENERATE_SLIPSURGE) {
//			return 16f;
//		}
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
                        tooltip.addTitle("Found a lead");
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
                info.addPara("You have a choice! Damn!", tc, initPad);
            }
            if (esd.id == Stage.OPPORTUNITY) {
                info.addPara("You have an opportunity! Damn!", tc, initPad);
            }
            if (esd.id == Stage.FOUND) {
                info.addPara("You found the treasure! Damn!", tc, initPad);
            }
            return;
        }

//		EventStageData esd = getLastActiveStage(false);
//		if (esd != null && EnumSet.of(Stage.START, Stage.HA_1, Stage.HA_2, Stage.HA_3, Stage.HA_4).contains(esd.id)) {
//
//		}
    }
}
