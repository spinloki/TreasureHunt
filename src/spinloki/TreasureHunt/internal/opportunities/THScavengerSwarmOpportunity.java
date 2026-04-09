package spinloki.TreasureHunt.internal.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.internal.intel.THScavengerSwarmIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;

public class THScavengerSwarmOpportunity extends BaseTHOpportunity {

    @Override
    public void trigger() {
        super.trigger();
        var system = pickTargetSystem();
        new THScavengerSwarmIntel(system, getIconPath());
    }

    @Override
    public String getDisplayName() {
        return "Scavenger Swarm";
    }

    @Override
    public String getIcon() {
        return "scavenger_swarm";
    }

    private StarSystemAPI pickTargetSystem() {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!system.isProcgen()) continue;
            boolean interesting = system.hasTag(Tags.THEME_INTERESTING) || system.hasTag(Tags.THEME_INTERESTING_MINOR);
            if (!interesting) continue;
            float value = 1;
            for (var entity : system.getEntitiesWithTag(Tags.SALVAGEABLE)){
                var type = entity.getCustomEntityType();
                value += THRegistry.getRewardRegistry().getRewardValue(type);
            }
            float weight = (value * value) /
                    MathUtils.getDistanceSquared(system.getCenter().getLocationInHyperspace(), new Vector2f(0,0));
            picker.add(system, weight);
        }
        return picker.pick();
    }
}
