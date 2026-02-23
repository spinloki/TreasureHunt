package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.events.factors.THSectorSprintFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;

import java.awt.*;
import java.util.*;
import java.util.List;

public class THSectorSprintIntel extends BaseIntelPlugin {
    private float daysRemaining = 60f;
    private final StarSystemAPI targetSystem;
    private final String icon;

    private static final String MEDDLER_FLEET_ID_PREFIX = "th_sectorSprint_meddler_";
    private String meddlerFleetId = null;
    private boolean meddlingActive = false;

    public boolean isMeddlingActive() {
        return meddlingActive;
    }

    public THSectorSprintIntel(StarSystemAPI targetSystem, int duration, String icon) {
        this.targetSystem = targetSystem;
        daysRemaining = duration;
        this.icon = icon;
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
        initMeddlerIfPlayerRelayPresent();
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
            if (meddlerFleetId != null) {
                SectorEntityToken e = Global.getSector().getEntityById(meddlerFleetId);
                if (e instanceof CampaignFleetAPI) {
                    meddlingActive = ((CampaignFleetAPI) e).isAlive();
                } else {
                    meddlingActive = false;
                }
            }
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
        return icon;
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

        if (meddlerFleetId != null) {
            SectorEntityToken e = Global.getSector().getEntityById(meddlerFleetId);
            if (e != null) e.setExpired(true);
        }
    }

    private void initMeddlerIfPlayerRelayPresent() {
        // Only spawn if the player ALREADY owns a relay here at the time this intel is created
        SectorEntityToken relay = targetSystem.getEntitiesWithTag(Tags.COMM_RELAY).stream()
                .filter(r -> r.getFaction() == Global.getSector().getPlayerFaction())
                .findFirst()
                .orElse(null);

        if (relay == null) return;

        String id = MEDDLER_FLEET_ID_PREFIX + targetSystem.getId();
        SectorEntityToken existing = Global.getSector().getEntityById(id);
        if (existing instanceof CampaignFleetAPI && existing.getStarSystem() == targetSystem) {
            meddlerFleetId = id;
            meddlingActive = ((CampaignFleetAPI) existing).isAlive();
            return;
        }

        CampaignFleetAPI fleet = spawnMeddlerFleet(targetSystem, relay, id);
        if (fleet != null) {
            meddlerFleetId = id;
            meddlingActive = true;
        }
    }

    private CampaignFleetAPI spawnMeddlerFleet(StarSystemAPI system, SectorEntityToken relay, String fleetId) {
        float fp = 60f; // tune this (or make it a THSettings value)

        FleetParamsV3 params = new FleetParamsV3(
                        null,
                        system.getLocation(),
                        Factions.PIRATES,
                        null,
                        FleetTypes.RAIDER,
                        40f + new Random().nextFloat() * 60,  // combat
                        10f,  // freighter
                        5f,   // tanker
                        0f, 0f, 0f, 0f
                );

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null) return null;

        fleet.setId(fleetId);
        fleet.setNoAutoDespawn(true);

        // Make it a problem for the player
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);

        system.addEntity(fleet);

        // place & assign
        fleet.setLocation(relay.getLocation().x + 300f, relay.getLocation().y + 300f);
        fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, relay, 999999f, "meddling with the comm relay");

        return fleet;
    }
}
