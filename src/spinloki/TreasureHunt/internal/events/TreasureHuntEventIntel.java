package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.BaseOptionStoryPointActionDelegate;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetStoryOption.StoryOptionParams;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.api.ITHClaimHandler;
import spinloki.TreasureHunt.api.ITHUncappedFactor;
import spinloki.TreasureHunt.api.ITHOpportunity;
import spinloki.TreasureHunt.internal.intel.THFoundTreasureIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THRewardItem;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class TreasureHuntEventIntel extends BaseEventIntel {
    private THTreasurePicker treasurePicker;
    private String treasure;
    private String opportunityIcon;
    private String opportunityDisplayName;

    private static final String category = "treasure_hunt_events";
    private static final String BUTTON_ABANDON = "abandon_hunt";
    private static final String BUTTON_REGEN_ONE_TIME = "regen_one_time_pool";
    private static final String POOL_LABEL = "treasure_pool_label";

    private static final Color ABANDON_COLOR = new Color(235, 100, 100);

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

    @Override
    public void addFactor(EventFactor factor, InteractionDialogAPI dialog) {
        if (THRegistry.getSettings().isShowFactorNotifications()) {
            super.addFactor(factor, dialog);
        } else {
            // Add factor and adjust progress without sending a fleet log notification.
            // Stage-reached notifications are still sent by setProgress().
            factors.add(0, factor);
            setProgress(getProgress() + factor.getProgress(this));
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
        treasurePicker.registerListener();
        treasure = "";
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName(category, "hunt_begins");
    }

    @Override
    public int getMaxMonthlyProgress() {
        int base = THRegistry.getSettings().getMaxMonthlyProgress();
        if (THUtils.isClanRivalryActive()) {
            return Math.round(base * THRegistry.getSettings().getClanRivalryCapMult());
        }
        return base;
    }

    @Override
    public int getMonthlyProgress() {
        int capped = 0;
        int uncapped = 0;
        float mult = 1f;
        for (EventFactor factor : factors) {
            if (factor.isOneTime()) continue;
            int p = factor.getProgress(this);
            if (factor instanceof ITHUncappedFactor) {
                uncapped += p;
            } else {
                capped += p;
            }
            mult *= factor.getAllProgressMult(this);
        }

        // Apply mult to capped portion and enforce cap
        if (capped != 0) {
            float sign = Math.signum(capped);
            capped = Math.round(sign * Math.abs(capped) * mult);
            if (capped == 0) capped = (int) Math.round(sign);
        }
        capped = Math.min(capped, getMaxMonthlyProgress());

        // Apply mult to uncapped portion (no cap)
        if (uncapped != 0) {
            float sign = Math.signum(uncapped);
            uncapped = Math.round(sign * Math.abs(uncapped) * mult);
            if (uncapped == 0) uncapped = (int) Math.round(sign);
        }

        return capped + uncapped;
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
            if (opportunityIcon != null){
                return opportunityIcon;
            }
            return Global.getSettings().getSpriteName(category, "found_lead");
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
    protected String getName() {
        return "Treasure Hunt";
    }

    @Override
    public void addStageDescriptionText(TooltipMakerAPI info, float width, Object stageId) {
        float small = 0f;

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
            String displayName = THRewardItem.parse(treasure).getDisplayName();
            info.addPara(String.format("You have a lead on a %s", displayName), initPad);
        }
        else if (stageId == Stage.OPPORTUNITY){
            if (opportunityIcon != null && !opportunityIcon.isEmpty()){
                info.addPara(String.format("Opportunity found: %s", opportunityDisplayName), initPad);
            }
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
                        tooltip.addTitle("Lead found");
                    } else if (esd.id == Stage.OPPORTUNITY) {
                        tooltip.addTitle("Opportunity found");
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
                message += ": " + THRewardItem.parse(treasure).getDisplayName() + " location discovered.";
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

    public Set<String> getRandomRewardItems(int count, Random random) {
        return treasurePicker.getRandomUnseenItems(count, random);
    }

    public void removeRewardItemFromPool(String itemId) {
        treasurePicker.removeItemFromPool(itemId);
    }

    public void syncNewOneTimeContent() {
        treasurePicker.syncNewlyConfigured();
    }

    public Set<String> getOneTimePool() {
        return treasurePicker.getOneTimeCandidates();
    }

    public Set<String> getRepeatablePool() {
        return treasurePicker.getRepeatableCandidates();
    }

    @Override
    public void afterStageDescriptions(TooltipMakerAPI info) {
        float opad = 10f;
        float rowHeight = 24f;
        float barWidth = getBarWidth();
        float gap = 10f;
        float halfWidth = (barWidth - gap) / 2f;

        Set<String> oneTime = treasurePicker.getOneTimeCandidates();
        Set<String> repeatable = treasurePicker.getRepeatableCandidates();
        final int missingOneTime = treasurePicker.getFullOneTimePoolSize() - oneTime.size();
        final boolean canAbandon = getProgress() > 0;

        TooltipMakerAPI oneTimePool = addBox(info, halfWidth, rowHeight,
                "Remaining one-time treasures (" + oneTime.size() + ")", POOL_LABEL, outlineColor(), true,
                createPoolTooltip("Remaining one-time treasures",
                        "These treasures can only be awarded once. This pool is only refilled by spending a story point.",
                        oneTime));
        TooltipMakerAPI repeatablePool = addBox(info, halfWidth, rowHeight,
                "Remaining repeatable treasures (" + repeatable.size() + ")", POOL_LABEL, outlineColor(), true,
                createPoolTooltip("Remaining repeatable treasures",
                        "These treasures can be awarded multiple times. This pool is refilled when emptied.",
                        repeatable));
        info.addCustom(oneTimePool, opad);
        info.addCustomDoNotSetPosition(repeatablePool).getPosition().rightOfTop(oneTimePool, gap);

        TooltipMakerAPI restore = addBox(info, halfWidth, rowHeight,
                "Restore one-time treasures", BUTTON_REGEN_ONE_TIME, Misc.getStoryOptionColor(),
                missingOneTime > 0,
                new BaseFactorTooltip() {
                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        tooltip.addTitle("Restore one-time treasures");
                        if (missingOneTime <= 0) {
                            tooltip.addPara("Every one-time treasure is still in the pool.", 10f);
                            return;
                        }
                        tooltip.addPara("Spend a %s to put %s claimed one-time "
                                        + (missingOneTime == 1 ? "treasure" : "treasures")
                                        + " back into the pool. Grants %s bonus experience.", 10f,
                                Misc.getHighlightColor(), "" + Misc.STORY + " point", "" + missingOneTime, "100%");
                    }
                });
        TooltipMakerAPI abandon = addBox(info, halfWidth, rowHeight,
                "Abandon Hunt", BUTTON_ABANDON, ABANDON_COLOR, canAbandon,
                new BaseFactorTooltip() {
                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        tooltip.addTitle("Abandon Hunt");
                        if (!canAbandon) {
                            tooltip.addPara("There is no progress to abandon.", 10f);
                            return;
                        }
                        tooltip.addPara("Reset all hunt progress to zero and clear your current lead.", 10f);
                    }
                });
        info.addCustom(restore, opad);
        info.addCustomDoNotSetPosition(abandon).getPosition().rightOfTop(restore, gap);
    }

    private TooltipMakerAPI addBox(TooltipMakerAPI info, float width, float height, String text, Object buttonId,
                                   Color accent, boolean enabled, TooltipMakerAPI.TooltipCreator tooltip) {
        TooltipMakerAPI cell = info.beginSubTooltip(width);
        ButtonAPI button = cell.addAreaCheckbox(text, buttonId, outlineColor(), fillColor(), accent,
                width, height, 0f);
        button.setChecked(false);
        button.setEnabled(enabled);
        button.setShowTooltipWhileInactive(true);
        if (tooltip != null) {
            cell.addTooltipToPrevious(tooltip, TooltipMakerAPI.TooltipLocation.BELOW);
        }
        info.endSubTooltip();
        return cell;
    }

    private static Color outlineColor() {
        return Misc.interpolateColor(BAR_COLOR, Color.white, 0.3f);
    }

    private static Color fillColor() {
        return Misc.interpolateColor(BAR_COLOR, Color.black, 0.9f);
    }

    @Override
    public StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId) {
        if (BUTTON_REGEN_ONE_TIME.equals(buttonId)) {
            final int restored = treasurePicker.getFullOneTimePoolSize() - treasurePicker.getOneTimeCandidates().size();
            StoryOptionParams params = new StoryOptionParams(null, 1, "thRestoreOneTimePool",
                    Sounds.STORY_POINT_SPEND_TECHNOLOGY,
                    "Renewed the search for treasures already claimed");
            return new BaseOptionStoryPointActionDelegate(null, params) {
                @Override
                public void confirm() {
                    treasurePicker.regenerateOneTimePool();
                }

                @Override
                public String getTitle() {
                    return null;
                }

                @Override
                public void createDescription(TooltipMakerAPI info) {
                    info.setParaInsigniaLarge();
                    info.addPara("Chase down fresh rumors about relics already accounted for, returning %s "
                                    + (restored == 1 ? "treasure" : "treasures") + " to the one-time pool.",
                            -10f, Misc.getHighlightColor(), "" + restored);
                    info.addSpacer(20f);
                    super.createDescription(info);
                }
            };
        }
        return super.getButtonStoryPointActionDelegate(buttonId);
    }

    @Override
    public void storyActionConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_REGEN_ONE_TIME.equals(buttonId)) {
            ui.updateUIForItem(this);
            return;
        }
        super.storyActionConfirmed(buttonId, ui);
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
        if (POOL_LABEL.equals(buttonId)) {
            ui.updateUIForItem(this);
            return;
        }
        super.buttonPressConfirmed(buttonId, ui);
    }

    @Override
    public void buttonPressCancelled(Object buttonId, IntelUIAPI ui) {
        ui.updateUIForItem(this);
        super.buttonPressCancelled(buttonId, ui);
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
                sorted.sort((a, b) -> THRewardItem.parse(a).getDisplayName()
                        .compareToIgnoreCase(THRewardItem.parse(b).getDisplayName()));
                for (String token : sorted) {
                    tooltip.addPara("  - " + THRewardItem.parse(token).getDisplayName(), 2f);
                }
            }
        };
    }

    protected void notifyStageReached(EventStageData stage){
        if (stage.id == Stage.CHOOSE) {
            var script = new THChooseLeadScript(this);
            Global.getSector().addScript(script);
        }
        if (stage.id == Stage.OPPORTUNITY){
            ITHOpportunity opportunity = THRegistry.getOpportunityRegistry().pickCandidate();
            if (opportunity != null) {
                opportunity.trigger();
                opportunityIcon = Global.getSettings().getSpriteName(ITHOpportunity.ICON_CATEGORY, opportunity.getIcon());
                opportunityDisplayName = opportunity.getDisplayName();
            }
        }
        if (stage.id == Stage.FOUND){
            setProgress(0);
            THRewardItem reward = THRewardItem.parse(treasure);
            if (!treasure.isEmpty() && reward.isValid()) {
                ITHClaimHandler claimHandler = THRegistry.getClaimHandlerRegistry().pickCandidate();
                if (claimHandler != null) {
                    claimHandler.trigger(reward.toSpecialItemData());
                } else {
                    new THFoundTreasureIntel(treasure);
                }
            }
            treasure = "";
        }
    }
}
