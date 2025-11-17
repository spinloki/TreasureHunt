package spinloki.TreasureHunt.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import spinloki.TreasureHunt.campaign.fleets.THScavengerSwarmRouteFleetManager;
import spinloki.TreasureHunt.util.THUtils;

import java.awt.*;
import java.util.*;

public class THScavengerSwarmIntel extends BaseIntelPlugin {
    private final StarSystemAPI target;
    private final String icon;
    private static final String displayName = "Scavenger Swarm";
    private final THScavengerSwarmRouteFleetManager routeManager;
    public float daysRemaining = 180f;

    // Maps factionId to fleet AI
    private static final Map<FactionAPI, Pair<THSwarmAICreator, THSwarmFleetCreator>> factions = new HashMap<>();

    public static void addFactionWithAIAndFleetCreators(String factionId, THSwarmAICreator aiCreator, THSwarmFleetCreator fleetCreator){
        factions.put(Global.getSector().getFaction(factionId), new Pair<>(aiCreator, fleetCreator));
    }

    public static Map<FactionAPI, Pair<THSwarmAICreator, THSwarmFleetCreator>> getFactionsWithAIAndFleetCreators(){
        return factions;
    }

    public static void resetFactionsWithAIAndFleetCreators(){
        factions.clear();
    }

    public interface THSwarmAICreator {
        RouteFleetAssignmentAI create(CampaignFleetAPI fleet, RouteData route);
    }
    public interface THSwarmFleetCreator{
        CampaignFleetAPI createFleet(
                StarSystemAPI system,
                RouteData route,
                MarketAPI sourceMarket,
                Random random
        );
    }

    public THScavengerSwarmIntel(StarSystemAPI target, String icon){
        this.target = target;
        this.icon = icon;
        Global.getSector().getIntelManager().addIntel(this);
        Global.getSector().addScript(this);
        routeManager = new THScavengerSwarmRouteFleetManager(target);
        target.addScript(routeManager);
    }

    @Override
    public void advanceImpl(float amount){
        if (Global.getSector().isPaused()) return;

        if (factions.isEmpty()){
            endAfterDelay();
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        daysRemaining -= days;
        if (daysRemaining < 0f){
            endAfterDelay();
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

            for (var faction : factions.keySet()){
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
            info.addPara("Factions swarming system", initPad);
            info.addPara((int)daysRemaining + " days remaining", initPad, tc, h, "" + (int)daysRemaining);
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
        target.removeScript(routeManager);
    }
}
