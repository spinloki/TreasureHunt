package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.internal.items.THDynamicPackagePlugin;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.util.THRewardItem;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    private static final Logger log = Global.getLogger(THTreasurePicker.class);

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
    private Set<String> everPooled;

    private void addRepeatableItems() {
        repeatableCandidates = resolveConfiguredItems(THRegistry.getRewardRegistry().getRepeatItems(), "th_repeat_items");
    }

    private void addOneTimeItems(){
        oneTimeCandidates = resolveOneTimeItems();
        everPooled = new HashSet<>(oneTimeCandidates);
    }

    /**
     * Brings one-time treasures configured since this hunt began into the pool, so that packages
     * added by an update or a newly installed mod reach a campaign already in progress.
     * Entries already offered are tracked separately from the live pool so that claimed treasures
     * are not resurrected.
     */
    void syncNewlyConfigured(){
        Set<String> configured = resolveOneTimeItems();

        if (everPooled == null) {
            // Saved before this tracking existed. Anything that could have been configured back
            // then is treated as already offered; only dynamic packages, which could not have
            // existed in such a save, are genuinely new.
            everPooled = new HashSet<>(oneTimeCandidates);
            for (String token : configured) {
                if (THRewardItem.parse(token).getData() == null) everPooled.add(token);
            }
        }

        List<String> added = new ArrayList<>();
        for (String token : configured) {
            if (everPooled.add(token)) {
                oneTimeCandidates.add(token);
                added.add(token);
            }
        }
        if (!added.isEmpty()) {
            log.info("Added " + added.size() + " newly configured one-time treasures to the pool: " + added);
        }
    }

    private Set<String> resolveOneTimeItems(){
        Set<String> result = resolveConfiguredItems(THRegistry.getRewardRegistry().getOneTimeItems(), "th_one_time_items");
        result.addAll(resolveDynamicPackages());
        return result;
    }

    private Set<String> resolveConfiguredItems(List<String> configured, String sourceKey) {
        Set<String> wanted = new HashSet<>(configured);
        Set<String> result = new HashSet<>();
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (wanted.remove(item.getId())){
                result.add(item.getId());
            }
        }
        for (String missing : wanted) {
            log.warn("Item '" + missing + "' listed in " + sourceKey
                    + " has no special item spec and was skipped");
        }
        return result;
    }

    private Set<String> resolveDynamicPackages() {
        Set<String> result = new HashSet<>();
        if (Global.getSettings().getSpecialItemSpec(THDynamicPackagePlugin.ITEM_ID) == null) {
            log.warn("Missing spec " + THDynamicPackagePlugin.ITEM_ID + "; dynamic blueprint packages unavailable");
            return result;
        }
        var rewards = THRegistry.getRewardRegistry();
        for (String key : rewards.getAllBlueprintPackages()) {
            var pkg = rewards.getBlueprintPackage(key);
            if (pkg != null && pkg.isOneTime()) {
                result.add(THRewardItem.encode(THDynamicPackagePlugin.ITEM_ID, key));
            }
        }
        return result;
    }

    /** Refills the one-time pool with every configured one-time treasure. */
    public void regenerateOneTimePool(){
        addOneTimeItems();
    }

    public int getFullOneTimePoolSize(){
        return resolveOneTimeItems().size();
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
                        // Also match the bare id, for pooled items whose data field isn't part
                        // of their identity in the pool (a vanilla ship_bp, say).
                        removeItemsFromPool(List.of(
                                THRewardItem.from(specialItemData).getToken(),
                                specialItemData.getId()));
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

