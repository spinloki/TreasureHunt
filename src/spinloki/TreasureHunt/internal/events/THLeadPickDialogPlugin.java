package spinloki.TreasureHunt.internal.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.impl.items.MultiBlueprintItemPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;
import spinloki.TreasureHunt.internal.registry.THRewardRegistry;
import spinloki.TreasureHunt.util.THRewardItem;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class THLeadPickDialogPlugin implements InteractionDialogPlugin {

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;

    private final Set<String> candidates;
    private String chosen = null;

    public THLeadPickDialogPlugin(Set<String> candidates) {
        this.candidates = candidates;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();

        text.setFontOrbitronUnnecessarilyLarge();
        text.addPara("Treasure Hunt - Choose Lead", Misc.getHighlightColor());

        buildUI();
    }

    private void buildUI() {
        text.setFontInsignia();
        text.addPara("You have uncovered several promising leads. You can pick one to follow or ignore them all so that none of them soon reappear.");

        for (String token : candidates) {

            THRewardItem reward = THRewardItem.parse(token);
            if (!reward.isValid()) continue;

            String name = reward.getDisplayName();

            // Smaller font for tighter layout
            text.setFontSmallInsignia();

            text.addPara(name, Misc.getHighlightColor());

            // Shortened description instead of full spec text
            String desc = reward.getDescription();
            if (desc != null && !desc.isEmpty()) {
                text.addPara(desc);
            }
            options.addOption(name, token);
            addItemTooltip(token);
        }

        options.addOption("None of these", "cancel");
    }

    private void addItemTooltip(String token) {
        THRewardItem reward = THRewardItem.parse(token);
        var spec = reward.getSpec();
        if (spec == null) return;

        options.addOptionTooltipAppender(token, (TooltipMakerAPI tooltip, boolean hadOtherText) -> {
            float pad = hadOtherText ? 10f : 0f;

            String desc = reward.getDescription();
            if (desc == null || desc.isEmpty()) desc = spec.getDescFirstPara();
            if (desc != null && !desc.isEmpty()) {
                tooltip.addPara(desc, pad);
                pad = 10f;
            }

            addBlueprintSection(tooltip, token, pad);
        });
    }

    /** Data field, then the legacy {@code <key>_package} convention, then the spec's params tags. */
    private void addBlueprintSection(TooltipMakerAPI tooltip, String token, float pad) {
        List<String> ships = new ArrayList<>();
        List<String> weapons = new ArrayList<>();
        List<String> fighters = new ArrayList<>();

        THRewardItem reward = THRewardItem.parse(token);
        String itemId = reward.getItemId();
        THRewardRegistry rewards = THRegistry.getRewardRegistry();

        String packageKey = reward.getData();
        if (packageKey == null && itemId != null && itemId.endsWith("_package")) {
            String candidate = itemId.substring(0, itemId.length() - "_package".length());
            if (rewards.hasBlueprintPackage(candidate)) packageKey = candidate;
        }

        THBlueprintPackage pkg = packageKey == null ? null : rewards.getBlueprintPackage(packageKey);

        if (pkg != null && !pkg.usesTagExpression()) {
            ships = pkg.getShips();
            weapons = pkg.getWeapons();
            fighters = pkg.getFighters();
        } else {
            Set<String> tags = null;
            if (pkg != null) {
                tags = pkg.getEffectiveTags();
            } else {
                var spec = reward.getSpec();
                if (spec != null && spec.getParams() != null && !spec.getParams().trim().isEmpty()) {
                    tags = new LinkedHashSet<>();
                    for (String tag : spec.getParams().split(",")) {
                        tag = tag.trim();
                        if (!tag.isEmpty()) tags.add(tag);
                    }
                }
            }
            if (tags != null && !tags.isEmpty()) {
                ships = MultiBlueprintItemPlugin.getShipIds(tags);
                weapons = MultiBlueprintItemPlugin.getWeaponIds(tags);
                fighters = MultiBlueprintItemPlugin.getWingIds(tags);
            }
        }

        if (ships.isEmpty() && weapons.isEmpty() && fighters.isEmpty()) return;

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        Color knownColor = Misc.getGrayColor();
        Color newColor = Misc.getHighlightColor();

        if (!ships.isEmpty()) {
            tooltip.addPara("Ships:", pad);
            pad = 0f;
            for (String id : ships) {
                var hullSpec = Global.getSettings().getHullSpec(id);
                String name = hullSpec != null ? hullSpec.getHullName() : id;
                boolean known = playerFaction.knowsShip(id);
                String label = "  " + name + (known ? " (known)" : "");
                tooltip.addPara(label, 0f, known ? knownColor : newColor, name);
            }
        }

        if (!weapons.isEmpty()) {
            tooltip.addPara("Weapons:", pad > 0 ? pad : 10f);
            for (String id : weapons) {
                var weaponSpec = Global.getSettings().getWeaponSpec(id);
                String name = weaponSpec != null ? weaponSpec.getWeaponName() : id;
                boolean known = playerFaction.knowsWeapon(id);
                String label = "  " + name + (known ? " (known)" : "");
                tooltip.addPara(label, 0f, known ? knownColor : newColor, name);
            }
        }

        if (!fighters.isEmpty()) {
            tooltip.addPara("Fighters:", pad > 0 ? pad : 10f);
            for (String id : fighters) {
                var wingSpec = Global.getSettings().getFighterWingSpec(id);
                String name = wingSpec != null ? wingSpec.getWingName() : id;
                boolean known = playerFaction.knowsFighter(id);
                String label = "  " + name + (known ? " (known)" : "");
                tooltip.addPara(label, 0f, known ? knownColor : newColor, name);
            }
        }
    }
    @Override
    public void optionSelected(String optionText, Object optionData) {

        if (optionData == null) return;

        String id = optionData.toString();

        if ("cancel".equals(id)) {
            dialog.dismiss();
            return;
        }

        chosen = id;

        dialog.dismiss();
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    public String getChosen() {
        return chosen;
    }

    @Override
    public void advance(float amount) { }

    @Override
    public void backFromEngagement(EngagementResultAPI result) { }

    @Override
    public Object getContext() { return null; }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() { return null; }
}