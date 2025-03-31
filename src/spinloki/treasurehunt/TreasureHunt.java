package spinloki.treasurehunt;

import com.fs.starfarer.api.BaseModPlugin;

public class TreasureHunt extends BaseModPlugin {
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
        TreasureHuntEventIntel.addFactorCreateIfNecessary(new THTimeFactor(10), null);
    }


    // You can add more methods from ModPlugin here. Press Control-O in IntelliJ to see options.
}
