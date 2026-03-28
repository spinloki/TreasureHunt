package spinloki.TreasureHunt.internal.bar;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;

import java.util.Map;

public class THSalvorClanBarEvent extends BaseBarEventWithPerson {

    private static final String TRIGGER = "THSalvorClanOption";
    private static final String PROMPT_TEXT =
            "A weathered group sits around a table cluttered with star charts and salvage manifests. " +
            "Their jackets bear the sigil of a Salvor Clan — one of the large outfits devoted to " +
            "reclaiming the ruins left by the Domain and the AI wars.";
    private static final String OPT_APPROACH_TEXT = "Approach the Clanners and see what they know";

    @Override public boolean isAlwaysShow() {
        return true;
    }

    @Override
    protected String getPersonFaction() {
        return "independent";
    }

    @Override
    protected String getPersonRank() {
        return null;
    }

    @Override
    protected String getPersonPost() {
        return null;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        return market != null;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);
        regen(dialog.getInteractionTarget().getMarket());
        dialog.getTextPanel().addPara(PROMPT_TEXT);
        dialog.getOptionPanel().addOption(OPT_APPROACH_TEXT, this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        done = false;
        noContinue = false;

        MemoryAPI local = memoryMap.get("local");
        if (local == null) {
            local = dialog.getInteractionTarget().getMemoryWithoutUpdate();
        }
        local.set("$option", "thSalvorStart", 0f);
        local.set("$manOrWoman", getManOrWoman(), 0f);
        local.set("$heOrShe", getHeOrShe(), 0f);
        local.set("$HeOrShe", getHeOrShe().substring(0, 1).toUpperCase() + getHeOrShe().substring(1), 0f);
        local.set("$hisOrHer", getHisOrHer(), 0f);
        local.set("$HisOrHer", getHisOrHer().substring(0, 1).toUpperCase() + getHisOrHer().substring(1), 0f);
        local.set("$himOrHer", getHimOrHer(), 0f);

        FireAll.fire(null, dialog, memoryMap, TRIGGER);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (noContinue || done) return;

        String optionId = (String) optionData;

        if ("thSalvorLeave".equals(optionId)) {
            noContinue = true;
            done = true;
            return;
        }

        if ("thSalvorDone".equals(optionId)) {
            BarEventManager.getInstance().notifyWasInteractedWith(this);
            noContinue = true;
            done = true;
            return;
        }

        MemoryAPI local = memoryMap.get("local");
        if (local == null) {
            local = dialog.getInteractionTarget().getMemoryWithoutUpdate();
        }
        local.set("$option", optionId, 0f);

        dialog.getOptionPanel().clearOptions();
        FireAll.fire(null, dialog, memoryMap, TRIGGER);
    }
}
