package spinloki.TreasureHunt.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.Objects;

/**
 * A treasure pool entry: a special item id plus the optional data field dynamic items carry.
 * Serialized as an {@code id} or {@code id|data} string, because the pools live on a non-transient
 * field of {@code TreasureHuntEventIntel} and a real value type would not deserialize old saves.
 */
public class THRewardItem {
    private static final String SEP = "|";

    private final String itemId;
    private final String data;

    private THRewardItem(String itemId, String data) {
        this.itemId = itemId;
        this.data = data;
    }

    public static THRewardItem of(String itemId, String data) {
        return new THRewardItem(itemId, data == null || data.isEmpty() ? null : data);
    }

    public static THRewardItem parse(String token) {
        if (token == null) return new THRewardItem(null, null);
        int idx = token.indexOf(SEP);
        if (idx < 0) return new THRewardItem(token, null);
        String id = token.substring(0, idx);
        String data = token.substring(idx + SEP.length());
        return new THRewardItem(id, data.isEmpty() ? null : data);
    }

    /** Builds the token matching a looted stack, so pool removal can distinguish dynamic items. */
    public static THRewardItem from(SpecialItemData stackData) {
        if (stackData == null) return new THRewardItem(null, null);
        return of(stackData.getId(), stackData.getData());
    }

    public static String encode(String itemId, String data) {
        return of(itemId, data).getToken();
    }

    public String getItemId() { return itemId; }
    public String getData() { return data; }

    public String getToken() {
        if (itemId == null) return "";
        return data == null ? itemId : itemId + SEP + data;
    }

    public SpecialItemData toSpecialItemData() {
        return new SpecialItemData(itemId, data);
    }

    public SpecialItemSpecAPI getSpec() {
        return itemId == null ? null : Global.getSettings().getSpecialItemSpec(itemId);
    }

    public boolean isValid() {
        return getSpec() != null;
    }

    /** Prefers a package's configured name; the shared spec name reads the same for all of them. */
    public String getDisplayName() {
        if (data != null) {
            THBlueprintPackage pkg = THRegistry.getRewardRegistry().getBlueprintPackage(data);
            if (pkg != null && pkg.getName() != null) return pkg.getName();
        }
        SpecialItemSpecAPI spec = getSpec();
        return spec != null ? spec.getName() : String.valueOf(itemId);
    }

    public String getDescription() {
        if (data != null) {
            THBlueprintPackage pkg = THRegistry.getRewardRegistry().getBlueprintPackage(data);
            if (pkg != null && pkg.getDesc() != null) return pkg.getDesc();
        }
        SpecialItemSpecAPI spec = getSpec();
        return spec != null ? spec.getDesc() : null;
    }

    public String getIconName() {
        if (data != null) {
            THBlueprintPackage pkg = THRegistry.getRewardRegistry().getBlueprintPackage(data);
            if (pkg != null && pkg.getIcon() != null) return pkg.getIcon();
        }
        SpecialItemSpecAPI spec = getSpec();
        return spec != null ? spec.getIconName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof THRewardItem)) return false;
        THRewardItem other = (THRewardItem) o;
        return Objects.equals(itemId, other.itemId) && Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, data);
    }

    @Override
    public String toString() {
        return getToken();
    }
}
