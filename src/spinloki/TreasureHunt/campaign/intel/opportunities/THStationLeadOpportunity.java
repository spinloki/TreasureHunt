package spinloki.TreasureHunt.campaign.intel.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import spinloki.TreasureHunt.campaign.intel.THStationLeadIntel;
import spinloki.TreasureHunt.campaign.intel.events.THStationsRanOutFactor;
import spinloki.TreasureHunt.campaign.intel.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.*;

public class THStationLeadOpportunity extends BaseTHOpportunity{
    private final int numStationsToReveal = 3;
    private boolean ranOutOfStuff = false;
    private final int baseProgressReward = 50;
    private final List<String> findables = new ArrayList<>(List.of(
            "Research Station",
            "Mining Station",
            "Orbital Habitat"
    ));
    private final Set<String> revealed = new HashSet<>();
    private final List<String> locations = new ArrayList<>();

    @Override
    public float getProbabilityWeight() {
        return ranOutOfStuff ? 0 : super.getProbabilityWeight();
    }

    @Override
    public void trigger() {
        super.trigger();
        List<SectorEntityToken> toReveal = new ArrayList<>();
        for (var findable : findables){
            var entities = THUtils.getNearestEntitiesWithName(Global.getSector().getPlayerFleet(),
                    1000,
                    findable,
                    true)
                    .stream()
                    .filter(SectorEntityToken::isDiscoverable) // should exclude things that are already discovered
                    .filter(e -> !revealed.contains(e.getId()))
                    .toList();
            toReveal.addAll(entities);
        }
        if (toReveal.size() < numStationsToReveal){
            ranOutOfStuff = true;
            TreasureHuntEventIntel.addFactorCreateIfNecessary(
                    new THStationsRanOutFactor(baseProgressReward * (numStationsToReveal - toReveal.size())),
                    null);
        }
        toReveal.sort(Comparator.comparingDouble(e -> THUtils.getCrossSystemDistance(Global.getSector().getPlayerFleet(), e)));

        for (var e : toReveal.stream().limit(numStationsToReveal).toList()){
            revealed.add(e.getId());
            new THStationLeadIntel(e, BreadcrumbSpecial.getLocatedString(e, false));
        }
    }
}
