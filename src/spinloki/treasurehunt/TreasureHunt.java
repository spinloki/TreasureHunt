package spinloki.treasurehunt;

import com.fs.starfarer.api.BaseModPlugin;
import org.apache.log4j.Logger;
import spinloki.treasurehunt.campaign.intel.events.THFactorTracker;
import spinloki.treasurehunt.config.Settings;

public class TreasureHunt extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(TreasureHunt.class);

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        Settings.loadSettingsFromJson();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        new THFactorTracker();
    }
}
