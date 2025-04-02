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
        if (name.equals("Research Station") || name.equals("Mining Station") || name.equals("Orbital Habitat")){
            setNotify(new THSalvageFactor(20, "exploring a derelict station"));
        }
        else if (name.equals("Derelict Ship")){
            setNotify(new THSalvageFactor(5, "exploring a derelict ship"));
        }
        else if (name.equals("Supply Cache")){
            setNotify(new THSalvageFactor(5, "exploring a supply cache"));
        }
        else if (entity.getId().contains("planet")){
            setNotify(new THSalvageFactor(10, "exploring a ruin"));
        }
        else if (!entity.getFaction().getId().equals("neutral")){
            setNotify(new THSalvageFactor(10, "raiding a colony"));
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
