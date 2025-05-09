package spinloki.treasurehunt;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.campaign.fleet.CampaignFleet;

import java.util.Map;

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
        if (entity.getClass().getName().equals("CampaignFleet")){
            return;
        }
        var name = entity.getName();
        if (name.equals("Research Station") || name.equals("Mining Station") || name.equals("Orbital Habitat")){
            setNotify(new THSalvageFactor(25, "exploring a derelict station"));
        }
        else if (name.equals("Derelict Ship")){
            setNotify(new THSalvageFactor(3, "exploring a derelict ship"));
        }
        else if (name.equals("Supply Cache")){
            setNotify(new THSalvageFactor(5, "exploring a supply cache"));
        }
        else if (entity.getClass().getName().contains("CampaignPlanet")){
            if (!entity.getFaction().getId().equals("neutral")){
                setNotify(new THSalvageFactor(15, "raiding a colony"));
            }
            else {
                var ruin = "";
                for (var condition : entity.getMarket().getConditions()){
                    if (condition.getId().contains("ruin")){
                        ruin = condition.getId();
                        break;
                    }
                }
                if (!ruin.isEmpty()){
                    var value = 10;
                    var size = "scattered";
                    if (ruin.contains("widespread")){
                        value = 20;
                        size = "widespread";
                    }
                    else if (ruin.contains("extensive")){
                        value = 40;
                        size = "extensive";
                    }
                    else if (ruin.contains("vast")){
                        value = 80;
                        size = "vast";
                    }
                    setNotify(new THSalvageFactor(value, String.format("exploring a %s ruin", size)));
                }
            }
        }
    }

    private THSalvageFactor mFactor;
    private boolean mNotify = false;
    private float interval = 1;
    private float timePassed = 0;
    private boolean debugAdvancement = false;
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
          if (debugAdvancement){
            timePassed += amount;
            if (timePassed > interval){
                timePassed = 0;
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor( 50), null);
            }
        }
        if (mNotify) {
            mNotify = false;
            TreasureHuntEventIntel.addFactorCreateIfNecessary(mFactor, null);
        }
    }
}
