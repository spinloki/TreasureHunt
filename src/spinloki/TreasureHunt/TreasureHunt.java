package spinloki.TreasureHunt;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.api.THApi;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.intel.THExcavationRaidListener;
import spinloki.TreasureHunt.internal.intel.THRuinExcavationIntel;
import spinloki.TreasureHunt.internal.items.THVanillaItemTagger;
import spinloki.TreasureHunt.internal.opportunities.THRuinExcavationOpportunity;
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
        registerLunaListener();
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
        Global.getSector().getListenerManager().removeListenerOfClass(THExcavationRaidListener.class);

        factorTracker = new THFactorTracker();
        Global.getSector().addScript(factorTracker);
        Global.getSector().getListenerManager().addListener(factorTracker);
        Global.getSector().getListenerManager().addListener(new THExcavationRaidListener());

        THRegistry.reset();
        THRegistry.getSettings().loadFromLuna();
        registerBuiltInOpportunities();
        repairExcavationStations();
    }

    private void registerBuiltInOpportunities() {
        THApi.registerOpportunity(new THSectorSprintOpportunity());
        THApi.registerOpportunity(new THStationLeadOpportunity());
        THApi.registerOpportunity(new THScavengerSwarmOpportunity());
        THApi.registerOpportunity(new THRuinExcavationOpportunity());
    }

    /**
     * Re-apply FIDConfigGen to existing excavation station fleets from old saves
     * where the lambda couldn't survive XStream deserialization.
     */
    private void repairExcavationStations() {
        for (var intel : Global.getSector().getIntelManager().getIntel(THRuinExcavationIntel.class)) {
            var excavation = (THRuinExcavationIntel) intel;
            var fleet = excavation.getStationFleet();
            if (fleet != null && fleet.isAlive()) {
                var mem = fleet.getMemoryWithoutUpdate();
                if (mem.get("$fidConifgGen") == null) {
                    mem.set("$fidConifgGen", new THRuinExcavationIntel.THExcavationStationFIDConfigGen());
                }
            }
        }
    }

    public static THFactorTracker getFactorTrackerForTestOnly(){
        return factorTracker;
    }

    private void registerLunaListener() {
        try {
            Class.forName("lunalib.lunaSettings.LunaSettings");
        } catch (ClassNotFoundException e) {
            return;
        }
        if (!lunalib.lunaSettings.LunaSettings.hasSettingsListenerOfClass(THLunaSettingsListener.class)) {
            lunalib.lunaSettings.LunaSettings.addSettingsListener(new THLunaSettingsListener());
        }
    }

    private static class THLunaSettingsListener implements lunalib.lunaSettings.LunaSettingsListener {
        @Override
        public void settingsChanged(String modId) {
            if ("spinloki_treasurehunt".equals(modId)) {
                THRegistry.getSettings().loadFromLuna();
            }
        }
    }
}
