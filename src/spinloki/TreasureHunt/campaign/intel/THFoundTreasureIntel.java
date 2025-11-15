package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.listeners.ShowLootListener;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.Set;

public class THFoundTreasureIntel extends BaseIntelPlugin implements ShowLootListener {

    private final SpecialItemData treasure;
    private final String displayName;
    private boolean lootGiven = false;
    private final SpecialItemSpecAPI spec;

    public THFoundTreasureIntel(String treasure) {
        this.treasure = new SpecialItemData(treasure, null);
        this.displayName = THUtils.getSpecialItemDisplayName(treasure);
        this.spec = Global.getSettings().getSpecialItemSpec(treasure);

        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().getListenerManager().addListener(this);
    }

    @Override
    public void reportAboutToShowLootToPlayer(CargoAPI loot, InteractionDialogAPI dialog) {
        // Skip cargo pods so the player can't cheese it by ejecting & re-looting
        if (dialog != null) {
            SectorEntityToken target = dialog.getInteractionTarget();
            if (target != null) {
                String type = target.getCustomEntityType();
                if (Entities.CARGO_PODS.equals(type) ||
                        Entities.CARGO_POD_SPECIAL.equals(type)) {
                    return;
                }
            }
        }

        if (lootGiven) return;
        lootGiven = true;

        loot.addSpecial(treasure, 1);

        endAfterDelay();
        Global.getSector().getListenerManager().removeListener(this);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color titleColor = getTitleColor(mode);
        info.addPara(getName(), titleColor, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    protected void addBulletPoints(
            TooltipMakerAPI info,
            ListInfoMode mode,
            boolean isUpdate,
            Color tc,
            float initPad
    ) {
        Color h = Misc.getHighlightColor();

        if (mode == ListInfoMode.MESSAGES) {
            info.addPara("Location revealed: " + displayName, initPad, tc, h, displayName);
            return;
        }

        if (lootGiven) {
            info.addPara("Treasure recovered", initPad, tc, h, "recovered");
        } else {
            info.addPara("Treasure ready to claim", initPad, tc, h, "treasure");
        }
    }

    @Override
    public String getName() {
        return "Treasure Location Discovered - " + displayName;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        if (!lootGiven) {
            info.addPara("""
            You've uncovered information pointing to the location of a hidden treasure cache. You know where to look.
            """, 0f);
            info.addPara("""
            The next time you salvage anything, the treasure will be included.
            """, 0f);
        } else {
            info.addPara("""
            Using the information you uncovered, you've successfully located and recovered the treasure.
            """, 0f);
        }
    }

    @Override
    public String getIcon() {
        return spec.getIconName();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(THUtils.TH_TAG);
        return tags;
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().getListenerManager().removeListener(this);
    }
}
