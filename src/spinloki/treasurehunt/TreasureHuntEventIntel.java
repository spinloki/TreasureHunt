package spinloki.treasurehunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.ht.HyperspaceTopographyEventIntel;

import java.awt.*;

public class TreasureHuntEventIntel extends BaseEventIntel {
    public static Color BAR_COLOR = Global.getSettings().getColor("progressBarFleetPointsColor");
    public static int PROGRESS_MAX = 1000;
    public static int PROGRESS_1 = 100;
    public static int PROGRESS_2 = 250;
    public static int PROGRESS_3 = 400;
    public static int PROGRESS_4 = 550;
    public static int PROGRESS_5 = 700;

    public static String KEY = "$treasure_hunt_event_ref";

    public static enum Stage {
        START,
        FOO,
        BAR,
        BASH,
    }

    public static void addFactorCreateIfNecessary(EventFactor factor, InteractionDialogAPI dialog) {
        if (get() == null) {
            //TextPanelAPI text = dialog == null ? null : dialog.getTextPanel();
            //new HyperspaceTopographyEventIntel(text);
            // adding a factor anyway, so it'll show a message - don't need to double up
            new TreasureHuntEventIntel(null, false);
        }
        if (get() != null) {
            get().addFactor(factor, dialog);
        }
    }

    public static TreasureHuntEventIntel get() {
        return (TreasureHuntEventIntel) Global.getSector().getMemoryWithoutUpdate().get(KEY);
    }

    public TreasureHuntEventIntel(TextPanelAPI text, boolean withIntelNotification) {
        super();

        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);


        setup();

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this, !withIntelNotification, text);
    }

    protected void setup() {
        factors.clear();
        stages.clear();

        setMaxProgress(PROGRESS_MAX);

        setProgress(10);

        addStage(Stage.START, 0);
        addStage(Stage.FOO, PROGRESS_1, StageIconSize.MEDIUM);
        addStage(Stage.BAR, PROGRESS_2, StageIconSize.LARGE);
        addStage(Stage.BASH, PROGRESS_3, StageIconSize.MEDIUM);

        getDataFor(Stage.START).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.FOO).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.BAR).keepIconBrightWhenLaterStageReached = true;
        getDataFor(Stage.BASH).keepIconBrightWhenLaterStageReached = true;

    }
}
