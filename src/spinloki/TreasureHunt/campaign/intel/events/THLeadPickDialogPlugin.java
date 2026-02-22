package spinloki.TreasureHunt.campaign.intel.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;

import java.util.Map;
import java.util.Set;

public class THLeadPickDialogPlugin implements InteractionDialogPlugin {

    private InteractionDialogAPI dialog;
    private TextPanelAPI text;
    private OptionPanelAPI options;

    private final Set<String> candidates;
    private String chosen = null;

    public THLeadPickDialogPlugin(Set<String> candidates) {
        this.candidates = candidates;
    }

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        this.text = dialog.getTextPanel();
        this.options = dialog.getOptionPanel();

        text.setFontOrbitronUnnecessarilyLarge();
        text.addPara("Treasure Hunt - Choose Lead", Misc.getHighlightColor());

        buildUI();
    }

    private void buildUI() {
        text.setFontInsignia();
        text.addPara("You have uncovered several promising leads. You can pick one to follow or ignore them all so that none of them soon reappear.");

        for (String id : candidates) {

            var spec = Global.getSettings().getSpecialItemSpec(id);
            if (spec == null) continue;

            String name = spec.getName();

            // Smaller font for tighter layout
            text.setFontSmallInsignia();

            text.addPara(name, Misc.getHighlightColor());

            // Shortened description instead of full spec text
            String desc = spec.getDesc();
            if (desc != null && !desc.isEmpty()) {
                text.addPara(desc);
            }
            options.addOption(name, id);
        }

        options.addOption("None of these", "cancel");
    }
    @Override
    public void optionSelected(String optionText, Object optionData) {

        if (optionData == null) return;

        String id = optionData.toString();

        if ("cancel".equals(id)) {
            dialog.dismiss();
            return;
        }

        chosen = id;

        dialog.dismiss();
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {

    }

    public String getChosen() {
        return chosen;
    }

    @Override
    public void advance(float amount) { }

    @Override
    public void backFromEngagement(EngagementResultAPI result) { }

    @Override
    public Object getContext() { return null; }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() { return null; }
}