package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.EngagementResultForFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Map;

public class THMockBattle implements BattleAPI {
    private final List<CampaignFleetAPI> nonPlayerSide;

    public THMockBattle(List<CampaignFleetAPI> nonPlayerSide) {
        this.nonPlayerSide = nonPlayerSide;
    }

    @Override
    public void genCombined() {

    }

    @Override
    public void genCombinedDoNotRemoveEmpty() {

    }

    @Override
    public void uncombine() {

    }

    @Override
    public CampaignFleetAPI getCombinedOne() {
        return null;
    }

    @Override
    public CampaignFleetAPI getCombinedTwo() {
        return null;
    }

    @Override
    public boolean canJoin(CampaignFleetAPI fleet) {
        return false;
    }

    @Override
    public BattleSide pickSide(CampaignFleetAPI fleet) {
        return null;
    }

    @Override
    public boolean join(CampaignFleetAPI fleet) {
        return false;
    }

    @Override
    public boolean isPlayerInvolved() {
        return false;
    }

    @Override
    public List<CampaignFleetAPI> getFleetsFor(EngagementResultForFleetAPI side) {
        return List.of();
    }

    @Override
    public boolean isPlayerSide(EngagementResultForFleetAPI side) {
        return false;
    }

    @Override
    public CampaignFleetAPI getCombinedFor(CampaignFleetAPI participantOrCombined) {
        return null;
    }

    @Override
    public CampaignFleetAPI getSourceFleet(FleetMemberAPI member) {
        return null;
    }

    @Override
    public List<CampaignFleetAPI> getSideFor(CampaignFleetAPI participantOrCombined) {
        return List.of();
    }

    @Override
    public CampaignFleetAPI getPrimary(List<CampaignFleetAPI> side) {
        return null;
    }

    @Override
    public boolean isPlayerSide(List<CampaignFleetAPI> side) {
        return false;
    }

    @Override
    public List<CampaignFleetAPI> getPlayerSide() {
        return List.of();
    }

    @Override
    public void removeEmptyFleets() {

    }

    @Override
    public boolean isPlayerPrimary() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public List<CampaignFleetAPI> getSideOne() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getSideTwo() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getNonPlayerSide() {
        return this.nonPlayerSide;
    }

    @Override
    public CampaignFleetAPI getPlayerCombined() {
        return null;
    }

    @Override
    public CampaignFleetAPI getNonPlayerCombined() {
        return null;
    }

    @Override
    public CampaignFleetAPI getCombined(BattleSide side) {
        return null;
    }

    @Override
    public CampaignFleetAPI getOtherSideCombined(BattleSide side) {
        return null;
    }

    @Override
    public void leave(CampaignFleetAPI fleet, boolean engagedInHostilities) {

    }

    @Override
    public List<CampaignFleetAPI> getSide(BattleSide side) {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getOtherSide(BattleSide side) {
        return List.of();
    }

    @Override
    public boolean knowsWhoPlayerIs(List<CampaignFleetAPI> side) {
        return false;
    }

    @Override
    public BattleSide pickSide(CampaignFleetAPI fleet, boolean considerPlayerTransponderStatus) {
        return null;
    }

    @Override
    public void takeSnapshots() {

    }

    @Override
    public List<CampaignFleetAPI> getSnapshotSideOne() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getSnapshotSideTwo() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getSnapshotSideFor(CampaignFleetAPI participantOrCombined) {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getSnapshotFor(List<CampaignFleetAPI> side) {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getBothSides() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getSnapshotBothSides() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getOtherSideFor(CampaignFleetAPI participantOrCombined) {
        return List.of();
    }

    @Override
    public boolean isOnPlayerSide(CampaignFleetAPI participantOrCombined) {
        return false;
    }

    @Override
    public List<CampaignFleetAPI> getOtherSideSnapshotFor(CampaignFleetAPI participantOrCombined) {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getPlayerSideSnapshot() {
        return List.of();
    }

    @Override
    public List<CampaignFleetAPI> getNonPlayerSideSnapshot() {
        return List.of();
    }

    @Override
    public boolean isInvolved(CampaignFleetAPI test) {
        return false;
    }

    @Override
    public float getPlayerInvolvementFraction() {
        return 0;
    }

    @Override
    public boolean hasSnapshots() {
        return false;
    }

    @Override
    public void applyVisibilityMod(CampaignFleetAPI fleet) {

    }

    @Override
    public boolean onSameSide(CampaignFleetAPI one, CampaignFleetAPI two) {
        return false;
    }

    @Override
    public boolean onPlayerSide(CampaignFleetAPI fleet) {
        return false;
    }

    @Override
    public CampaignFleetAPI getClosestInvolvedFleetTo(CampaignFleetAPI fleet) {
        return null;
    }

    @Override
    public void finish(BattleSide winner) {

    }

    @Override
    public void finish(BattleSide winner, boolean engagedInHostilities) {

    }

    @Override
    public boolean isPlayerInvolvedAtStart() {
        return false;
    }

    @Override
    public void setPlayerInvolvedAtStart(boolean playerInvolvedAtStart) {

    }

    @Override
    public void setPlayerInvolvementFraction(float playerInvolvementFraction) {

    }

    @Override
    public CampaignFleetAPI getPrimary(List<CampaignFleetAPI> side, boolean nonPlayer) {
        return null;
    }

    @Override
    public Map<FleetMemberAPI, CampaignFleetAPI> getMemberSourceMap() {
        return Map.of();
    }

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public Vector2f computeCenterOfMass() {
        return null;
    }

    @Override
    public boolean isStationInvolved() {
        return false;
    }

    @Override
    public boolean isStationInvolvedOnPlayerSide() {
        return false;
    }

    @Override
    public boolean isStationInvolved(List<CampaignFleetAPI> side) {
        return false;
    }

    @Override
    public List<CampaignFleetAPI> getStationSide() {
        return List.of();
    }

    @Override
    public void genCombined(boolean withStation) {

    }

    @Override
    public boolean join(CampaignFleetAPI fleet, BattleSide side) {
        return false;
    }

    @Override
    public boolean wasFleetDefeated(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner) {
        return false;
    }

    @Override
    public boolean wasFleetVictorious(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner) {
        return false;
    }
}
