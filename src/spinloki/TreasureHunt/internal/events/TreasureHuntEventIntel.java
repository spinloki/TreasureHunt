package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.intel.THFoundTreasureIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public class TreasureHuntEventIntel extends BaseEventIntel {
    private THTreasurePicker treasurePicker;
    private String treasure;
    private String opportunityIcon;
    private transient CustomPanelAPI currentPanel;

    private static final String category = "treasure_hunt_events";
    private static final String BUTTON_ABANDON = "abandon_hunt";

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
                message += ": " + THUtils.getSpecialItemDisplayName(treasure) + " location discovered.";
                info.addPara(message, tc, initPad);
            }
        }
    }

    void pickTreasureFromCandidates(Set<String> candidates, String picked){
        if (picked == null || !candidates.contains(picked)){
            treasurePicker.removeItemsFromPool(candidates);
            return;
        }
        treasurePicker.removeItemFromPool(picked);
        treasure = picked;
    }

    public Set<String> getRandomRewardItems(int count) {
        return treasurePicker.getRandomUnseenItems(count);
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        currentPanel = panel;
        super.createLargeDescription(panel, width, height);
    }

    @Override
    public void afterStageDescriptions(TooltipMakerAPI info) {
        float opad = 10f;
        float rowHeight = 24f;

        Color textColor = new Color(235, 200, 80);
        Color bgColor = new Color(60, 50, 20);

        Set<String> oneTime = treasurePicker.getOneTimeCandidates();
        Set<String> repeatable = treasurePicker.getRepeatableCandidates();

        boolean showOneTime = !oneTime.isEmpty();
        float barWidth = getBarWidth();
        float gap = 10f;

        if (showOneTime) {
            float halfWidth = (barWidth - gap) / 2f;

            CustomPanelAPI row = currentPanel.createCustomPanel(barWidth, rowHeight, null);

            TooltipMakerAPI left = row.createUIElement(halfWidth, rowHeight, false);
            left.addSectionHeading("Remaining one-time treasures (" + oneTime.size() + ")",
                    textColor, bgColor, Alignment.MID, 0f);
            left.addTooltipToPrevious(createPoolTooltip("Remaining one-time treasures",
                    "These treasures can only be awarded once. This pool will not be refilled.", oneTime),
                    TooltipMakerAPI.TooltipLocation.BELOW);
            row.addUIElement(left).inTL(0, 0);

            TooltipMakerAPI right = row.createUIElement(halfWidth, rowHeight, false);
            right.addSectionHeading("Remaining repeatable treasures (" + repeatable.size() + ")",
                    textColor, bgColor, Alignment.MID, 0f);
            right.addTooltipToPrevious(createPoolTooltip("Remaining repeatable treasures",
                    "These treasures can be awarded multiple times. This pool is refilled when emptied.", repeatable),
                    TooltipMakerAPI.TooltipLocation.BELOW);
            row.addUIElement(right).inTR(0, 0);

            info.addCustom(row, opad);
        } else {
            CustomPanelAPI row = currentPanel.createCustomPanel(barWidth, rowHeight, null);

            TooltipMakerAPI full = row.createUIElement(barWidth, rowHeight, false);
            full.addSectionHeading("Remaining repeatable treasures (" + repeatable.size() + ")",
                    textColor, bgColor, Alignment.MID, 0f);
            full.addTooltipToPrevious(createPoolTooltip("Remaining repeatable treasures",
                    "These treasures can be awarded multiple times. This pool is refilled when emptied.", repeatable),
                    TooltipMakerAPI.TooltipLocation.BELOW);
            row.addUIElement(full).inTL(0, 0);

            info.addCustom(row, opad);
        }

        if (getProgress() > 0) {
            Color abandonBase = new Color(150, 50, 50);
            Color abandonDark = new Color(80, 25, 25);
            addGenericButton(info, getBarWidth(), abandonBase, abandonDark, "Abandon Hunt", BUTTON_ABANDON);
        }
    }

    @Override
    public boolean doesButtonHaveConfirmDialog(Object buttonId) {
        if (BUTTON_ABANDON.equals(buttonId)) return true;
        return super.doesButtonHaveConfirmDialog(buttonId);
    }

    @Override
    public void createConfirmationPrompt(Object buttonId, TooltipMakerAPI prompt) {
        if (BUTTON_ABANDON.equals(buttonId)) {
            prompt.addPara("This will reset all hunt progress to zero and clear your current lead. Are you sure?", 0f,
                    Misc.getNegativeHighlightColor(), "reset all hunt progress to zero");
            return;
        }
        super.createConfirmationPrompt(buttonId, prompt);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_ABANDON.equals(buttonId)) {
            setProgress(0);
            treasure = "";
            ui.updateUIForItem(this);
            return;
        }
        super.buttonPressConfirmed(buttonId, ui);
    }

    private TooltipMakerAPI.TooltipCreator createPoolTooltip(String title, String description, Set<String> pool) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addTitle(title);
                tooltip.addPara(description, 10f);
                if (pool.isEmpty()) {
                    tooltip.addPara("Pool is empty - will be refilled.", 10f);
                    return;
                }
                java.util.List<String> sorted = new java.util.ArrayList<>(pool);
                sorted.sort((a, b) -> THUtils.getSpecialItemDisplayName(a)
                        .compareToIgnoreCase(THUtils.getSpecialItemDisplayName(b)));
                for (String itemId : sorted) {
                    tooltip.addPara("  - " + THUtils.getSpecialItemDisplayName(itemId), 2f);
                }
            }
        };
    }

    protected void notifyStageReached(EventStageData stage){
        if (stage.id == Stage.CHOOSE) {
            var script = new THChooseLeadScript(
                    ()->setProgress(0),
                    (int count)->treasurePicker.getRandomUnseenItems(count),
                    this::pickTreasureFromCandidates
            );
            Global.getSector().addScript(script);
        }
        if (stage.id == Stage.OPPORTUNITY){
            ITHOpportunity opportunity = THRegistry.getOpportunityRegistry().pickCandidate();
            if (opportunity != null) {
                opportunity.trigger();
                opportunityIcon = opportunity.getIcon();
            }
        }
        if (stage.id == Stage.FOUND){
            setProgress(0);
            if (!treasure.isEmpty() && Global.getSettings().getSpecialItemSpec(treasure) != null) {
                new THFoundTreasureIntel(treasure);
            }
            treasure = "";
        }
    }
}
