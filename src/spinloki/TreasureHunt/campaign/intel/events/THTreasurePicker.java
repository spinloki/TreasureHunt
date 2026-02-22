package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.config.THSettings;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    THTreasurePicker(){
        addRepeatableItems();
        addOneTimeItems();
        Global.getSector().getListenerManager().addListener(this);
    }

    private Set<String> oneTimeCandidates;
    private Set<String> repeatableCandidates;

    private void addRepeatableItems() {
        repeatableCandidates = new HashSet<>();
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (THSettings.getRepeatItems().contains(item.getId())){
                repeatableCandidates.add(item.getId());
            }
        }
    }

    private void addOneTimeItems(){
        oneTimeCandidates = new HashSet<>();
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (THSettings.getOneTimeItems().contains(item.getId())){
                oneTimeCandidates.add(item.getId());
            }
        }
    }

    public Set<String> getRandomUnseenItems(int count) {
        Set<String> combinedPool = new HashSet<>();
        combinedPool.addAll(repeatableCandidates);
        combinedPool.addAll(oneTimeCandidates);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        picker.addAll(combinedPool);

        Set<String> result = new HashSet<>();

        for (int i = 0; i < count && !picker.isEmpty(); i++) {
            String pick = picker.pickAndRemove();
            result.add(pick);
        }

        if (picker.isEmpty()){
            addRepeatableItems();
        }

        return result;
    }

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        if (dialog.getInteractionTarget().getFullName().equals("Cargo Pods")){
            // So if the player puts an item in a cargo pod and then looks at the cargo pod, it doesn't get removed
            return;
        }
        if (loot.getQuantity(CargoAPI.CargoItemType.SPECIAL, null) != 0){
            for (CargoStackAPI stack : loot.getStacksCopy()){
                if (stack.isSpecialStack()){
                    SpecialItemData specialItemData = stack.getSpecialDataIfSpecial();
                    if (specialItemData != null){
                        removeItemFromPool(specialItemData.getId());
                    }
                }
            }
        }
    }

    public void removeItemFromPool(String itemId) {
        removeItemsFromPool(Collections.singleton(itemId));
    }

    public void removeItemsFromPool(Collection<String> itemIds) {
        for (String id : itemIds) {
            oneTimeCandidates.remove(id);
            repeatableCandidates.remove(id);
        }
    }
}

