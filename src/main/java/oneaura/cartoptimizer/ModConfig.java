package oneaura.cartoptimizer;

public class ModConfig {
    private static final ModConfig INSTANCE = new ModConfig();
    private boolean debugMode = false;
    private boolean enabled = true;

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns true if the mod is enabled AND not on a blacklisted server.
     */
    public boolean isEffectivelyEnabled() {
        if (!enabled)
            return false;
        return !ServerBlacklist.getInstance().isOnBlacklistedServer();
    }
}
