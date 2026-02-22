package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.events.THLeadPickDialogPlugin;
import spinloki.TreasureHunt.config.THSettings;

import java.util.Set;

public class THChooseLeadScript implements EveryFrameScript {

    protected IntervalUtil interval = new IntervalUtil(.05f, .1f);
    protected boolean done = false;

    private boolean dialogShown = false;
    private boolean waitingForChoice = false;

    private Set<String> candidates;

    ResetProgress reset;
    GetCandidates getCandidates;
    ChooseCandidate chooseCandidate;

    public interface ResetProgress {
        void reset();
    }

    public interface GetCandidates {
        Set<String> getCandidates(int count);
    }

    public interface ChooseCandidate {
        void chooseCandidate(Set<String> candidates, String picked);
    }

    public THChooseLeadScript(ResetProgress reset,
                              GetCandidates getCandidates,
                              ChooseCandidate chooseCandidate) {
        this.reset = reset;
        this.getCandidates = getCandidates;
        this.chooseCandidate = chooseCandidate;
    }

    @Override
    public void advance(float amount) {
        if (done) return;

        interval.advance(amount);
        if (!interval.intervalElapsed()) return;

        CampaignFleetAPI pf = Global.getSector().getPlayerFleet();

        if (!dialogShown) {
            if (!Global.getSector().getCampaignUI().isShowingDialog() &&
                    !Global.getSector().getCampaignUI().isShowingMenu()) {

                candidates = getCandidates.getCandidates(THSettings.TH_NUM_LEAD_CANDIDATES);

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

                String choice = dialogPlugin.getChosen();

                if (choice == null) {
                    reset.reset();
                }
                chooseCandidate.chooseCandidate(candidates, choice);

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

    private THLeadPickDialogPlugin dialogPlugin;
}