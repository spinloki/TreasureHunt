package spinloki.TreasureHunt.test;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.campaign.CampaignEngine;
import headless.Stubs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Dialogs {
    public static final class Open {
        public final InteractionDialogPlugin plugin;
        public final SectorEntityToken target;
        public final Map<Object, String> options = new LinkedHashMap<>();
        public final List<String> text = new ArrayList<>();
        public final InteractionDialogAPI dialog;
        boolean dismissed;

        Open(InteractionDialogPlugin plugin, SectorEntityToken target) {
            this.plugin = plugin;
            this.target = target;
            this.dialog = dialogFor(this);
        }

        public void select(Object optionData) {
            Suite.check(options.containsKey(optionData), "no option %s among %s", optionData, options.keySet());
            plugin.optionSelected(options.get(optionData), optionData);
        }
    }

    private static Open current;
    private static final List<Open> shown = new ArrayList<>();
    private static boolean installed;

    private Dialogs() {}

    public static void install() {
        if (installed) return;
        CampaignEngine engine = CampaignEngine.getInstance();
        Object real = engine.getCampaignUI();
        Object ui = Proxy.newProxyInstance(Dialogs.class.getClassLoader(), new Class<?>[]{CampaignEngine.o.class},
                (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "showInteractionDialog":
                            if (args.length == 2 && args[0] instanceof InteractionDialogPlugin plugin) {
                                return show(plugin, (SectorEntityToken) args[1]);
                            }
                            break;
                        case "isShowingDialog":
                            return current != null;
                        case "getCurrentInteractionDialog":
                            return current == null ? null : current.dialog;
                    }
                    try {
                        return m.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        engine.setCampaignUI((CampaignEngine.o) ui);
        installed = true;
    }

    private static boolean show(InteractionDialogPlugin plugin, SectorEntityToken target) {
        if (current != null) return false;
        Open open = new Open(plugin, target);
        current = open;
        shown.add(open);
        plugin.init(open.dialog);
        return true;
    }

    public static void dismissAll() {
        if (current != null) current.dialog.dismiss();
    }

    public static Open current() {
        return current;
    }

    public static List<Open> shown() {
        return shown;
    }

    public static InteractionDialogAPI targeting(SectorEntityToken target) {
        return new Open(null, target).dialog;
    }

    private static InteractionDialogAPI dialogFor(Open open) {
        OptionPanelAPI options = (OptionPanelAPI) Proxy.newProxyInstance(Dialogs.class.getClassLoader(),
                new Class<?>[]{OptionPanelAPI.class}, (proxy, m, args) -> {
                    if (m.getName().equals("addOption") && args.length >= 2) {
                        open.options.put(args[1], (String) args[0]);
                        return null;
                    }
                    if (m.getName().equals("clearOptions")) {
                        open.options.clear();
                        return null;
                    }
                    if (m.getName().equals("hasOption")) return open.options.containsKey(args[0]);
                    return defaultFor(m.getReturnType());
                });
        TextPanelAPI text = (TextPanelAPI) Proxy.newProxyInstance(Dialogs.class.getClassLoader(),
                new Class<?>[]{TextPanelAPI.class}, (proxy, m, args) -> {
                    if (m.getName().equals("addPara") && args != null && args.length > 0 && args[0] instanceof String s) {
                        open.text.add(s);
                    }
                    return stubFor(m.getReturnType());
                });
        return (InteractionDialogAPI) Proxy.newProxyInstance(Dialogs.class.getClassLoader(),
                new Class<?>[]{InteractionDialogAPI.class}, (proxy, m, args) -> {
                    switch (m.getName()) {
                        case "getInteractionTarget": return open.target;
                        case "getPlugin": return open.plugin;
                        case "getOptionPanel": return options;
                        case "getTextPanel": return text;
                        case "dismiss":
                        case "dismissAsCancel":
                            open.dismissed = true;
                            if (current == open) current = null;
                            return null;
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return "TestDialog[" + open.target + "]";
                    }
                    return stubFor(m.getReturnType());
                });
    }

    private static Object stubFor(Class<?> type) {
        if (type.isInterface()) return Stubs.noop(type);
        return defaultFor(type);
    }

    private static Object defaultFor(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }
}
