package spinloki.TreasureHunt.internal.opportunities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import spinloki.TreasureHunt.api.BaseTHOpportunity;
import spinloki.TreasureHunt.internal.intel.THStationLeadIntel;
import spinloki.TreasureHunt.internal.factors.THStationsRanOutFactor;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.util.THUtils;

import java.util.*;
import java.util.Map;

public class THStationLeadOpportunity extends BaseTHOpportunity{
    private static final String PERSISTENCE_KEY = "th_station_lead_revealed";
    private final int numStationsToReveal = 3;
    private boolean ranOutOfStuff = false;
    private final int baseProgressReward = 50;
    private final List<String> findables = new ArrayList<>(List.of(
            "Research Station",
            "Mining Station",
            "Orbital Habitat"
    ));

    @SuppressWarnings("unchecked")
    private Set<String> getRevealed() {
        Map<String, Object> data = Global.getSector().getPersistentData();
        Set<String> revealed = (Set<String>) data.get(PERSISTENCE_KEY);
        if (revealed == null) {
            revealed = new HashSet<>();
            data.put(PERSISTENCE_KEY, revealed);
        }
        return revealed;
    }

    @Override
    public float getProbabilityWeight() {
        return ranOutOfStuff ? 0 : super.getProbabilityWeight();
    }

    @Override
    public void trigger() {
        super.trigger();
        Set<String> revealed = getRevealed();
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
            new THStationLeadIntel(e, BreadcrumbSpecial.getLocatedString(e, false), getIconPath());
        }
    }

    @Override
    public String getDisplayName() {
        return "Station Lead";
    }

    @Override
    public String getIcon(){
        return "station_lead";
    }
}
