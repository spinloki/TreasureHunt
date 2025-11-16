package spinloki.TreasureHunt.campaign.intel.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.coreui.V;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import spinloki.TreasureHunt.campaign.intel.THScavengerSwarmIntel;
import spinloki.TreasureHunt.config.THSettings;

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
            float value = 1;
            for (var entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)){
                var type = entity.getCustomEntityType();
                value += THSettings.getTHRewardValue(type);
            }
            float weight = (value * value) /
                    MathUtils.getDistanceSquared(system.getCenter().getLocationInHyperspace(), new Vector2f(0,0));
            picker.add(system, weight);
        }
        return picker.pick();
    }
}
