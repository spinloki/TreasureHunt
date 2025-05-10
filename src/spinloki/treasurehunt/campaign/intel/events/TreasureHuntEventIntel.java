package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.treasurehunt.util.THUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static spinloki.treasurehunt.campaign.intel.events.THTreasurePicker.getShipBlueprintDisplayName;
import static spinloki.treasurehunt.campaign.intel.events.THTreasurePicker.getSpecialItemDisplayName;

public class TreasureHuntEventIntel extends BaseEventIntel {
    private THTreasurePicker picker;
    private String treasure;
    private THUtils.TreasureType treasureType = THUtils.TreasureType.ITEM;

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

        picker = new THTreasurePicker();
        treasure = "";

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
            if (treasureType == THUtils.TreasureType.ITEM){
                var spec = Global.getSettings().getSpecialItemSpec(treasure);
                if (spec != null){
                    return spec.getIconName();
                }
            }
            if (treasureType == THUtils.TreasureType.SHIP_BLUEPRINT){
                var spec = Global.getSettings().getSpecialItemSpec("ship_bp");
                if (spec != null){
                    return spec.getIconName();
                }
            }
            return Global.getSettings().getSpriteName(category, "found_lead");
        }
        if (stageId == Stage.OPPORTUNITY){
            return Global.getSettings().getSpriteName(category, "found_opportunity");
        }
        if (stageId == Stage.FOUND){
            return Global.getSettings().getSpriteName(category, "found_treasure");
        }
        return Global.getSettings().getSpriteName(category, "hunt_begins");
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
            info.addPara("The Hunt awaits!",
                    initPad);
        }
        else if (stageId == Stage.CHOOSE){
            String displayName = null;
            if (treasureType == THUtils.TreasureType.ITEM){
                displayName = getSpecialItemDisplayName(treasure);
            }
            if (treasureType == THUtils.TreasureType.SHIP_BLUEPRINT){
                displayName = getShipBlueprintDisplayName(treasure);
            }
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
                        if (treasureType == THUtils.TreasureType.ITEM){
                            displayName = getSpecialItemDisplayName(treasure);
                        }
                        if (treasureType == THUtils.TreasureType.SHIP_BLUEPRINT){
                            displayName = getShipBlueprintDisplayName(treasure);
                        }
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
                if (treasureType == THUtils.TreasureType.ITEM){
                    message += ": " + getSpecialItemDisplayName(treasure) + " added to inventory.";
                }
                if (treasureType == THUtils.TreasureType.SHIP_BLUEPRINT){
                    message += ": " + getShipBlueprintDisplayName(treasure) + " added to known ship blueprints.";
                }
                info.addPara(message, tc, initPad);
            }
        }
    }

    protected void notifyStageReached(EventStageData stage){
        if (stage.id == Stage.CHOOSE) {
            treasure = picker.getRandomUnseenItem();
            treasureType = picker.getLastChosenType();
        }
        if (stage.id == Stage.FOUND){
            setProgress(0);
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            if (treasureType == THUtils.TreasureType.ITEM){
                cargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(treasure, null), 1);
            }
            if (treasureType == THUtils.TreasureType.SHIP_BLUEPRINT){
                Global.getSector().getPlayerFaction().addKnownShip(treasure, true);
            }
            treasure = "";
        }
    }

    public void addAbility(String id) {
        if (Global.getSector().getPlayerFleet().hasAbility(id)) {
            return;
        }
        List<Misc.Token> params = new ArrayList<Misc.Token>();
        Misc.Token t = new Misc.Token(id, Misc.TokenType.LITERAL);
        params.add(t);
        t = new Misc.Token("-1", Misc.TokenType.LITERAL);
        params.add(t); // don't want to assign it to a slot - will assign as hyper-only alternate later here
        new AddAbility().execute(null, null, params, null);

        PersistentUIDataAPI.AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
        int curr = slots.getCurrBarIndex();
        OUTER: for (int i = 0; i < 5; i++) {
            slots.setCurrBarIndex(i);
            for (PersistentUIDataAPI.AbilitySlotAPI slot : slots.getCurrSlotsCopy()) {
                if (slot.getAbilityId().isEmpty()){
                    slot.setAbilityId(id);
                    break OUTER;
                }
            }
        }
        slots.setCurrBarIndex(curr);
    }
}
