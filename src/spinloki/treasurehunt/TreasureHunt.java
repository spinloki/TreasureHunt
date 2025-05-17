package spinloki.treasurehunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTFactorTracker;
import org.apache.log4j.Logger;
import spinloki.treasurehunt.campaign.intel.events.THFactorTracker;
import spinloki.treasurehunt.campaign.items.THVanillaItemTagger;
import spinloki.treasurehunt.config.Settings;

public class TreasureHunt extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(TreasureHunt.class);
    private static THFactorTracker factorTracker = null;

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        Settings.loadSettingsFromJson();
        THVanillaItemTagger.tagItems();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        factorTracker = new THFactorTracker();
    }

    @Override
    public void onGameLoad(boolean newGame){
        if (factorTracker == null){
            factorTracker = new THFactorTracker();
        }

        // Sorry, but this hacky way is the only way I can reliably solve that stupid duplicate glitch
        Global.getSector().removeScriptsOfClass(factorTracker.getClass());
        Global.getSector().getListenerManager().removeListenerOfClass(factorTracker.getClass());
        Global.getSector().addScript(factorTracker);
        Global.getSector().getListenerManager().addListener(factorTracker);

        // TODO: Replace the whole factor tracker member nonsense with the below on major increment
        /*if (!Global.getSector().hasScript(THFactorTracker.class)) {
            Global.getSector().addScript(new THFactorTracker());
        }*/
    }
}
