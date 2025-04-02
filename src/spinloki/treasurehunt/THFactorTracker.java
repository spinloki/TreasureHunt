package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;

public class THFactorTracker implements ShowLootListener {
    public void init(){
        Global.getSector().getListenerManager().addListener(this);
    }

    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        var entity = dialog.getInteractionTarget();
        if (entity == null){
            return;
        }
        var name = entity.getName();
        if (name.equals("Research Station") || name.equals("Mining Station")){
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(10, "station"), null);
        }
        else if (name.equals("Derelict Ship")){
            TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSalvageFactor(5, "ship"), null);
        }
    }
}
