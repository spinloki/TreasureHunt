package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.fleet.CampaignFleet;

public class THFactorTracker implements ShowLootListener, EveryFrameScript {
    public THFactorTracker(){
        Global.getSector().getListenerManager().addListener(this);
        Global.getSector().addScript(this);
    }

    // Sigmoid because I'm FANCY even though a straight line clamping from 15 to 50 would work exactly as well
    private static final double K = 0.00001;     // Steepness
    private static final double X0 = 250000.0;  // Midpoint at 100k base value
    private static float calculateProgressFromBaseValue(float baseValue) {
        double sigmoid = 1.0 / (1.0 + Math.exp(-K * ((double) baseValue - X0)));
        double scaled = 15.0 + (50.0 - 15.0) * sigmoid;
        return (float) Math.round(scaled);
    }

    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        var entity = dialog.getInteractionTarget();
        if (entity == null){
            return;
        }
        var name = entity.getName();
        if (entity instanceof CampaignFleet fleet){
            if (Misc.isScavenger(fleet)){
                float value = 0;
                for (var member : fleet.getFleetData().getSnapshot()){
                    value += member.getHullSpec().getBaseValue();
                }
                for (var member: fleet.getFleetData().getMembers()){
                    value -= member.getHullSpec().getBaseValue();
                }
                setNotify(new THSalvageFactor((int) calculateProgressFromBaseValue(value), "destroying a scavenger fleet"));
            }
            return;
        }
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
