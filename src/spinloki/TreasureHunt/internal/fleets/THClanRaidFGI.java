package spinloki.TreasureHunt.internal.fleets;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.group.FGRaidAction;
import com.fs.starfarer.api.impl.campaign.intel.group.GenericRaidFGI;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerNum;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers.OfficerQuality;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import spinloki.TreasureHunt.internal.events.THFactorTracker;
import spinloki.TreasureHunt.internal.events.TreasureHuntEventIntel;
import spinloki.TreasureHunt.internal.factors.THRaidLossFactor;
import spinloki.TreasureHunt.util.THUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class THClanRaidFGI extends GenericRaidFGI {

    private static final String DROP_BLUEPRINTS_LOW = "blueprints_low";

    private static final float GAMMA_CORES_MAIN = 6f;
    private static final float BETA_CORES_MAIN = 1.5f;

    public static final String MEM_ROLE = "$th_raid_role";
    public static final String ROLE_MAIN = "main";
    public static final String ROLE_SUPPORT = "support";
    public static final String ROLE_SUPPLY = "supply";

    public static final String FACTION_ID = "salvor_clan";

    private Set<String> supplyFleetIds = new HashSet<>();
    private Set<String> mainFleetIds = new HashSet<>();
    private Set<String> allFleetIds = new HashSet<>();
    private transient String abortReason;
    private boolean rewarded = false;
    private String targetMarketId;

    public THClanRaidFGI(GenericRaidParams params) {
        super(params);
        if (params.raidParams != null && !params.raidParams.allowedTargets.isEmpty()) {
            targetMarketId = params.raidParams.allowedTargets.get(0).getId();
        }
    }

    public static List<Integer> computeFleetSizes(MarketAPI target, float mult) {
        int size = target.getSize();
        int main = Math.round((100 + size * 30) * mult);
        List<Integer> sizes = new ArrayList<>();
        sizes.add(main);
        int supports = Math.max(0, (size - 2) / 2);
        for (int i = 0; i < supports; i++) sizes.add(Math.round(main * 0.4f));
        sizes.add(Math.round(main * 0.15f));
        return sizes;
    }

    public static GenericRaidParams buildParams(MarketAPI target, MarketAPI source, float mult) {
        GenericRaidParams p = new GenericRaidParams(new Random(), true);
        p.factionId = FACTION_ID;
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
        rp.where = target.getStarSystem();
        rp.type = FGRaidAction.FGRaidType.CONCURRENT;
        rp.allowedTargets = new ArrayList<>(List.of(target));
        rp.maxStabilityLostPerRaid = 3;
        rp.raidsPerColony = 1;
        // The clan is neutral to the player; without this FGRaidAction filters out every
        // target and the raid finishes with nothing to do.
        rp.allowNonHostileTargets = true;
        rp.setDisrupt(Industries.TECHMINING);
        p.raidParams = rp;
        return p;
    }

    public static THClanRaidFGI launchAgainst(MarketAPI target, float mult) {
        MarketAPI source = findSourceMarket(target);
        if (source == null) return null;
        THClanRaidFGI raid = new THClanRaidFGI(buildParams(target, source, mult));
        Global.getSector().getIntelManager().addIntel(raid);
        return raid;
    }

    private static MarketAPI findSourceMarket(MarketAPI target) {
        MarketAPI nearest = null;
        float best = Float.MAX_VALUE;
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            if (m.isPlayerOwned() || m.isHidden() || m.getPrimaryEntity() == null) continue;
            float d = Misc.getDistanceLY(target.getLocationInHyperspace(), m.getLocationInHyperspace());
            if (d < best) { best = d; nearest = m; }
        }
        return nearest;
    }

    public CampaignFleetAPI createFleetForTesting(int size) {
        return createFleet(size, 0f);
    }

    @Override
    public void abort() {
        boolean beaten = isSpawnedFleets();
        super.abort();
        if (beaten && targetMarketId != null) {
            THUtils.getClanClearedColonies().add(targetMarketId);
            THFactorTracker.syncClanRivalryFactors();
        }
    }

    @Override
    protected void configureFleet(int size, FleetCreatorMission m) {
        super.configureFleet(size, m);

        String role = roleForSize(size);
        if (ROLE_SUPPLY.equals(role)) {
            m.triggerSetFleetType(FleetTypes.SUPPLY_FLEET);
            m.triggerSetFleetComposition(0.6f, 0.6f, 0.5f, 0f, 0.1f);
            m.triggerSetFleetOfficers(OfficerNum.FEWER, OfficerQuality.LOWER);
        } else if (ROLE_MAIN.equals(role)) {
            m.triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
            m.triggerSetFleetCompositionNoSupportShips();
        } else {
            m.triggerSetFleetOfficers(OfficerNum.FEWER, OfficerQuality.LOWER);
            m.triggerSetFleetCompositionNoSupportShips();
        }
    }

    @Override
    protected void configureFleet(int size, CampaignFleetAPI fleet) {
        super.configureFleet(size, fleet);
        if (fleet == null) return;

        fleet.getMemoryWithoutUpdate().set(THUtils.MEMORY_KEY_TH_SCAVENGER, true);

        String role = roleForSize(size);
        fleet.getMemoryWithoutUpdate().set(MEM_ROLE, role);
        allFleetIds.add(fleet.getId());
        if (ROLE_SUPPLY.equals(role)) {
            supplyFleetIds.add(fleet.getId());
            fleet.setName("Supply Detachment");
            loadRaidStores(fleet);
        } else if (ROLE_MAIN.equals(role)) {
            mainFleetIds.add(fleet.getId());
            fleet.setName("Raider Detachment");
        } else {
            fleet.setName("Escort Detachment");
        }

        fleet.getMemoryWithoutUpdate().set(
                MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
                new THRaidFIDConfigGen(this, role));
    }

    private void loadRaidStores(CampaignFleetAPI fleet) {
        CargoAPI cargo = fleet.getCargo();

        float fuelRoom = Math.max(0f, cargo.getMaxFuel() - cargo.getFuel());
        cargo.addCommodity(Commodities.FUEL, fuelRoom * 0.8f);

        float room = Math.max(0f, cargo.getMaxCapacity() - cargo.getSpaceUsed()) * 0.8f;
        if (room <= 0f) return;
        cargo.addCommodity(Commodities.MARINES, room * 0.4f);
        cargo.addCommodity(Commodities.SUPPLIES, room * 0.3f);
        cargo.addCommodity(Commodities.HAND_WEAPONS, room * 0.15f);
        cargo.addCommodity(Commodities.HEAVY_MACHINERY, room * 0.15f);
    }

    // Named static, not a lambda: XStream nulls anonymous classes held in fleet memory.
    public static class THRaidFIDConfigGen implements FleetInteractionDialogPluginImpl.FIDConfigGen {
        private THClanRaidFGI intel;
        private String role;

        public THRaidFIDConfigGen(THClanRaidFGI intel, String role) {
            this.intel = intel;
            this.role = role;
        }

        @Override
        public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
            FleetInteractionDialogPluginImpl.FIDConfig config =
                    new FleetInteractionDialogPluginImpl.FIDConfig();
            config.delegate = new THRaidSalvageDelegate(intel, role);
            return config;
        }
    }

    public static class THRaidSalvageDelegate extends FleetInteractionDialogPluginImpl.BaseFIDDelegate {
        private THClanRaidFGI intel;
        private String role;

        public THRaidSalvageDelegate(THClanRaidFGI intel, String role) {
            this.intel = intel;
            this.role = role;
        }

        @Override
        public void postPlayerSalvageGeneration(InteractionDialogAPI dialog,
                                                FleetEncounterContext context, CargoAPI salvage) {
            if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI fleet)) return;
            addClanHoard(fleet, role, salvage);
            if (ROLE_MAIN.equals(role) && intel != null) intel.addRewardToSalvage(salvage);
        }
    }

    private static void addClanHoard(CampaignFleetAPI fleet, String role, CargoAPI salvage) {
        float mult = ROLE_MAIN.equals(role) ? 1f : ROLE_SUPPLY.equals(role) ? 0.6f : 0.35f;
        Random random = new Random(Misc.getSalvageSeed(fleet));

        addCores(salvage, random, Commodities.GAMMA_CORE, GAMMA_CORES_MAIN * mult);
        addCores(salvage, random, Commodities.BETA_CORE, BETA_CORES_MAIN * mult);

        List<DropData> dropValue = new ArrayList<>();
        DropData bulk = new DropData();
        bulk.group = Drops.BASIC;
        bulk.chances = 1;
        bulk.value = Math.round(8000 * mult);
        dropValue.add(bulk);

        List<DropData> dropRandom = new ArrayList<>();
        addDrop(dropRandom, Drops.MACHINERY, Math.round(4 * mult));
        addDrop(dropRandom, Drops.ANY_HULLMOD_MEDIUM, Math.round(6 * mult));
        addDrop(dropRandom, Drops.WEAPONS2, Math.round(8 * mult));
        addDrop(dropRandom, DROP_BLUEPRINTS_LOW, Math.round(8 * mult));

        CargoAPI extra = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, dropValue, dropRandom);
        for (CargoStackAPI stack : extra.getStacksCopy()) {
            salvage.addFromStack(stack);
        }
    }

    private static void addCores(CargoAPI salvage, Random random, String coreId, float target) {
        if (target <= 0f) return;
        float jittered = target * (0.7f + random.nextFloat() * 0.6f);
        int count = (int) jittered;
        if (random.nextFloat() < jittered - count) count++;
        if (count > 0) salvage.addCommodity(coreId, count);
    }

    private static void addDrop(List<DropData> drops, String group, int chances) {
        if (chances <= 0) return;
        DropData d = new DropData();
        d.group = group;
        d.chances = chances;
        drops.add(d);
    }

    private String roleForSize(int size) {
        List<Integer> sizes = getParams().fleetSizes;
        if (sizes == null || sizes.isEmpty()) return ROLE_SUPPORT;
        int max = Collections.max(sizes);
        int min = Collections.min(sizes);
        if (size >= max) return ROLE_MAIN;
        if (size <= min) return ROLE_SUPPLY;
        return ROLE_SUPPORT;
    }

    @Override
    protected boolean shouldAbort() {
        if (super.shouldAbort()) {
            abortReason = "base class shouldAbort";
            return true;
        }
        if (!isSpawnedFleets()) return false;
        if (!supplyFleetIds.isEmpty() && noneStillFighting(supplyFleetIds)) {
            abortReason = "supply detachment destroyed or beaten below the return threshold";
            return true;
        }
        if (!mainFleetIds.isEmpty() && noneStillFighting(mainFleetIds)) {
            abortReason = "raider detachment destroyed or beaten below the return threshold";
            return true;
        }
        return false;
    }

    // Judged from the fleet itself rather than getFleets() membership: the base class also
    // prunes healthy fleets that lag behind as stragglers, which is not a defeat.
    private boolean isStillFighting(String id) {
        CampaignFleetAPI fleet = resolveFleet(id);
        if (fleet == null || !fleet.isAlive()) return false;
        float spawnFP = fleet.getMemoryWithoutUpdate().getFloat(KEY_SPAWN_FP);
        return spawnFP <= 0f || fleet.getFleetPoints() > spawnFP * getFleetAbortsMissionFPFraction();
    }

    private boolean noneStillFighting(Set<String> ids) {
        for (String id : ids) {
            if (isStillFighting(id)) return false;
        }
        return true;
    }

    private CampaignFleetAPI resolveFleet(String id) {
        SectorEntityToken entity = Global.getSector().getEntityById(id);
        return entity instanceof CampaignFleetAPI fleet ? fleet : null;
    }

    private String describeValidTargets() {
        FGRaidAction.FGRaidParams rp = getParams().raidParams;
        if (rp == null || rp.where == null) return "no raid params";
        List<String> valid = new ArrayList<>();
        for (MarketAPI market : Misc.getMarketsInLocation(rp.where)) {
            if (!rp.allowAnyHostileMarket && !rp.allowedTargets.contains(market)) continue;
            if (!rp.allowNonHostileTargets && !getFaction().isHostileTo(market.getFaction())) continue;
            valid.add(market.getName());
        }
        return valid.isEmpty()
                ? "none (allowNonHostileTargets=" + rp.allowNonHostileTargets
                        + ", faction hostile to player=" + getFaction().isHostileTo(Global.getSector().getPlayerFaction()) + ")"
                : String.join(", ", valid);
    }

    private String describeGroupStrength() {
        float live = 0f;
        for (CampaignFleetAPI fleet : getFleets()) {
            if (fleet.isAlive()) live += fleet.getFleetPoints();
        }
        float threshold = totalFPSpawned * getGroupAbortsMissionFPFraction();
        return String.format("group fp %.0f vs abort threshold %.0f of %.0f spawned",
                live, threshold, totalFPSpawned);
    }

    @Override
    public void finish(boolean isAbort) {
        if (!isEnding()) logRaidEnd(isAbort);
        super.finish(isAbort);
    }

    private void logRaidEnd(boolean isAbort) {
        Logger log = Global.getLogger(THClanRaidFGI.class);
        log.info("TreasureHunt clan raid ended (" + (isAbort ? "aborted" : "failed without abort")
                + ", fleets spawned: " + isSpawnedFleets() + "): "
                + (abortReason == null ? "no TreasureHunt condition fired, " + describeGroupStrength() : abortReason));
        log.info("  valid raid targets: " + describeValidTargets());
        for (String id : allFleetIds) {
            CampaignFleetAPI fleet = resolveFleet(id);
            if (fleet == null) {
                log.info("  " + id + " no longer exists");
                continue;
            }
            log.info(String.format("  %-20s role=%-8s alive=%-5s fp=%d/%.0f inGroup=%s",
                    fleet.getName(),
                    fleet.getMemoryWithoutUpdate().getString(MEM_ROLE),
                    fleet.isAlive(),
                    fleet.getFleetPoints(),
                    fleet.getMemoryWithoutUpdate().getFloat(KEY_SPAWN_FP),
                    getFleets().contains(fleet)));
        }
    }

    void addRewardToSalvage(CargoAPI salvage) {
        if (rewarded) return;
        TreasureHuntEventIntel intel = TreasureHuntEventIntel.get();
        if (intel == null) return;
        for (String itemId : intel.getRandomRewardItems(1)) {
            salvage.addSpecial(new SpecialItemData(itemId, null), 1);
            intel.removeRewardItemFromPool(itemId);
            rewarded = true;
        }
    }

    @Override
    public boolean hasCustomRaidAction() {
        return true;
    }

    @Override
    public void doCustomRaidAction(CampaignFleetAPI fleet, MarketAPI market, float raidStr) {
        Industry techmining = market.getIndustry(Industries.TECHMINING);
        if (techmining != null) {
            float durMult = Global.getSettings().getFloat("punitiveExpeditionDisruptDurationMult");
            new MarketCMD(market.getPrimaryEntity()).doIndustryRaid(getFaction(), raidStr, techmining, durMult);
        } else {
            new MarketCMD(market.getPrimaryEntity()).doGenericRaid(getFaction(), raidStr,
                    getParams().raidParams.maxStabilityLostPerRaid, false);
        }

        int penalty = market.getSize() * 10;
        TreasureHuntEventIntel.addFactorCreateIfNecessary(new THRaidLossFactor(penalty, market.getName()), null);
    }
}
