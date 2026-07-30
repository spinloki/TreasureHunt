package spinloki.TreasureHunt.internal.items;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.MultiBlueprintItemPlugin;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.Color;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.internal.registry.THBlueprintPackage;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.util.Collections;
import java.util.List;

/**
 * A blueprint package identified by the cargo stack's data field rather than by its spec, so one
 * {@code special_items.csv} row backs every package defined in {@code th_blueprints_packages}.
 * Mirrors how {@code ShipBlueprintItemPlugin} carries a hull id.
 */
public class THDynamicPackagePlugin extends MultiBlueprintItemPlugin {
    private static final Logger log = Global.getLogger(THDynamicPackagePlugin.class);

    public static final String ITEM_ID = "th_bp_package";

    protected THBlueprintPackage pkg;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);

        String key = resolveKey(stack);
        pkg = key == null ? null : THRegistry.getRewardRegistry().getBlueprintPackage(key);

        if (pkg != null) {
            tags.clear();
            tags.addAll(pkg.getEffectiveTags());
        } else if (tags.isEmpty()) {
            log.error("Blueprint package could not be resolved (key=" + key + "); it will provide nothing");
        }
    }

    /** Data field first, then the spec's params column, which is what legacy per-package rows use. */
    private String resolveKey(CargoStackAPI stack) {
        SpecialItemData data = stack == null ? null : stack.getSpecialDataIfSpecial();
        String fromData = data == null ? null : data.getData();
        if (fromData != null && !fromData.trim().isEmpty()) return fromData.trim();

        String params = spec == null ? null : spec.getParams();
        if (params != null && !params.trim().isEmpty()) return params.trim();

        return null;
    }

    /** {@code getWeaponIds} and {@code getWingIds} return everything in the game on empty tags. */
    private boolean isUnresolved() {
        return tags.isEmpty();
    }

    @Override
    public List<String> getProvidedShips() {
        return isUnresolved() ? Collections.<String>emptyList() : super.getProvidedShips();
    }

    @Override
    public List<String> getProvidedWeapons() {
        return isUnresolved() ? Collections.<String>emptyList() : super.getProvidedWeapons();
    }

    @Override
    public List<String> getProvidedFighters() {
        return isUnresolved() ? Collections.<String>emptyList() : super.getProvidedFighters();
    }

    @Override
    public String getName() {
        if (pkg != null && pkg.getName() != null) return pkg.getName();
        return super.getName();
    }

    @Override
    public int getPrice(MarketAPI market, SubmarketAPI submarket) {
        if (pkg != null && pkg.hasPrice()) return (int) pkg.getPrice();
        return super.getPrice(market, submarket);
    }

    /**
     * Projects the package's largest entry into the holo plane of the package icon, using the
     * quad {@code MultiBlueprintItemPlugin} projects its own holo sprite into. Content lists are
     * size-sorted, so entry 0 is the biggest.
     */
    @Override
    public void render(float x, float y, float w, float h, float alphaMult,
                       float glowMult, SpecialItemRendererAPI renderer) {
        List<String> ships = getProvidedShips();
        List<String> fighters = getProvidedFighters();
        List<String> weapons = getProvidedWeapons();

        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float p = 1f;
        float blX = cx - 12f - p, blY = cy - 22f - p;
        float tlX = cx - 26f - p, tlY = cy + 19f + p;
        float trX = cx + 20f + p, trY = cy + 24f + p;
        float brX = cx + 34f + p, brY = cy - 9f - p;

        boolean known = areAllKnown(ships, weapons, fighters);

        if (renderEmblem(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, known, renderer)) return;

        String id = null;
        boolean isWeapon = false;

        String pinned = pkg == null ? null : pkg.getIconEntry();
        if (pinned != null && (ships.contains(pinned) || fighters.contains(pinned))) {
            id = pinned;
        } else if (pinned != null && weapons.contains(pinned)) {
            id = pinned; isWeapon = true;
        } else if (!ships.isEmpty()) {
            id = ships.get(0);
        } else if (!fighters.isEmpty()) {
            id = fighters.get(0);
        } else if (!weapons.isEmpty()) {
            id = weapons.get(0); isWeapon = true;
        }

        if (id == null) {
            super.render(x, y, w, h, alphaMult, glowMult, renderer);
            return;
        }

        if (isWeapon) {
            renderer.renderWeaponWithCorners(id, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult, glowMult * 0.5f, !known);
        } else {
            renderer.renderShipWithCorners(id, null, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult, glowMult * 0.5f, !known);
        }

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);

        if (known) {
            renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult * 0.5f, 0f, false);
        }
    }

    /** Returns false when no usable emblem is declared, so the caller falls back to a schematic. */
    private boolean renderEmblem(float blX, float blY, float tlX, float tlY,
                                 float trX, float trY, float brX, float brY,
                                 float alphaMult, boolean known, SpecialItemRendererAPI renderer) {
        if (pkg == null || pkg.getEmblem() == null) return false;

        SpriteAPI sprite = Global.getSettings().getSprite(pkg.getEmblem());
        if (sprite == null || sprite.getTextureId() == 0) return false;

        // Sprites are shared, so colour and alpha are always set rather than left from a prior pass.
        // White is identity: faction crests carry their own colours and a tint would flatten them.
        Color color = pkg.getEmblemColor();
        sprite.setColor(color == null ? Color.white : color);
        sprite.setAlphaMult(alphaMult);
        sprite.setNormalBlend();
        sprite.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);

        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);

        if (known) {
            renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult * 0.5f, 0f, false);
        }
        return true;
    }

    /** The spec is shared, so the per-package desc is swapped in only for the inherited build. */
    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded,
                              CargoTransferHandlerAPI transferHandler, Object stackSource) {
        if (pkg == null || pkg.getDesc() == null || spec == null) {
            super.createTooltip(tooltip, expanded, transferHandler, stackSource);
            return;
        }
        String saved = spec.getDesc();
        spec.setDesc(pkg.getDesc());
        try {
            super.createTooltip(tooltip, expanded, transferHandler, stackSource);
        } finally {
            spec.setDesc(saved);
        }
    }
}
