package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class THScavengerSwarmIntel extends BaseIntelPlugin {
    private final StarSystemAPI target;
    private final String icon;
    private List<FactionAPI> factions = new ArrayList<>();
    private static final String displayName = "Scavenger Swarm";

    public THScavengerSwarmIntel(StarSystemAPI target, String icon){
        this.target = target;
        this.icon = icon;
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
        factions.add(Global.getSector().getFaction(Factions.PIRATES));
        factions.add(Global.getSector().getFaction(Factions.HEGEMONY));
        factions.add(Global.getSector().getFaction(Factions.PERSEAN));
        factions.add(Global.getSector().getFaction(Factions.TRITACHYON));
        factions.add(Global.getSector().getFaction(Factions.INDEPENDENT));
        factions.add(Global.getSector().getFaction(Factions.LUDDIC_CHURCH));
        factions.add(Global.getSector().getFaction(Factions.LUDDIC_PATH));
        factions.add(Global.getSector().getFaction(Factions.DIKTAT));
    }

    private float timePassed = 0;
    private final float interval = 1;
    @Override
    public void advanceImpl(float amount){
        if (Global.getSector().isPaused()) return;

        if (factions.isEmpty()){
            endAfterDelay();
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        timePassed += days;
        if (timePassed > interval){
            timePassed = 0;
            factions.remove(0);
        }
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color titleColor = getTitleColor(mode);
        info.addPara(getName(), titleColor, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color p = Misc.getBasePlayerColor();
        Color pDark = Misc.getDarkPlayerColor();

        if (!(isEnded() || isEnding())) {
            info.addPara("Several factions are converging on the unclaimed system of "
                    + target.getCenter().getFullName()
                    + ", pursuing rumors of lost technology."
            , 0f);

            info.addPara("""
            Whether or not these rumors are true, these fleets likely have valuable information that would aid your hunt.
            """, opad);
        } else {
            info.addPara("""
            Whether due to high costs, casualties, or disappointingly few finds, factions are no longer swarming the system.
            """, opad);
        }
        if (!(isEnded() || isEnding())) {
            info.addSectionHeading("Involved Factions", Alignment.MID, 10f);
            bullet(info);
            for (var faction : factions){
                info.addPara(Misc.ucFirst(faction.getDisplayName()), 3f, faction.getBaseUIColor(), Misc.ucFirst(faction.getDisplayName()));
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
            info.addPara("Factions are swarming " + target.getCenter().getFullName(), initPad, tc, h, displayName);
            return;
        }

        if (isEnding() || isEnded()) {
            info.addPara("Factions have lost interest", initPad, tc, h, "lost interest");
        } else {
            info.addPara("Factions swarming system", initPad, tc, h, "treasure");
        }
    }

    @Override
    public String getName() {
        return displayName + " - " + target.getCenter().getFullName();
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
        return target.getCenter();
    }

    @Override
    public void notifyEnded(){
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }
}
