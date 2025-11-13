package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import spinloki.TreasureHunt.campaign.intel.events.THSectorSprintFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

public class THSectorSprintIntel extends BaseIntelPlugin {
    private float daysRemaining = 60f;
    private final StarSystemAPI targetSystem;

    public THSectorSprintIntel(StarSystemAPI targetSystem, int duration) {
        this.targetSystem = targetSystem;
        daysRemaining = duration;
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
    }

    float timePassed = 0f;
    private List<SectorEntityToken> relays = new ArrayList<>();
    @Override
    public void advanceImpl(float amount) {
        if (Global.getSector().isPaused()) return;
        daysRemaining -= Global.getSector().getClock().convertToDays(amount);
        if (daysRemaining <= 0f) {
            endAfterDelay();
        }

        timePassed += amount;
        if (timePassed > .5f){
            timePassed = 0;
            var relays = targetSystem
                    .getEntitiesWithTag(Tags.COMM_RELAY)
                    .stream()
                    .filter(r -> r.getFaction() == Global.getSector().getPlayerFaction())
                    .toList();
            boolean playerOwnsRelay = !relays.isEmpty();
            if (playerOwnsRelay && relays.size() != this.relays.size()){
                TreasureHuntEventIntel.addFactorCreateIfNecessary(new THSectorSprintFactor(relays, this), null);
            }
            this.relays = relays;
        }
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
            info.addPara("Signal detected in remote systems", initPad);
            return;
        }

        if (isEnded() || isEnding()) {
            info.addPara("Signal has faded", initPad, tc, h, "faded");
            return;
        }

        info.addPara("Expires in %s days",
                initPad, tc, h, String.format("%.0f", daysRemaining));

        if (mode == ListInfoMode.INTEL || mode == ListInfoMode.MAP_TOOLTIP) {
            info.addPara("Resonant system: %s", 0f, tc, h,
                    String.valueOf(targetSystem.getName()));
        }
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);

    }

    @Override
    public String getSortString() {
        return "Sector Sprint";
    }

    @Override
    public String getName() {
        String base = "Sector Sprint";
        return base + " - " + targetSystem.getName();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color p = Misc.getBasePlayerColor();
        Color pDark = Misc.getDarkPlayerColor();

        info.addPara("""
        A powerful hyperwave ping is resonating across the sector and interacting strangely with certain \
        remote systems. Controlling Comm Relays in the resonant systems will yield great progress toward \
        your treasure hunt. The signal is rapidly weakening and will eventually fade into the background \
        noise of hyperspace comms.
        """, 0f);

        if (isEnded() || isEnding()) {
            info.addPara("The hyperwave signal has faded.", opad);
        } else {
            info.addPara("The resonance will persist for approximately %s days.",
                    opad,
                    h,
                    String.format("%.0f", daysRemaining));
        }

        if (targetSystem != null) {
            info.addSectionHeading("Resonant System", p, pDark, Alignment.MID, opad);
            info.addPara("• %s", 3f, h, targetSystem.getName());
        }
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("treasure_hunt_events", "sector_sprint");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(THUtils.TH_TAG);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return targetSystem.getCenter();
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }
}
