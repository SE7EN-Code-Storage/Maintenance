package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public abstract class MaintenanceCommand {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    private final String name;

    protected MaintenanceCommand(final MaintenanceModePlugin plugin, final Settings settings, final String name) {
        this.plugin = plugin;
        this.settings = settings;
        this.name = name;
    }

    public void execute(final SenderInfo sender, final String[] args) {
        if (checkPermission(sender, "command")) return;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                if (checkPermission(sender, "toggle")) return;
                plugin.setMaintenance(true);
            } else if (args[0].equalsIgnoreCase("off")) {
                if (checkPermission(sender, "toggle")) return;
                plugin.setMaintenance(false);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (checkPermission(sender, "reload")) return;
                settings.reloadConfigs();
                sender.sendMessage(plugin.getPrefix() + "§aReloaded config, whitelistedplayers and the maintenance icon");
            } else if (args[0].equalsIgnoreCase("update")) {
                if (checkPermission(sender, "update")) return;
                checkForUpdate(sender);
            } else if (args[0].equals("forceupdate")) {
                if (checkPermission(sender, "update")) return;
                sender.sendMessage(plugin.getPrefix() + "§c§lDownloading update...");

                if (plugin.installUpdate())
                    sender.sendMessage(plugin.getPrefix() + "§a§lThe update was successful! To prevent issues with tasks and to complete the update, you have to restart the proxy!");
                else
                    sender.sendMessage(plugin.getPrefix() + "§4Failed!");
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                if (checkPermission(sender, "whitelist.list")) return;
                final Map<UUID, String> players = settings.getWhitelistedPlayers();
                if (players.isEmpty()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe maintenance whitelist is empty! Use \"/maintenance add <player>\" to add someone!");
                } else if (players.size() == 1 && players.containsKey(UUID.fromString("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"))) {
                    sender.sendMessage(plugin.getPrefix() + "§cUse \"/maintenance add <player>\" to add someone. Alternatively, you can add the uuid of a player to the WhitelistedPlayers.yml as seen in the example in the file!");
                } else {
                    sender.sendMessage("§6Whitelisted players for maintenance:");
                    players.forEach((key, value) -> sender.sendMessage("§8- §e" + value + " §8(§7" + key + "§8)"));
                    sender.sendMessage("");
                }
            } else if (args[0].equalsIgnoreCase("motd")) {
                if (checkPermission(sender, "motd")) return;
                sender.sendMessage(plugin.getPrefix() + "§7List of your maintenance motds:");
                for (int i = 0; i < settings.getPingMessages().size(); i++) {
                    sender.sendMessage("§b" + (i + 1) + "§8§m---------");
                    for (final String motd : settings.getColoredString(settings.getPingMessages().get(i)).split("%NEWLINE%")) {
                        sender.sendMessage(motd);
                    }
                }
                sender.sendMessage("§8§m----------");
            } else
                sendUsage(sender);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("endtimer")) {
                if (checkPermission(sender, "timer")) return;
                if (!isNumeric(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + "§6/maintenance endtimer <minutes>");
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThere's already a starttimer scheduled!");
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe number has to be less than 40320 (28 days)!");
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage(plugin.getPrefix() + "§cThink about running a timer for a negative amount of minutes. Doesn't work §othat §r§cwell.");
                    return;
                }

                plugin.setMaintenance(true);
                sender.sendMessage(plugin.getPrefix() + "§aStarted timer: §7Maintenance mode will be deactivated in §6" + minutes + " minutes§7.");
                plugin.setTaskId(plugin.schedule(new MaintenanceRunnable(plugin, settings, minutes, false)));
            } else if (args[0].equalsIgnoreCase("starttimer")) {
                if (checkPermission(sender, "timer")) return;
                if (!isNumeric(args[1])) {
                    sender.sendMessage(plugin.getPrefix() + "§6/maintenance starttimer <minutes>");
                    return;
                }
                if (plugin.isTaskRunning()) {
                    sender.sendMessage(plugin.getPrefix() + "§cThere's already running a timer!");
                    return;
                }

                final int minutes = Integer.parseInt(args[1]);
                if (minutes > 40320) {
                    sender.sendMessage(plugin.getPrefix() + "§cThe number has to be less than 40320 (28 days)!");
                    return;
                }
                if (minutes < 1) {
                    sender.sendMessage(plugin.getPrefix() + "§cThink about running a timer for a negative amount of minutes. Doesn't work §othat §cwell.");
                    return;
                }

                sender.sendMessage(plugin.getPrefix() + "§aStarted timer: §7Maintenance mode will be activated in §6" + minutes + " minutes§7.");
                plugin.setTaskId(plugin.schedule(new MaintenanceRunnable(plugin, settings, minutes, true)));
            } else if (args[0].equalsIgnoreCase("timer")) {
                if (args[1].equalsIgnoreCase("abort") || args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("cancel")) {
                    if (checkPermission(sender, "timer")) return;
                    if (!plugin.isTaskRunning()) {
                        sender.sendMessage(plugin.getPrefix() + "§cThere's currently no running timer.");
                        return;
                    }

                    plugin.cancelTask();
                    sender.sendMessage(plugin.getPrefix() + "§cThe timer has been disabled.");
                } else
                    sendUsage(sender);
            } else if (args[0].equalsIgnoreCase("add")) {
                if (checkPermission(sender, "whitelist.add")) return;
                addPlayerToWhitelist(sender, args[1]);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (checkPermission(sender, "whitelist.remove")) return;
                removePlayerFromWhitelist(sender, args[1]);
            } else
                sendUsage(sender);
        } else if (args.length > 3 && args[0].equalsIgnoreCase("setmotd")) {
            if (checkPermission(sender, "setmotd")) return;
            if (!isNumeric(args[1])) {
                sender.sendMessage(plugin.getPrefix() + "§cThe first argument has to be the motd index!");
                return;
            }

            final int index = Integer.parseInt(args[1]);
            if (index < 1 || index > settings.getPingMessages().size() + 1) {
                sender.sendMessage(plugin.getPrefix() + "§cYou currently have " + settings.getPingMessages().size()
                        + " motds, so you have to pick a number between 1 and " + (settings.getPingMessages().size() + 1));
                return;
            }

            if (!isNumeric(args[2])) {
                sender.sendMessage(plugin.getPrefix() + "§cThe second argument has to be the line number (1 or 2)!");
                return;
            }

            final int line = Integer.parseInt(args[2]);
            if (line != 1 && line != 2) {
                sender.sendMessage(plugin.getPrefix() + "§cThe second argument has to be the line number (1 or 2)!");
                return;
            }

            final String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            final String oldMessage = index > settings.getPingMessages().size() ? "" : settings.getPingMessages().get(index - 1);
            final String newMessage;
            if (line == 1)
                newMessage = oldMessage.contains("%NEWLINE%") ?
                        message + "%NEWLINE%" + oldMessage.split("%NEWLINE%", 2)[1] : message;
            else
                newMessage = oldMessage.contains("%NEWLINE%") ?
                        oldMessage.split("%NEWLINE%", 2)[0] + "%NEWLINE%" + message : oldMessage + "%NEWLINE%" + message;

            if (index > settings.getPingMessages().size())
                settings.getPingMessages().add(newMessage);
            else
                settings.getPingMessages().set(index - 1, newMessage);
            settings.setToConfig("pingmessages", settings.getPingMessages());
            settings.saveConfig();
            settings.reloadConfigs();
            sender.sendMessage(plugin.getPrefix() + "§aSet line " + line + " of the " + index + ". maintenance motd to §f" + settings.getColoredString(message));
        } else
            sendUsage(sender);
    }

    private void sendUsage(final SenderInfo sender) {
        sender.sendMessage("");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        if (sender.hasPermission("maintenance.reload"))
            sender.sendMessage("§6/maintenance reload §7(Reloads the config file, whitelist file and the server-icon)");
        if (sender.hasPermission("maintenance.toggle")) {
            sender.sendMessage("§6/maintenance on §7(Enables maintenance mode");
            sender.sendMessage("§6/maintenance off §7(Disables maintenance mode)");
        }
        if (sender.hasPermission("maintenance.timer")) {
            sender.sendMessage("§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled. Broadcast settings for the timer can be found in the config)");
            sender.sendMessage("§6/maintenance endtimer <minutes> §7(Enables maintenance mode. After the given time in minutes, maintenance mode will be disabled)");
            sender.sendMessage("§6/maintenance timer abort §7(If running, the current timer will be aborted)");
        }
        if (sender.hasPermission("maintenance.whitelist.list"))
            sender.sendMessage("§6/maintenance whitelist §7(Shows all whitelisted players for the maintenance mode)");
        if (sender.hasPermission("maintenance.whitelist.add"))
            sender.sendMessage("§6/maintenance add <player> §7(Adds the player to the maintenance whitelist, so they can join the server even though maintenance is enabled)");
        if (sender.hasPermission("maintenance.whitelist.remove"))
            sender.sendMessage("§6/maintenance remove <player> §7(Removes the player from the maintenance whitelist)");
        if (sender.hasPermission("maintenance.setmotd"))
            sender.sendMessage("§6/maintenance setmotd <index> <1/2> <message> §7(Sets a motd for maintenance mode)");
        if (sender.hasPermission("maintenance.motd"))
            sender.sendMessage("§6/maintenance motd §7(Lists the currently set maintenance motds)");
        if (sender.hasPermission("maintenance.update"))
            sender.sendMessage("§6/maintenance update §7(Remotely downloads the newest version of the plugin onto your server)");
        sender.sendMessage("§7Created by §bKennyTV");
        sender.sendMessage("§8===========[ §e" + name + " §8| §eVersion: §e" + plugin.getVersion() + " §8]===========");
        sender.sendMessage("");
    }

    private boolean checkPermission(final SenderInfo sender, final String permission) {
        if (!sender.hasPermission("maintenance." + permission)) {
            sender.sendMessage(settings.getNoPermMessage());
            return true;
        }
        return false;
    }

    private boolean isNumeric(final String string) {
        try {
            Integer.parseInt(string);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    protected abstract void addPlayerToWhitelist(SenderInfo sender, String name);

    protected abstract void removePlayerFromWhitelist(SenderInfo sender, String name);

    protected abstract void checkForUpdate(SenderInfo sender);
}
