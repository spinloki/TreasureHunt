package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import spinloki.TreasureHunt.config.Settings;
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

    private Set<String> unseenOneTimeItems;
    private Set<String> unseenRepeatableItems;

    private void resetUnseenItems() {
        unseenRepeatableItems = new HashSet<>(COLONY_ITEMS);
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (item.hasTag(THUtils.TH_SPECIAL_ITEM)){
                unseenRepeatableItems.add(item.getId());
            }
        }
    }

    private void addOneTimeItems(){
        unseenOneTimeItems = new HashSet<>(BLUEPRINT_ITEMS);
        for (var item : Global.getSettings().getAllSpecialItemSpecs()){
            if (item.hasTag(THUtils.TH_BLUEPRINT_PACKAGE)){
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
        boolean pickBlueprint = (new Random().nextDouble() <= Settings.TH_PICK_BLUEPRINT_WEIGHT) && !unseenOneTimeItems.isEmpty();
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
        unseenOneTimeItems.remove(itemId);
        unseenRepeatableItems.remove(itemId);
    }
}

