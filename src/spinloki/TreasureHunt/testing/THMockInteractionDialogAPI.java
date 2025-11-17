package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;

import java.awt.*;
import java.util.List;

public class THMockInteractionDialogAPI implements InteractionDialogAPI{
    SectorEntityToken entityToken;

    public THMockInteractionDialogAPI(SectorEntityToken entityToken){
        this.entityToken = entityToken;
    }

    @Override
    public void setTextWidth(float width) {

    }

    @Override
    public void setTextHeight(float height) {

    }

    @Override
    public void setXOffset(float xOffset) {

    }

    @Override
    public void setYOffset(float yOffset) {

    }

    @Override
    public void setPromptText(String promptText) {

    }

    @Override
    public void hideTextPanel() {

    }

    @Override
    public void showTextPanel() {

    }

    @Override
    public float getTextWidth() {
        return 0;
    }

    @Override
    public float getTextHeight() {
        return 0;
    }

    @Override
    public float getXOffset() {
        return 0;
    }

    @Override
    public float getYOffset() {
        return 0;
    }

    @Override
    public String getPromptText() {
        return "";
    }

    @Override
    public void flickerStatic(float in, float out) {

    }

    @Override
    public OptionPanelAPI getOptionPanel() {
        return null;
    }

    @Override
    public TextPanelAPI getTextPanel() {
        return null;
    }

    @Override
    public VisualPanelAPI getVisualPanel() {
        return null;
    }

    @Override
    public SectorEntityToken getInteractionTarget() {
        return entityToken;
    }

    @Override
    public InteractionDialogPlugin getPlugin() {
        return null;
    }

    @Override
    public void setOptionOnEscape(String text, Object optionId) {

    }

    @Override
    public void startBattle(BattleCreationContext context) {

    }

    @Override
    public void dismiss() {

    }

    @Override
    public void dismissAsCancel() {

    }

    @Override
    public void showFleetMemberPickerDialog(String title, String okText, String cancelText, int rows, int cols, float iconSize, boolean canPickNotReady, boolean canPickMultiple, List<FleetMemberAPI> pool, FleetMemberPickerListener listener) {

    }

    @Override
    public void showCustomDialog(float customPanelWidth, float customPanelHeight, CustomDialogDelegate delegate) {

    }

    @Override
    public void hideVisualPanel() {

    }

    @Override
    public void showCommDirectoryDialog(CommDirectoryAPI dir) {

    }

    @Override
    public void setOptionOnConfirm(String text, Object optionId) {

    }

    @Override
    public void setOpacity(float opacity) {

    }

    @Override
    public void setBackgroundDimAmount(float backgroundDimAmount) {

    }

    @Override
    public void setPlugin(InteractionDialogPlugin plugin) {

    }

    @Override
    public void setInteractionTarget(SectorEntityToken entityToken) {
        this.entityToken = entityToken;
    }

    @Override
    public void showCargoPickerDialog(String title, String okText, String cancelText, boolean small, float textPanelWidth, CargoAPI cargo, CargoPickerListener listener) {

    }

    @Override
    public void showIndustryPicker(String title, String okText, MarketAPI market, List<Industry> industries, IndustryPickerListener listener) {

    }

    @Override
    public void makeOptionOpenCore(String optionId, CoreUITabId tabId, CampaignUIAPI.CoreUITradeMode mode) {

    }

    @Override
    public void makeOptionOpenCore(String optionId, CoreUITabId tabId, CampaignUIAPI.CoreUITradeMode mode, boolean onlyShowTargetTabShortcut) {

    }

    @Override
    public void setOptionColor(Object optionId, Color color) {

    }

    @Override
    public void makeStoryOption(Object optionId, int storyPoints, float bonusXPFraction, String soundId) {

    }

    @Override
    public void addOptionSelectedText(Object optionId) {

    }

    @Override
    public void addOptionSelectedText(Object optionId, boolean allowPrintingStoryOption) {

    }

    @Override
    public void showFleetMemberRecoveryDialog(String title, List<FleetMemberAPI> pool, FleetMemberPickerListener listener) {

    }

    @Override
    public void showFleetMemberRecoveryDialog(String title, List<FleetMemberAPI> pool, List<FleetMemberAPI> storyPool, FleetMemberPickerListener listener) {

    }

    @Override
    public void showGroundRaidTargetPicker(String title, String okText, MarketAPI market, List<GroundRaidObjectivePlugin> data, GroundRaidTargetPickerDelegate listener) {

    }

    @Override
    public void showVisualPanel() {

    }

    @Override
    public void showCustomProductionPicker(CustomProductionPickerDelegate delegate) {

    }

    @Override
    public void showCampaignEntityPicker(String title, String selectedText, String okText, FactionAPI factionForUIColors, List<SectorEntityToken> entities, CampaignEntityPickerListener listener) {

    }

    @Override
    public boolean isCurrentOptionHadAConfirm() {
        return false;
    }

    @Override
    public void showCustomVisualDialog(float customPanelWidth, float customPanelHeight, CustomVisualDialogDelegate delegate) {

    }

    @Override
    public void showCargoPickerDialog(String title, String okText, String cancelText, boolean small, float textPanelWidth, float width, float height, CargoAPI cargo, CargoPickerListener listener) {

    }
}
