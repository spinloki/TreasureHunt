package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.Set;

public class THChooseLeadScript implements EveryFrameScript {

    protected IntervalUtil interval = new IntervalUtil(.05f, .1f);
    protected boolean done = false;

    private boolean dialogShown = false;
    private boolean waitingForChoice = false;

    private Set<String> candidates;

    private TreasureHuntEventIntel intel;

    private THLeadPickDialogPlugin dialogPlugin;

    public THChooseLeadScript(TreasureHuntEventIntel intel) {
        this.intel = intel;
    }

    private TreasureHuntEventIntel resolveIntel() {
        if (intel == null) {
            intel = TreasureHuntEventIntel.get();
        }
        return intel;
    }

    @Override
    public void advance(float amount) {
        if (done) return;

        interval.advance(amount);
        if (!interval.intervalElapsed()) return;

        TreasureHuntEventIntel intel = resolveIntel();
        if (intel == null) {
            done = true;
            return;
        }

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        if (!dialogShown) {
            if (!Global.getSector().getCampaignUI().isShowingDialog() &&
                    !Global.getSector().getCampaignUI().isShowingMenu()) {

                candidates = intel.getRandomRewardItems(THRegistry.getSettings().getNumLeadCandidates());

                THLeadPickDialogPlugin plugin =
                        new THLeadPickDialogPlugin(candidates);

                Global.getSector().getCampaignUI().showInteractionDialog(plugin, pf);

                this.dialogPlugin = plugin;
                dialogShown = true;
                waitingForChoice = true;
            }
            return;
        }

        if (waitingForChoice) {
            if (!Global.getSector().getCampaignUI().isShowingDialog()) {

                String choice = dialogPlugin == null ? null : dialogPlugin.getChosen();

                if (choice == null) {
                    intel.setProgress(0);
                }
                intel.pickTreasureFromCandidates(candidates, choice);

                waitingForChoice = false;
                done = true;
            }
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
