package at.junction.anathema;

import at.junction.api.alts.AltAPI;
import at.junction.api.alts.Alt;
import at.junction.api.bans.BanAPI;
import at.junction.api.bans.Ban;
import at.junction.api.bans.Note;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class Anathema extends JavaPlugin {

    BanAPI banAPI;
    AltAPI altAPI;
    Configuration config;


    @Override
    public void onEnable() {
        File conf = new File(getDataFolder(), "config.yml");
        if (!conf.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        config = new Configuration(this);
        config.load();

        banAPI = new BanAPI(config.ENDPOINT, config.APIKEY);
        altAPI = new AltAPI(config.ENDPOINT, config.APIKEY);

        getServer().getPluginManager().registerEvents(new AnathemaListener(this), this);
        getLogger().info("Enabled Anathema");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled Anathema");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (command.getName().equalsIgnoreCase("a")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Advanced Anathema Usage: http://junction.at/wiki/anathema");
                sender.sendMessage(ChatColor.RED + "NYI");
                return true;
            }


        } else if (command.getName().equalsIgnoreCase("ban")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /ban <username> <reason>");
                return true;
            }

            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                reason.append(args[i]);
                if (i != args.length - 1)
                    reason.append(" ");
            }
            ban(args[0], sender, reason.toString());

        } else if (command.getName().equalsIgnoreCase("unban")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
                return true;
            }
            unban(args[0], sender);

        } else if (command.getName().equalsIgnoreCase("addnote")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addnote <username> <message>");
            }
            StringBuilder note = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                note.append(args[i]);
                if (i != args.length - 1)
                    note.append(" ");
            }
            addnote(args[0], sender, note.toString());
        } else if (command.getName().equals("lookup")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /lookup <player>");
                return true;
            }
            lookup(args[0], sender);
        }
        return true;
    }

    void staffBroadcast(String message) {
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("anathema.access")) {
                p.sendMessage(ChatColor.GREEN + "[ANATHEMA] " + ChatColor.WHITE + message);
            }
        }
    }

    void ban(String username, CommandSender sender, String reason) {
        try {
            banAPI.addBan(username, sender.getName(), reason, config.SERVERNAME);
            staffBroadcast(username + " was banned by " + sender.getName() + ". Reason: " + reason);
            Player p = getServer().getPlayer(username);
            if (p != null)
                p.kickPlayer(String.format("You have been banned. \nrReason: %s\n%s", reason, config.BANAPPEND));
            else
                sender.sendMessage(ChatColor.GRAY + "Player is not online, not kicked");
            sender.sendMessage(ChatColor.GREEN + "Player banned");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Player was not banned. Please contact tech staff.");
            getLogger().severe("Error while trying to ban player. Username: " + username + " Issuer: " + sender.getName() +
                    " Message: " + e.getMessage());
        }
    }

    void unban(String username, CommandSender sender) {
        try {
            banAPI.delBan(username, sender.getName());
            staffBroadcast(username + " was unbanned by " + sender.getName());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Player was not unbanned. Please contact tech staff.");
            getLogger().severe("Error while trying to ban player. Username: " + username + " Issuer: " + sender.getName() +
                    " Message: " + e.getMessage());
        }
    }

    void addnote(String username, CommandSender sender, String note) {
        try {
            banAPI.addNote(username, sender.getName(), note, config.SERVERNAME);
            staffBroadcast(String.format("Note added to %s by %s: %s", sender.getName(), username, note));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Note was not added. Please contact tech staff.");
            getLogger().severe("Error while trying to add note to player. Username: " + username + " Issuer: " + sender.getName() +
                    " Note: " + e.getMessage());

        }

    }

    void lookup(String username, CommandSender sender) {
        try {
            List<Ban> localBans = banAPI.getLocalBans(username, "true");
            List<Note> localNotes = banAPI.getLocalNotes(username, "true");
            List<Alt> alts = altAPI.getAlts(username);

            sender.sendMessage(String.format("%s%s---%s%sLookup for %s%s%s---", ChatColor.STRIKETHROUGH, ChatColor.DARK_GRAY, ChatColor.RESET, ChatColor.GREEN, username, ChatColor.STRIKETHROUGH, ChatColor.DARK_GRAY));

            sender.sendMessage(String.format("%s%sBans", ChatColor.ITALIC, ChatColor.RED));
            if (localBans.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no local bans", ChatColor.GRAY, username));
            } else {
                for (Ban b : localBans) {
                    sender.sendMessage(String.format("    %s %s %s-%s", b.id, b.reason, ChatColor.DARK_PURPLE, b.issuer));
                }
            }

            sender.sendMessage(String.format("%s%sNotes", ChatColor.ITALIC, ChatColor.YELLOW));
            if (localNotes.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no local notes", ChatColor.GRAY, username));
            } else {
                for (Note n : localNotes) {
                    sender.sendMessage(String.format("    %s %s %s-%s", n.id, n.note, ChatColor.DARK_PURPLE, n.issuer));
                }
            }

            sender.sendMessage(String.format("%s%sAlts", ChatColor.ITALIC, ChatColor.BLUE));
            if (alts.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no alts", ChatColor.GRAY, username));
            } else {
                for (Alt a : alts) {
                    sender.sendMessage(String.format("    %s %sLast login: %s", a.alt, ChatColor.GRAY, a.last_login));
                }
            }

            sender.sendMessage(String.format("%s%s-----", ChatColor.BOLD, ChatColor.GRAY));

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Lookup failed. Please contact tech staff.");
            getLogger().severe("Error while doing lookup. Message: " + e.getMessage());
        }
    }


}
