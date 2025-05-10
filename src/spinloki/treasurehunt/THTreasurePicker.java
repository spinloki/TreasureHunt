package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import org.apache.log4j.Logger;

import java.util.*;

public class THTreasurePicker implements ShowLootListener {
    private static final Logger log = Logger.getLogger(THTreasurePicker.class);

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
                    var obj = Global.getSettings().getJSONObject(THConstants.TH_BLUEPRINT_PRIORITY_QUEUE);
                    return obj.optInt(hullId, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return 0;
                }
            }
        });

        try {
            var priorityData = Global.getSettings().getJSONObject(THConstants.TH_BLUEPRINT_PRIORITY_QUEUE);
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

    private static final Set<String> TREASURE_TYPES = Set.of(
            "item",
            "blueprint"
    );

    private PriorityQueue<String> shipBlueprints;

    private Set<String> unseenItems;

    private Set<String> treasureTypes = new HashSet<>();

    private String lastChosenType = "item";

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

        List<String> typeList = new ArrayList<>(treasureTypes);
        String chosenType = typeList.get(new Random().nextInt(typeList.size()));
        lastChosenType = chosenType;

        String chosenTreasure = null;

        if (Objects.equals(chosenType, "blueprint")){
            chosenTreasure = getShipFromPriorityQueue();
            if (chosenTreasure == null){
                treasureTypes.remove("blueprint");
                return getRandomUnseenItem();
            }
        }

        if (Objects.equals(chosenType, "item")){
            List<String> itemList = new ArrayList<>(unseenItems);
            chosenTreasure = itemList.get(new Random().nextInt(itemList.size()));
            unseenItems.remove(chosenTreasure);
        }

        return chosenTreasure;
    }

    public String getLastChosenType(){
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

