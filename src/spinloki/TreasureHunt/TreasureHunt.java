package spinloki.TreasureHunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.api.THApi;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.items.THVanillaItemTagger;
import spinloki.TreasureHunt.internal.opportunities.THScavengerSwarmOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THSectorSprintOpportunity;
import spinloki.TreasureHunt.internal.opportunities.THStationLeadOpportunity;
import spinloki.TreasureHunt.internal.registry.THRegistry;

public class TreasureHunt extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(TreasureHunt.class);
    private static THFactorTracker factorTracker = null;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        THRegistry.initSettings();
        THVanillaItemTagger.tagItems();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        factorTracker = new THFactorTracker();
        Global.getSector().addScript(factorTracker);
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

        THRegistry.reset();
        registerBuiltInOpportunities();
    }

    private void registerBuiltInOpportunities() {
        THApi.registerOpportunity(new THSectorSprintOpportunity());
        THApi.registerOpportunity(new THStationLeadOpportunity());
        THApi.registerOpportunity(new THScavengerSwarmOpportunity());
    }

    public static THFactorTracker getFactorTrackerForTestOnly(){
        return factorTracker;
    }
}
