package spinloki.TreasureHunt.campaign.fleets;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RouteFleetAssignmentAI;

public class THScavengerSwarmFleetAssignmentAI extends RouteFleetAssignmentAI {
    public THScavengerSwarmFleetAssignmentAI(CampaignFleetAPI fleet, RouteManager.RouteData route) {
        super(fleet, route);
    }
}
