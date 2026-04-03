package spinloki.TreasureHunt.internal.factors;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.internal.registry.THRegistry;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class THSectorSprintFactor  extends BaseEventFactor {
    Set<String> relayIds;
    StarSystemAPI starSystem;
    boolean hasDomainEraRelay;
    int BASE_PROGRESS;
    THSectorSprintIntel parent;

    public THSectorSprintFactor(List<SectorEntityToken> relays, THSectorSprintIntel parent){
        super();
        this.relayIds = relays.stream().map(SectorEntityToken::getId).collect(Collectors.toSet());
        this.parent = parent;
        this.starSystem = relays.get(0).getStarSystem();
        BASE_PROGRESS = THRegistry.getSettings().getSectorSprintReward();
        hasDomainEraRelay = false;
        for (var relay : relays){
            if (!relay.getCustomEntityType().equals("comm_relay_makeshift")){
                hasDomainEraRelay = true;
            }
        }
    }

    @Override
    public boolean shouldShow(BaseEventIntel intel) {
        return true;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getMainRowTooltip(final BaseEventIntel intel) {
        return new BaseFactorTooltip() {
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                Color h = Misc.getHighlightColor();

                tooltip.addPara("Information gathered by your comms relay in %s as it deciphers a powerful hyperspace ping.",
                        opad,
                        h,
                        starSystem.getName());

                tooltip.addPara("Will contribute %s points per month to the treasure hunt.",
                        opad, h, "" + getProgress(intel));

                if (parent != null && parent.isMeddlingActive()) {
                    tooltip.addPara("Relay interference detected - reduced output until the meddling fleet is dealt with.",
                            opad, Misc.getNegativeHighlightColor(), "reduced output");
                }
            }
        };
    }

    @Override
    public String getDesc(BaseEventIntel intel){
        String type = hasDomainEraRelay ? "Domain Era Relay" : "Makeshift Relay";
        String desc = type + " at " + starSystem.getNameWithNoType();
        if (parent != null && parent.isMeddlingActive()){
            desc = "Disrupted " + desc;
        }
        return desc;
    }

    @Override
    public int getProgress(BaseEventIntel intel) {
        int base = hasDomainEraRelay ? BASE_PROGRESS * 2 : BASE_PROGRESS;

        if (parent != null && parent.isMeddlingActive()) {
            base = Math.round(base * THRegistry.getSettings().getSectorSprintMeddlingMult());
        }

        return base;
    }

    private boolean relayListChanged(){
        Set<String> currentRelayIds = starSystem
                .getEntitiesWithTag(Tags.COMM_RELAY)
                .stream()
                .filter(r -> r.getFaction() == Global.getSector().getPlayerFaction())
                .map(SectorEntityToken::getId)
                .collect(Collectors.toSet());
        return !this.relayIds.equals(currentRelayIds);
    }

    @Override
    public boolean isExpired() {
        return relayListChanged() || parent.isEnded() || parent.isEnding();
    }
}
