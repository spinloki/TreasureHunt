package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    THTreasurePicker(){
        addRepeatableItems();
        addOneTimeItems();
    }

    /** Must be called after construction and after deserialization to register the loot listener. */
    void registerListener() {
        Global.getSector().getListenerManager().addListener(this);
    }

    /** Must be called before discarding this picker to avoid a dangling listener. */
    void unregisterListener() {
        Global.getSector().getListenerManager().removeListener(this);
    }

    private Set<String> oneTimeCandidates;
    private Set<String> repeatableCandidates;

    private void addRepeatableItems() {
        repeatableCandidates = new HashSet<>();
        Set<String> repeatItems = new HashSet<>(THRegistry.getRewardRegistry().getRepeatItems());
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (repeatItems.contains(item.getId())){
                repeatableCandidates.add(item.getId());
            }
        }
    }

    private void addOneTimeItems(){
        oneTimeCandidates = new HashSet<>();
        Set<String> oneTimeItems = new HashSet<>(THRegistry.getRewardRegistry().getOneTimeItems());
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (oneTimeItems.contains(item.getId())){
                oneTimeCandidates.add(item.getId());
            }
        }
    }

    public Set<String> getRandomUnseenItems(int count) {
        return getRandomUnseenItems(count, new Random());
    }

    public Set<String> getRandomUnseenItems(int count, Random random) {
        float oneTimeWeight = THRegistry.getRewardRegistry().getPickOneTimeWeight();

        WeightedRandomPicker<String> oneTimePicker = new WeightedRandomPicker<>(random);
        for (String id : oneTimeCandidates) {
            oneTimePicker.add(id);
        }
        WeightedRandomPicker<String> repeatablePicker = new WeightedRandomPicker<>(random);
        for (String id : repeatableCandidates) {
            repeatablePicker.add(id);
        }

        Set<String> result = new HashSet<>();

        for (int i = 0; i < count; i++) {
            boolean pickOneTime = !oneTimePicker.isEmpty() && random.nextFloat() < oneTimeWeight;
            if (pickOneTime) {
                result.add(oneTimePicker.pickAndRemove());
            } else if (!repeatablePicker.isEmpty()) {
                result.add(repeatablePicker.pickAndRemove());
            } else if (!oneTimePicker.isEmpty()) {
                result.add(oneTimePicker.pickAndRemove());
            } else {
                break;
            }
        }

        if (repeatablePicker.isEmpty()) {
            addRepeatableItems();
        }

        return result;
    }

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        if ("Cargo Pods".equals(dialog.getInteractionTarget().getFullName())){
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

    public Set<String> getOneTimeCandidates() {
        return Collections.unmodifiableSet(oneTimeCandidates);
    }

    public Set<String> getRepeatableCandidates() {
        return Collections.unmodifiableSet(repeatableCandidates);
    }
}

