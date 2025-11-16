package spinloki.TreasureHunt.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.Random;

public class THBaseScavengerSwarmFleetAssignmentAI extends RouteFleetAssignmentAI {
    protected String expeditionPurpose;

    public THBaseScavengerSwarmFleetAssignmentAI(CampaignFleetAPI fleet, RouteManager.RouteData route) {
        super(fleet, route);
        this.expeditionPurpose = "on a salvage expedition";
    }

    public THBaseScavengerSwarmFleetAssignmentAI(CampaignFleetAPI fleet, RouteManager.RouteData route, String expeditionPurpose) {
        super(fleet, route);
        this.expeditionPurpose = expeditionPurpose;
    }

    @Override
    protected String getTravelActionText(RouteManager.RouteSegment segment) {
        //if (segment.systemTo == route.getMarket().getContainingLocation()) {
        if (segment.to == route.getMarket().getPrimaryEntity()) {
            return "returning to " + route.getMarket().getName();
        }
        return expeditionPurpose;
    }

    @Override
    protected String getInSystemActionText(RouteManager.RouteSegment segment) {
        return "exploring";
    }


    @Override
    protected void addLocalAssignment(RouteManager.RouteSegment segment, boolean justSpawned) {
        boolean pickSpecificEntity = (float) Math.random() > 0.2f && !segment.from.getContainingLocation().isHyperspace();
        if (pickSpecificEntity) {
            SectorEntityToken target = RemnantSeededFleetManager.pickEntityToGuard(new Random(), (StarSystemAPI) segment.from.getContainingLocation(), fleet);
            if (target != null) {
                if (justSpawned) {
                    Vector2f loc = Misc.getPointAtRadius(new Vector2f(target.getLocation()), 500);
                    fleet.setLocation(loc.x, loc.y);
                }

                float speed = Misc.getSpeedForBurnLevel(8);
                float dist = Misc.getDistance(fleet.getLocation(), target.getLocation());
                float seconds = dist / speed;
                float days = seconds / Global.getSector().getClock().getSecondsPerDay();
                days += 5f + 5f * (float) Math.random();
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, target, days, "investigating");
                return;
            } else {
                if (justSpawned) {
                    Vector2f loc = Misc.getPointAtRadius(new Vector2f(), 8000);
                    fleet.setLocation(loc.x, loc.y);
                }

                float days = 5f + 5f * (float) Math.random();
                fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, null, days, "exploring");
            }
        } else {
            super.addLocalAssignment(segment, justSpawned);
        }
    }

    @Override
    public void pickNext(){
        super.pickNext();
        var current = route.getCurrent();
        if (current.isTravel()){
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true);
        }
        else{
            fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED);
        }
    }

    @Override
    public void setDone(){
        super.setDone();
        fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED);
    }
}
