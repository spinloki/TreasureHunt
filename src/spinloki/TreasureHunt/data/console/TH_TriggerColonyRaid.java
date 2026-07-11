package spinloki.TreasureHunt.data.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import spinloki.TreasureHunt.internal.fleets.THClanRaidFGI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Dev/testing command: fires a Salvor Clan colony raid at a player colony via the
 * GenericRaidFGI framework. Pair with th_maketestcolony to iterate on raid scaling.
 *
 * Usage: th_triggercolonyraid [strengthMult=1.0]
 *   Targets the player colony in the current system (else nearest player colony).
 *   strengthMult scales the raid's fleet points so you can tune it against the
 *   colony's WarSim defensive strength without rebuilds (try 2, 3, 4...).
 */
public class TH_TriggerColonyRaid implements BaseCommand {

    private static final String RAIDER_FACTION = "salvor_clan";

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("Error: campaign-only.");
            return CommandResult.WRONG_CONTEXT;
        }

        float mult = 1f;
        if (args != null && !args.trim().isEmpty()) {
            try { mult = Float.parseFloat(args.trim()); } catch (NumberFormatException ignored) {}
        }

        MarketAPI target = pickTargetColony();
        if (target == null) {
            Console.showMessage("Error: no player colony found to raid.");
            return CommandResult.ERROR;
        }
        MarketAPI source = pickSourceMarket(target);
        if (source == null) {
            Console.showMessage("Error: no non-player market to source the raid from.");
            return CommandResult.ERROR;
        }
        StarSystemAPI system = target.getStarSystem();

        FactionAPI raiderFaction = Global.getSector().getFaction(RAIDER_FACTION);
        if (raiderFaction != null && target.getFactionId() != null) {
            raiderFaction.setRelationship(target.getFactionId(), RepLevel.HOSTILE);
        }

        GenericRaidFGI.GenericRaidParams p = new GenericRaidFGI.GenericRaidParams(new Random(), true);
        p.factionId = RAIDER_FACTION;
        p.source = source;
        p.style = FleetCreatorMission.FleetStyle.STANDARD;
        p.makeFleetsHostile = true;
        p.repImpact = HubMissionWithTriggers.ComplicationRepImpact.NONE;
        p.prepDays = 2f;
        p.payloadDays = 60f;
        p.noun = "raid";
        p.forcesNoun = "raiders";
        p.memoryKey = "$th_clan_raid_" + target.getId();
        p.fleetSizes = computeFleetSizes(target, mult);

        FGRaidAction.FGRaidParams rp = new FGRaidAction.FGRaidParams();
        rp.where = system;
        rp.type = FGRaidAction.FGRaidType.CONCURRENT;
        rp.allowedTargets = new ArrayList<>(List.of(target));
        rp.maxStabilityLostPerRaid = 3;
        rp.raidsPerColony = 1;
        rp.setDisrupt(Industries.TECHMINING);
        p.raidParams = rp;

        THClanRaidFGI raid = new THClanRaidFGI(p);
        Global.getSector().getIntelManager().addIntel(raid);

        Console.showMessage(String.format(
                "Clan raid launched at %s (size %d) in %s, sourced from %s. Mult %.1f, fleets: %s",
                target.getName(), target.getSize(), system.getName(), source.getName(), mult, p.fleetSizes));
        return CommandResult.SUCCESS;
    }

    private List<Integer> computeFleetSizes(MarketAPI m, float mult) {
        int size = m.getSize();
        int main = Math.round((100 + size * 30) * mult);
        List<Integer> sizes = new ArrayList<>();
        sizes.add(main);
        int supports = Math.max(0, (size - 2) / 2);
        for (int i = 0; i < supports; i++) sizes.add(Math.round(main * 0.6f));
        sizes.add(Math.round(main * 0.3f));
        return sizes;
    }

    private MarketAPI pickTargetColony() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        StarSystemAPI playerSystem = player.getStarSystem();
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!m.isPlayerOwned() || m.getPrimaryEntity() == null) continue;
            if (playerSystem != null && m.getStarSystem() == playerSystem) return m;
            float d = Misc.getDistanceLY(player.getLocationInHyperspace(), m.getLocationInHyperspace());
            if (d < best) { best = d; nearest = m; }
        }
        return nearest;
    }

    private MarketAPI pickSourceMarket(MarketAPI target) {
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.isPlayerOwned() || m.isHidden() || m.getPrimaryEntity() == null) continue;
            float d = Misc.getDistanceLY(target.getLocationInHyperspace(), m.getLocationInHyperspace());
            if (d < best) { best = d; nearest = m; }
        }
        return nearest;
    }
}
