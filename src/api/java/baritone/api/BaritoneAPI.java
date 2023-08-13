package baritone.api;

import baritone.api.utils.SettingsUtil;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class BaritoneAPI {

    private static final IBaritoneProvider provider;
    private static final Settings settings;

    static {
        settings = new Settings();
        SettingsUtil.readAndApply(settings, SettingsUtil.SETTINGS_DEFAULT_NAME);

        ServiceLoader<IBaritoneProvider> baritoneLoader = ServiceLoader.load(IBaritoneProvider.class);
        Iterator<IBaritoneProvider> instances = baritoneLoader.iterator();

        provider = instances.next();
    }

    public static IBaritoneProvider getProvider() {
        return BaritoneAPI.provider;
    }

    public static Settings getSettings() {
        return BaritoneAPI.settings;
    }
}
