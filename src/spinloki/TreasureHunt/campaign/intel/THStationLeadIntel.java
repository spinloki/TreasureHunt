package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.Set;

public class THStationLeadIntel extends BaseIntelPlugin {
    SectorEntityToken station;
    String locationDescription;
    private final String icon;

    public THStationLeadIntel(SectorEntityToken station, String locationDescription, String icon){
        this.station = station;
        this.locationDescription = locationDescription;
        this.icon = icon;
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
    }

    @Override
    public void advanceImpl(float amount) {
        if (Global.getSector().isPaused()) return;
        if (!station.isDiscoverable()){
            endAfterDelay();
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
            info.addPara("Learned of a derelict station", initPad);
            return;
        }

        if (isEnded() || isEnding()) {
            info.addPara("Station discovered", initPad);
            return;
        }
        if (mode == ListInfoMode.INTEL || mode == ListInfoMode.MAP_TOOLTIP) {
            info.addPara("Located in %s", 0f, tc, h,
                    String.valueOf(station.getConstellation().getName()));
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
        return station.getFullName() + " Lead";
    }

    @Override
    public String getName() {
        String base = station.getFullName() + " Lead";
        return base + " - " + station.getConstellation().getNameWithType();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color p = Misc.getBasePlayerColor();
        Color pDark = Misc.getDarkPlayerColor();

        info.addPara("New intel suggests the existence of an undiscovered derelict " + station.getFullName(), 0f);

        if (isEnded() || isEnding()) {
            info.addPara("Station discovered", opad);
        } else {
            info.addPara("The intel indicates that it is " + locationDescription,
                    opad);
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
        Constellation c = station.getConstellation();
        SectorEntityToken entity = null;
        if (c != null && map != null) {
            entity = map.getConstellationLabelEntity(c);
        }
        if (entity == null) entity = station;
        return entity;
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }
}
