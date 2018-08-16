package eu.kennytv.maintenance.core;

import eu.kennytv.maintenance.api.ISettings;

import java.util.*;

public abstract class Settings implements ISettings {
    private static final Random RANDOM = new Random();
    protected final Map<UUID, String> whitelistedPlayers = new HashMap<>();
    protected boolean maintenance;
    private Set<Integer> broadcastIntervalls;
    private List<String> pingMessages;
    private String timerBroadcastMessage;
    private String endtimerBroadcastMessage;
    private String kickMessage;
    private String joinNotification;
    private String noPermMessage;
    private String maintenanceActivated;
    private String maintenanceDeactivated;
    private String playerCountMessage;
    private String playerCountHoverMessage;
    private boolean joinNotifications;
    private boolean customMaintenanceIcon;

    protected void loadSettings() {
        updateConfig();

        pingMessages = getConfigList("pingmessages");
        timerBroadcastMessage = getConfigString("starttimer-broadcast-mesage");
        endtimerBroadcastMessage = getConfigString("endtimer-broadcast-mesage");
        kickMessage = getConfigString("kickmessage");
        joinNotification = getConfigString("join-notification");
        noPermMessage = getConfigString("no-permission");
        maintenanceActivated = getConfigString("maintenance-activated");
        maintenanceDeactivated = getConfigString("maintenance-deactivated");
        maintenance = getConfigBoolean("enable-maintenance-mode");
        joinNotifications = getConfigBoolean("send-join-notification");
        customMaintenanceIcon = getConfigBoolean("custom-maintenance-icon");
        broadcastIntervalls = new HashSet<>(getConfigIntList("timer-broadcasts-for-minutes"));
        playerCountMessage = getConfigString("playercountmessage");
        playerCountHoverMessage = getConfigString("playercounthovermessage");
        if (customMaintenanceIcon)
            reloadMaintenanceIcon();

        loadExtraSettings();
    }

    private void updateConfig() {
        // 2.3 pingmessage -> pingmessages
        if (configContains("pingmessage")) {
            final List<Object> list = new ArrayList<>();
            list.add(getConfigString("pingmessage"));
            setToConfig("pingmessages", list);
            setToConfig("pingmessage", null);
            saveConfig();
            reloadConfigs();
        }

        updateExtraConfig();
    }

    public void updateExtraConfig() {
    }

    public abstract void saveWhitelistedPlayers();

    public abstract boolean createFiles();

    public abstract String getConfigString(String path);

    public abstract boolean getConfigBoolean(String path);

    public abstract List<Integer> getConfigIntList(String path);

    public abstract List<String> getConfigList(String path);

    public abstract void loadExtraSettings();

    public abstract void setWhitelist(String uuid, String s);

    public abstract void saveConfig();

    public abstract void reloadConfigs();

    public abstract void setToConfig(String path, Object var);

    public abstract boolean configContains(String path);

    public abstract String getColoredString(String s);

    public List<String> getPingMessages() {
        return pingMessages;
    }

    public Set<Integer> getBroadcastIntervalls() {
        return broadcastIntervalls;
    }

    public String getTimerBroadcastMessage() {
        return timerBroadcastMessage;
    }

    public String getEndtimerBroadcastMessage() {
        return endtimerBroadcastMessage;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public String getJoinNotification() {
        return joinNotification;
    }

    public String getNoPermMessage() {
        return noPermMessage;
    }

    public String getMaintenanceActivated() {
        return maintenanceActivated;
    }

    public String getMaintenanceDeactivated() {
        return maintenanceDeactivated;
    }

    public String getPlayerCountMessage() {
        return playerCountMessage;
    }

    public String getPlayerCountHoverMessage() {
        return playerCountHoverMessage;
    }

    public void setMaintenance(final boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getRandomPingMessage() {
        if (pingMessages.isEmpty()) return "";
        final String s = pingMessages.size() > 1 ? pingMessages.get(RANDOM.nextInt(pingMessages.size())) : pingMessages.get(0);
        return getColoredString(s.replace("%NEWLINE%", "\n"));
    }

    @Override
    public boolean isMaintenance() {
        return maintenance;
    }

    @Override
    public boolean isJoinNotifications() {
        return joinNotifications;
    }

    @Override
    public boolean hasCustomIcon() {
        return customMaintenanceIcon;
    }

    @Override
    public Map<UUID, String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    @Override
    public boolean removeWhitelistedPlayer(final UUID uuid) {
        if (!whitelistedPlayers.containsKey(uuid)) return false;
        whitelistedPlayers.remove(uuid);
        setWhitelist(uuid.toString(), null);
        saveWhitelistedPlayers();
        return true;
    }

    @Deprecated
    @Override
    public boolean removeWhitelistedPlayer(final String name) {
        if (!whitelistedPlayers.containsValue(name)) return false;
        final UUID uuid = whitelistedPlayers.entrySet().stream().filter(entry -> entry.getValue().equals(name)).findAny().get().getKey();
        whitelistedPlayers.remove(uuid);
        setWhitelist(uuid.toString(), null);
        saveWhitelistedPlayers();
        return true;
    }

    @Override
    public boolean addWhitelistedPlayer(final UUID uuid, final String name) {
        final boolean contains = !whitelistedPlayers.containsKey(uuid);
        whitelistedPlayers.put(uuid, name);
        setWhitelist(uuid.toString(), name);
        saveWhitelistedPlayers();
        return contains;
    }
}
