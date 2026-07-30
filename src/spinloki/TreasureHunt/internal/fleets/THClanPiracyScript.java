package spinloki.TreasureHunt.internal.fleets;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


// Modeled on vanilla ScavengerPiracyScript, but turns the fleet hostile in place
// instead of swapping it to the pirate faction, so it stays identifiably a clanner.
public class THClanPiracyScript implements EveryFrameScript {

    private static final String REASON = "thClanPiracy";

    private final CampaignFleetAPI fleet;
    private final IntervalUtil piracyCheck = new IntervalUtil(0.2f, 0.4f);
    private boolean raiding = false;

    public THClanPiracyScript(CampaignFleetAPI fleet) {
        this.fleet = fleet;
    }

    @Override
    public void advance(float amount) {
        piracyCheck.advance(Global.getSector().getClock().convertToDays(amount));
        if (piracyCheck.intervalElapsed()) doPiracyCheck();
    }

    private void doPiracyCheck() {
        if (fleet == null || !fleet.isAlive() || fleet.getBattle() != null) return;

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        boolean inRange = player != null && player.getContainingLocation() == fleet.getContainingLocation()
                && isVisibleAndClose(player);

        if (raiding) {
            if (!inRange) stopRaiding();
            return;
        }
        if (inRange && isWeaker(player)) startRaiding();
    }

    private boolean isVisibleAndClose(CampaignFleetAPI other) {
        if (Misc.getDistance(fleet.getLocation(), other.getLocation()) >= 800f) return false;
        VisibilityLevel level = other.getVisibilityLevelTo(fleet);
        return level == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS
                || level == VisibilityLevel.COMPOSITION_DETAILS;
    }

    private boolean isWeaker(CampaignFleetAPI other) {
        if (fleet.getAI() == null) return false;
        EncounterOption option = fleet.getAI().pickEncounterOption(null, other, true);
        return option == EncounterOption.ENGAGE || option == EncounterOption.HOLD;
    }

    private void startRaiding() {
        raiding = true;
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE, REASON, true, -1f);
    }

    private void stopRaiding() {
        raiding = false;
        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_PIRATE);
        Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE, REASON, false, -1f);
        Misc.clearTarget(fleet, true);
    }

    @Override
    public boolean isDone() {
        return fleet == null || !fleet.isAlive();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
