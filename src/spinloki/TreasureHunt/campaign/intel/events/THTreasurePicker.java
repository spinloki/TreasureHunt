package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import spinloki.TreasureHunt.config.THSettings;
import spinloki.TreasureHunt.util.THUtils;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    THTreasurePicker(){
        resetUnseenItems();
        addOneTimeItems();
        Global.getSector().getListenerManager().addListener(this);
    }

    private static final String THTreasurePickerVersionId = "$th_treasure_picker_version";
    private static final int currentPickerVersion = 1;

    private Set<String> unseenOneTimeItems;
    private Set<String> unseenRepeatableItems;

    private void resetUnseenItems() {
        unseenRepeatableItems = new HashSet<>();
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (THSettings.getRepeatItems().contains(item.getId())){
                unseenRepeatableItems.add(item.getId());
            }
        }
    }

    private void addOneTimeItems(){
        unseenOneTimeItems = new HashSet<>();
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (THSettings.getOneTimeItems().contains(item.getId())){
                unseenOneTimeItems.add(item.getId());
            }
        }
    }

    public String getRandomUnseenItem() {
        if (!Global.getSector().getMemoryWithoutUpdate().is(THTreasurePickerVersionId, currentPickerVersion)){
            Global.getSector().getMemoryWithoutUpdate().set(THTreasurePickerVersionId, currentPickerVersion);
            resetUnseenItems();
            addOneTimeItems();
        }

        if (unseenRepeatableItems.isEmpty()){
            resetUnseenItems();
        }

        Set<String> unseenItems;
        boolean pickBlueprint = (new Random().nextDouble() <= THSettings.TH_PICK_BLUEPRINT_WEIGHT) && !unseenOneTimeItems.isEmpty();
        if (pickBlueprint) {
            unseenItems = unseenOneTimeItems;
        }
        else {
            unseenItems = unseenRepeatableItems;
        }

        List<String> itemList = new ArrayList<>(unseenItems);
        String chosenTreasure = itemList.get(new Random().nextInt(itemList.size()));
        unseenItems.remove(chosenTreasure);

        return chosenTreasure;
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
                        markItemAsSeen(specialItemData.getId());
                    }
                }
             }
        }
    }

    public void markItemAsSeen(String itemId) {
        unseenOneTimeItems.remove(itemId);
        unseenRepeatableItems.remove(itemId);
    }
}

