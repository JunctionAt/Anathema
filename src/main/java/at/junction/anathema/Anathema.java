package at.junction.anathema;

import at.junction.api.BanAPI;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Anathema extends JavaPlugin {


    public static BanAPI banAPI = new BanAPI("https://junction.at/api", "d6e37442-9c5d-48f7-be8b-46cac283ed2e");
    @Override
    public void onEnable(){
        getLogger().info("Enabled Anathema");
    }

    @Override
    public void onDisable(){
        getLogger().info("Disabled Anathema");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if(command.getName().equalsIgnoreCase("a")) {
            if (args.length > 1){
                if (args[0].equalsIgnoreCase("ban")){
                    Player banned = Bukkit.getPlayer(args[1]);
                    String reason = "";
                    for (int i=2; i<args.length; i++) reason += args[i];
                    banned.kickPlayer("BANNED: " + reason);
                    try {
                        getLogger().info(banAPI.addBan(banned.getName(), sender.getName(), reason, "test"));
                    } catch (Exception e){
                        getLogger().severe("ERROR");    
                    }
                    return true;
                }
                return false;
            } else {
                return false;
            }
        }
        return false;
    }
}
