package spinloki.treasurehunt;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.IntervalUtil;

public class THFactorTracker implements ShowLootListener, EveryFrameScript {
    THFactorTracker(){
        Global.getSector().getListenerManager().addListener(this);
        Global.getSector().addScript(this);
    }

    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        var entity = dialog.getInteractionTarget();
        if (entity == null){
            return;
        }
        var name = entity.getName();
        if (name.equals("Research Station") || name.equals("Mining Station")){
            setNotify(new THSalvageFactor(10, "station"));
        }
        else if (name.equals("Derelict Ship")){
            setNotify(new THSalvageFactor(5, "ship"));
        }
    }


    public static float CHECK_DAYS = 0.05f;

    protected IntervalUtil interval = new IntervalUtil(CHECK_DAYS * 0.8f, CHECK_DAYS * 1.2f);


    private THSalvageFactor mFactor;
    private boolean mNotify = false;
    public void setNotify(THSalvageFactor factor) {
        mNotify = true;
        mFactor = factor;
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused(){
        return false;
    }

    public void advance(float amount){
        float days = Global.getSector().getClock().convertToDays(amount);

        interval.advance(days);

        if (interval.intervalElapsed() && mNotify) {
            mNotify = false;
            TreasureHuntEventIntel.addFactorCreateIfNecessary(mFactor, null);
        }
    }
}
