package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.campaign.intel.THSectorSprintIntel;
import spinloki.TreasureHunt.config.Settings;

import java.awt.*;
import java.util.List;

public class THSectorSprintFactor  extends BaseEventFactor {
    List<SectorEntityToken> relays;
    StarSystemAPI starSystem;
    boolean hasDomainEraRelay;
    int BASE_PROGRESS;
    boolean ENABLED = true;
    THSectorSprintIntel parent;

    public THSectorSprintFactor(List<SectorEntityToken> relays, THSectorSprintIntel parent){
        super();
        this.relays = relays;
        this.parent = parent;
        this.starSystem = relays.get(0).getStarSystem();
        BASE_PROGRESS = Settings.TH_SECTOR_SPRINT_REWARD;
        hasDomainEraRelay = false;
        for (var relay : relays){
            if (!relay.getCustomEntityType().equals("comm_relay_makeshift")){
                hasDomainEraRelay = true;
            }
        }
    }

    @Override
    public boolean shouldShow(BaseEventIntel intel) {
        return ENABLED;
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
                        opad,
                        h,
                        "" + getProgress(intel));
            }
        };
    }

    @Override
    public String getDesc(BaseEventIntel intel){
        String type = hasDomainEraRelay ? "Domain Era Comm Relay" : "Makeshift Comm Relay";
        return type + " at " + starSystem.getName();
    }

    @Override
    public int getProgress(BaseEventIntel intel) {
        return hasDomainEraRelay ? BASE_PROGRESS * 2 : BASE_PROGRESS;
    }

    private boolean relayListChanged(){
        var currentPlayerOwnedRelays = starSystem
                .getEntitiesWithTag(Tags.COMM_RELAY)
                .stream()
                .filter(r -> r.getFaction() == Global.getSector().getPlayerFaction())
                .toList();
        return this.relays.size() != currentPlayerOwnedRelays.size();
    }

    @Override
    public boolean isExpired() {
        return relayListChanged() || parent.isEnded() || parent.isEnding();
    }
}
