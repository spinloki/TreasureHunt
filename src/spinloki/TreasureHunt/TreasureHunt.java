package spinloki.TreasureHunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.campaign.fleets.THScavengerSwarmFactionSetup;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;
import spinloki.TreasureHunt.campaign.intel.events.THFactorTracker;
import spinloki.TreasureHunt.campaign.items.THVanillaItemTagger;
import spinloki.TreasureHunt.config.THSettings;

public class TreasureHunt extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(TreasureHunt.class);
    private static THFactorTracker factorTracker = null;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        THSettings.loadSettingsFromJson();
        THSettings.loadTHRewards();
        THVanillaItemTagger.tagItems();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        factorTracker = new THFactorTracker();
        Global.getSector().addScript(new THFactorTracker());
        Global.getSector().getListenerManager().addListener(factorTracker);
    }

    @Override
    public void onGameLoad(boolean newGame){
        if (Global.getSector().hasScript(THFactorTracker.class)) {
            Global.getSector().removeScriptsOfClass(THFactorTracker.class);
            Global.getSector().getListenerManager().removeListenerOfClass(THFactorTracker.class);
        }
        factorTracker = new THFactorTracker();
        Global.getSector().addScript(factorTracker);
        Global.getSector().getListenerManager().addListener(factorTracker);

        THScavengerSwarmIntel.resetFactionsWithAIAndFleetCreators();
        THScavengerSwarmFactionSetup.setupScavengerSwarmVanillaFactionBehaviors();
    }
}
