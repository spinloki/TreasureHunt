package spinloki.TreasureHunt.testing;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class THMockFleetData implements FleetDataAPI {
    private final List<FleetMemberAPI> members;
    private final ArrayList<FleetMemberAPI> snapshot;

    public THMockFleetData(List<FleetMemberAPI> members, ArrayList<FleetMemberAPI> snapshot) {
        this.members = members;
        this.snapshot = snapshot;
    }

    @Override
    public List<FleetMemberAPI> getMembersInPriorityOrder() {
        return List.of();
    }

    @Override
    public List<FleetMemberAPI> getMembersListCopy() {
        return members;
    }

    @Override
    public List<FleetMemberAPI> getCombatReadyMembersListCopy() {
        return List.of();
    }

    @Override
    public float getFleetPointsUsed() {
        return 0;
    }

    @Override
    public void addFleetMember(FleetMemberAPI member) {

    }

    @Override
    public void removeFleetMember(FleetMemberAPI member) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void scuttle(FleetMemberAPI member) {

    }

    @Override
    public float getMaxBurnLevel() {
        return 0;
    }

    @Override
    public float getMinBurnLevel() {
        return 0;
    }

    @Override
    public float getBurnLevel() {
        return 0;
    }

    @Override
    public void setFlagship(FleetMemberAPI flagship) {

    }

    @Override
    public CampaignFleetAPI getFleet() {
        return null;
    }

    @Override
    public float getTravelSpeed() {
        return 0;
    }

    @Override
    public void takeSnapshot() {

    }

    @Override
    public ArrayList<FleetMemberAPI> getSnapshot() {
        return snapshot;
    }

    @Override
    public boolean areAnyShipsPerformingRepairs() {
        return false;
    }

    @Override
    public void sort() {

    }

    @Override
    public List<OfficerDataAPI> getOfficersCopy() {
        return List.of();
    }

    @Override
    public void addOfficer(PersonAPI person) {

    }

    @Override
    public void removeOfficer(PersonAPI person) {

    }

    @Override
    public OfficerDataAPI getOfficerData(PersonAPI person) {
        return null;
    }

    @Override
    public FleetMemberAPI getMemberWithCaptain(PersonAPI captain) {
        return null;
    }

    @Override
    public int getNumMembers() {
        return 0;
    }

    @Override
    public void syncMemberLists() {

    }

    @Override
    public boolean isOnlySyncMemberLists() {
        return false;
    }

    @Override
    public void setOnlySyncMemberLists(boolean onlySyncMemberLists) {

    }

    @Override
    public void syncIfNeeded() {

    }

    @Override
    public void setSyncNeeded() {

    }

    @Override
    public List<FleetMemberAPI> getMembersListWithFightersCopy() {
        return List.of();
    }

    @Override
    public PersonAPI getCommander() {
        return null;
    }

    @Override
    public float getMinCrew() {
        return 0;
    }

    @Override
    public void ensureHasFlagship() {

    }

    @Override
    public FleetMemberAPI addFleetMember(String variantId) {
        return null;
    }

    @Override
    public void addOfficer(OfficerDataAPI officer) {

    }

    @Override
    public void updateCargoCapacities() {

    }

    @Override
    public String pickShipName(FleetMemberAPI member, Random random) {
        return "";
    }

    @Override
    public float getEffectiveStrength() {
        return 0;
    }

    @Override
    public Map<String, Object> getCacheClearedOnSync() {
        return Map.of();
    }

    @Override
    public float getMinBurnLevelUnmodified() {
        return 0;
    }

    @Override
    public Random getShipNameRandom() {
        return null;
    }

    @Override
    public void setShipNameRandom(Random shipNameRandom) {

    }

    @Override
    public void sortToMatchOrder(List<FleetMemberAPI> originalOrder) {

    }

    @Override
    public boolean isForceNoSync() {
        return false;
    }

    @Override
    public void setForceNoSync(boolean forceNoSync) {

    }
}
