package spinloki.TreasureHunt.internal.opportunities;

import com.fs.starfarer.api.Global;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.internal.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.Random;

public class THSectorSprintOpportunity extends BaseTHOpportunity{
    private final int numIntelsToCreate = 3;

    @Override
    public void trigger() {
        super.trigger();
        int time = 90;
        String iconPath = getIconPath();
        for (var system : THUtils.getSectorSprintCandidates(numIntelsToCreate)){
            new THSectorSprintIntel(system, time + new Random().nextInt(9) - 4, iconPath);
            time -= 30;
        }
    }

    @Override
    public String getDisplayName() {
        return "Sector Sprint";
    }

    @Override
    public String getIcon(){
        return "sector_sprint";
    }
}
