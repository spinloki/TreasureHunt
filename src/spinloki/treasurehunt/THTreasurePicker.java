package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    THTreasurePicker(){
        resetUnseenItems();
        addOneTimeItems();
        Global.getSector().getListenerManager().addListener(this);
    }

    private static final Set<String> COLONY_ITEMS = Set.of(
            Items.PRISTINE_NANOFORGE,
            Items.SYNCHROTRON,
            Items.ORBITAL_FUSION_LAMP,
            Items.MANTLE_BORE,
            Items.CATALYTIC_CORE,
            Items.SOIL_NANITES,
            Items.BIOFACTORY_EMBRYO,
            Items.FULLERENE_SPOOL,
            Items.PLASMA_DYNAMO,
            Items.CRYOARITHMETIC_ENGINE,
            Items.DRONE_REPLICATOR,
            Items.DEALMAKER_HOLOSUITE,
            Items.CORONAL_PORTAL
    );

    private static final Set<String> BLUEPRINT_ITEMS = Set.of(
            Items.LOW_TECH_PACKAGE,
            Items.MIDLINE_PACKAGE,
            Items.HIGH_TECH_PACKAGE,
            Items.MISSILE_PACKAGE
    );

    private Set<String> unseenItems;

    private void resetUnseenItems() {
        unseenItems = new HashSet<>(COLONY_ITEMS);
    }

    private void addOneTimeItems(){
        unseenItems.addAll(BLUEPRINT_ITEMS);
    }

    public String getRandomUnseenItem() {
        if (unseenItems.isEmpty()) {
            resetUnseenItems();
        }

         List<String> itemList = new ArrayList<>(unseenItems);
        String chosen = itemList.get(new Random().nextInt(itemList.size()));
        unseenItems .remove(chosen);

        return chosen;
    }

    public static String getSpecialItemDisplayName(String specialItemId) {
        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(specialItemId);
        if (spec != null) {
            return spec.getName();
        } else {
            return specialItemId; // fallback if not found
        }
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
        unseenItems.remove(itemId); // In case you need to mark one manually
    }

    public Set<String> getUnseenItems() {
        return Collections.unmodifiableSet(unseenItems);
    }
}

