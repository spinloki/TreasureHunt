package spinloki.TreasureHunt.campaign.intel.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;

import java.util.List;

public class THScavengerSwarmOpportunity extends BaseTHOpportunity {
    private static final String icon = Global.getSettings().getSpriteName("treasure_hunt_events", "scavenger_swarm");

    @Override
    public void trigger() {
        super.trigger();
        var system = pickTargetSystem();
        new THScavengerSwarmIntel(system, icon);
    }

    @Override
    public String getIcon() {
        return icon;
    }

    private StarSystemAPI pickTargetSystem() {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            boolean interesting = system.hasTag(Tags.THEME_INTERESTING) || system.hasTag(Tags.THEME_INTERESTING_MINOR);
            if (!interesting) continue;
            float weight = 1f; // TODO: Weight unvisited systems more
            picker.add(system, weight);
        }
        return picker.pick();
    }
}
