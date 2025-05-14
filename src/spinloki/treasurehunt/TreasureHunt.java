package spinloki.treasurehunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
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
        factorTracker = new THFactorTracker(true);
    }

    @Override
    public void onGameLoad(boolean newGame){
        if (factorTracker == null){
            factorTracker = new THFactorTracker(true);
        }
    }
}
