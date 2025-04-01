package spinloki.treasurehunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.events.HAPirateBaseDestroyedFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HTScanFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;
import org.apache.log4j.Logger;

public class TreasureHunt extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(TreasureHunt.class);

    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
        // Add your code here, or delete this method (it does nothing unless you add code)
    }

    public void onGameLoad(boolean newGame) {
        TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(300), null);
    }


    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
}
