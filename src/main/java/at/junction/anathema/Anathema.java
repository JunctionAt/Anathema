package at.junction.anathema;

//Import APIs

import at.junction.api.BanStatus;
import at.junction.api.rest.RestApi;
//Import Objects
import at.junction.api.rest.Alt;
import at.junction.api.rest.Ban;
import at.junction.api.rest.Note;

import at.junction.api.fields.PlayerIdentifier;


import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class Anathema extends JavaPlugin {

    RestApi restAPI;
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

        restAPI = new RestApi(config.ENDPOINT, config.APIKEY, config.SERVERNAME);

        getServer().getPluginManager().registerEvents(new AnathemaListener(this), this);
        getLogger().info("Enabled Anathema");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled Anathema");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sSorry, this command must be run from a player. Blame UUIDs.", ChatColor.RED));
            return true;
        }
        Player player = (Player) sender;
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
            ban(args[0], player, reason.toString());

        } else if (command.getName().equalsIgnoreCase("unban")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /unban <player>");
                return true;
            }
            unban(args[0], player);

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
            addnote(args[0], player, note.toString());
        } else if (command.getName().equals("lookup")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /lookup <player>");
                return true;
            }
            lookup(args[0], sender);
        }
        return true;
    }

    void staffBroadcast(String... message) {
        for (Player p : getServer().getOnlinePlayers()) {
            if (p.hasPermission("anathema.access")) {
                p.sendMessage(String.format("%s[ANATHEMA %s%s", ChatColor.GREEN, ChatColor.WHITE, join(null, message)));
            }
        }
    }

    void ban(String username, Player sender, String reason) {
        try {
            PlayerIdentifier target = restAPI.players().getPlayerByName(username);
            PlayerIdentifier issuer = PlayerIdentifier.apply(sender);
            restAPI.bans().addBan(target, issuer, reason);
            staffBroadcast(username, " was banned by ", sender.getName(), ". Reason: ", reason);
            Player p = getServer().getPlayer(target.uuid());
            if (p != null)
                p.kickPlayer(String.format("You have been banned. \nrReason: %s\n%s", reason, config.BANAPPEND));
            else
                sender.sendMessage(ChatColor.GRAY + "Player is not online, not kicked");
            sender.sendMessage(ChatColor.GREEN + "Player banned");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Player was not banned. Please contact tech staff.");
            getLogger().log(Level.SEVERE, "Error while trying to ban player. Username: " + username + " Issuer: ", e);
            e.printStackTrace();
        }
    }

    void unban(String username, Player sender) {
        try {
            PlayerIdentifier target = restAPI.players().getPlayerByName(username);
            PlayerIdentifier issuer = PlayerIdentifier.apply(sender);
            restAPI.bans().delBan(target, issuer);
            staffBroadcast(username, " was unbanned by ", sender.getName());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Player was not unbanned. Please contact tech staff.");
            getLogger().log(Level.SEVERE, "Error while trying to ban player. Username: " + username + " Issuer: " + sender.getName(), e);
            e.printStackTrace();
        }
    }

    void addnote(String username, Player sender, String note) {
        try {
            PlayerIdentifier target = restAPI.players().getPlayerByName(username);
            PlayerIdentifier issuer = PlayerIdentifier.apply(sender);
            restAPI.bans().addNote(target, issuer, note);
            staffBroadcast("Note added to", sender.getName(), " by ", username, ": ", note);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error has occurred. Note was not added. Please contact tech staff.");
            getLogger().log(Level.SEVERE, "Error while trying to add note to player. Username: " + username + " Issuer: ", e);
            e.printStackTrace();

        }

    }

    void lookup(String username, CommandSender sender) {
        PlayerIdentifier target = restAPI.players().getPlayerByName(username);

        sender.sendMessage(String.format("%s%s---%s%sLookup for %s%s%s---", ChatColor.STRIKETHROUGH, ChatColor.DARK_GRAY, ChatColor.RESET, ChatColor.GREEN, username, ChatColor.STRIKETHROUGH, ChatColor.DARK_GRAY));
        sender.sendMessage(String.format("%s%sBans", ChatColor.ITALIC, ChatColor.RED));

        try {
            List<Ban> localBans = restAPI.bans().getBans(target, BanStatus.Active);
            if (localBans.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no local bans", ChatColor.GRAY, username));
            } else {
                for (Ban b : localBans) {
                    sender.sendMessage(String.format("    %s %s %s-%s", b.id(), b.reason(), ChatColor.DARK_PURPLE, b.issuer()));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(String.format("%sError occured while looking up bans", ChatColor.RED));
        }

        sender.sendMessage(String.format("%s%sNotes", ChatColor.ITALIC, ChatColor.YELLOW));
        try {
            List<Note> localNotes = restAPI.bans().getNotes(target, BanStatus.Active);

            if (localNotes.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no local notes", ChatColor.GRAY, username));
            } else {
                for (Note n : localNotes) {
                    sender.sendMessage(String.format("    %s %s %s-%s", n.id(), n.note(), ChatColor.DARK_PURPLE, n.issuer()));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(String.format("%sError occured while looking up notes", ChatColor.RED));
            e.printStackTrace();

        }

        try {
            List<Alt> alts = restAPI.alts().getAlts(target);
            sender.sendMessage(String.format("%s%sAlts", ChatColor.ITALIC, ChatColor.BLUE));
            if (alts.size() == 0) {
                sender.sendMessage(String.format("    %s%s has no alts", ChatColor.GRAY, username));
            } else {
                for (Alt a : alts) {
                    sender.sendMessage(String.format("    %s %sLast login: %s", a.alt(), ChatColor.GRAY, a.last_login()));
                }
            }

            sender.sendMessage(String.format("%s%s-----", ChatColor.BOLD, ChatColor.GRAY));
        } catch (Exception e) {
            sender.sendMessage(String.format("%sError occured while looking up alts", ChatColor.RED));
            e.printStackTrace();
        }

    }

    String join(Character c, String... str) {
        StringBuilder sb = new StringBuilder();
        for (String s : str) {
            sb.append(s);
            if (c != null)
                sb.append(c);
        }
        return sb.toString();
    }

}