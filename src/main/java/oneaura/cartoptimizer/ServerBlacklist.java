package oneaura.cartoptimizer;

import java.util.Set;

public class ServerBlacklist {
    private static final ServerBlacklist INSTANCE = new ServerBlacklist();

    private static final Set<String> BLACKLISTED_SERVERS = Set.of(
            "na.mcpvp.club",
            "eu.mcpvp.club",
            "as.mcpvp.club",
            "au.mcpvp.club");

    private String currentServer = null;

    public static ServerBlacklist getInstance() {
        return INSTANCE;
    }

    public void setCurrentServer(String server) {
        this.currentServer = server;
    }

    public String getCurrentServer() {
        return currentServer;
    }

    public boolean isOnBlacklistedServer() {
        if (currentServer == null)
            return false;

        String lowerServer = currentServer.toLowerCase();
        for (String blacklisted : BLACKLISTED_SERVERS) {
            if (lowerServer.contains(blacklisted)) {
                return true;
            }
        }
        return false;
    }

    public void clearServer() {
        this.currentServer = null;
    }
}
