package spinloki.treasurehunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import spinloki.treasurehunt.config.Settings;
import spinloki.treasurehunt.util.THUtils;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    THTreasurePicker(){
        resetUnseenItems();
        addOneTimeItems();
        Global.getSector().getListenerManager().addListener(this);
    }

    public static PriorityQueue<String> setupShipBlueprintQueue() {
        PriorityQueue<String> queue = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int p1 = getPriority(o1);
                int p2 = getPriority(o2);
                return Integer.compare(p1, p2); // lower values = higher priority
            }

            private int getPriority(String hullId) {
                try {
                    var obj = Settings.TH_BLUEPRINT_PRIORITY_QUEUE;
                    return obj.optInt(hullId, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return 0;
                }
            }
        });

        try {
            var priorityData = Settings.TH_BLUEPRINT_PRIORITY_QUEUE;
            Iterator<String> keys = priorityData.keys();

            while (keys.hasNext()) {
                String hullId = keys.next();
                queue.add(hullId);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return queue;
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

    private static final Set<THUtils.TreasureType> TREASURE_TYPES = Set.of(
            THUtils.TreasureType.ITEM,
            THUtils.TreasureType.SHIP_BLUEPRINT
    );

    private PriorityQueue<String> shipBlueprints;

    private Set<String> unseenItems;

    private final Set<THUtils.TreasureType> treasureTypes = new HashSet<>();

    private THUtils.TreasureType lastChosenType = THUtils.TreasureType.ITEM;

    private void resetUnseenItems() {
        unseenItems = new HashSet<>(COLONY_ITEMS);
    }

    private void addOneTimeItems(){
        unseenItems.addAll(BLUEPRINT_ITEMS);
        shipBlueprints = setupShipBlueprintQueue();
        treasureTypes.addAll(TREASURE_TYPES);
    }

    public String getRandomUnseenItem() {
        if (unseenItems.isEmpty()) {
            resetUnseenItems();
        }

        List<THUtils.TreasureType> typeList = new ArrayList<>(treasureTypes);
        THUtils.TreasureType chosenType = typeList.get(new Random().nextInt(typeList.size()));
        lastChosenType = chosenType;

        String chosenTreasure = null;

        if (chosenType == THUtils.TreasureType.SHIP_BLUEPRINT){
            chosenTreasure = getShipFromPriorityQueue();
            if (chosenTreasure == null){
                treasureTypes.remove(THUtils.TreasureType.SHIP_BLUEPRINT);
                return getRandomUnseenItem();
            }
        }

        if (chosenType == THUtils.TreasureType.ITEM){
            List<String> itemList = new ArrayList<>(unseenItems);
            chosenTreasure = itemList.get(new Random().nextInt(itemList.size()));
            unseenItems.remove(chosenTreasure);
        }

        return chosenTreasure;
    }

    public THUtils.TreasureType getLastChosenType(){
        return lastChosenType;
    }

    public String getShipFromPriorityQueue(){
        if (shipBlueprints.isEmpty()){
            return null;
        }
        var blueprint = shipBlueprints.poll();
        if(Global.getSector().getPlayerFaction().knowsShip(blueprint)){
            return getShipFromPriorityQueue();
        }
        return blueprint;
    }

    public static String getSpecialItemDisplayName(String specialItemId) {
        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(specialItemId);
        if (spec != null) {
            return spec.getName();
        } else {
            return specialItemId; // fallback if not found
        }
    }

    public static String getShipBlueprintDisplayName(String shipId){
        ShipHullSpecAPI spec = Global.getSettings().getHullSpec(shipId);
        if (spec != null){
            return spec.getHullName() + " blueprint";
        }
        return shipId + " blueprint";
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

