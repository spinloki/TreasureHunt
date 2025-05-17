package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import spinloki.treasurehunt.config.Settings;

public class THFactorTracker implements ShowLootListener, PlayerColonizationListener, EveryFrameScript {
    public THFactorTracker(){

    }

    // Sigmoid because I'm FANCY even though a straight line clamping from 15 to 50 would work exactly as well
    private static final double K = 0.00001;     // Steepness
    private static final double X0 = 250000.0;  // Midpoint at 250k base value
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
        if (entity instanceof CampaignFleetAPI fleet){
            if (Misc.isScavenger(fleet)){
                float value = 0;
                for (var member : fleet.getFleetData().getSnapshot()){
                    value += member.getHullSpec().getBaseValue();
                }
                for (var member: fleet.getFleetData().getMembersListCopy()){
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
        else if (entity instanceof PlanetAPI planet){
            var market = planet.getMarket();
            if (Misc.getDaysSinceLastRaided(market) < .3){
                setNotify(new THSalvageFactor(15, "raiding a colony"));
            }

            else if (Misc.hasRuins(market)){
                var ruin = Misc.getRuinsType(market);
                int value;
                String size;
                try{
                    value = (int) Settings.TH_RUINS_EXPLORATION_VALUES.get(ruin);
                    size = ruin.replace("ruins_","");
                } catch (JSONException e) {
                    value = 10;
                    size = "scattered";
                }
                setNotify(new THSalvageFactor(value, String.format("exploring a %s ruin", size)));
            }
        }
    }

    private THSalvageFactor mFactor;
    private boolean mNotify = false;
    private float interval = 1;
    private float timePassed = 0;
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
        if (Settings.TH_DEBUG_USE_TIME_FACTOR){
            timePassed += amount;
            if (timePassed > interval){
                timePassed = 0;
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(Settings.TH_DEBUG_TIME_FACTOR_POINTS), null);
            }
        }
        if (mNotify) {
            mNotify = false;
            TreasureHuntEventIntel.addFactorCreateIfNecessary(mFactor, null);
        }
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planet) {
        var market = planet.getMarket();
        if (Misc.hasRuins(market)){
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THColonyRuinFactor(market), null);
        }
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI colony) {
        // No need to do anything. Factor will expire by itself when the player abandons the colony
    }
}
