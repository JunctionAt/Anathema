package at.junction.anathema;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.bukkit.command.*;
import org.json.JSONException;

import at.junction.anathema.BanApi.APIException;
import at.junction.api.HttpException;
import at.junction.api.JunctionAPI;

public class ActionCommandExecutor implements CommandExecutor {
	
	final Pattern USER_REGEX = Pattern.compile("[0-9A-Za-z_]{1,16}");
	
	final IllegalArgumentException INCORRECT_USAGE = new IllegalArgumentException("Invalid usage. Please use the verb 'help' to see usage.");
	final IllegalArgumentException INVALID_USERNAME = new IllegalArgumentException("Invalid username.");
	final IllegalArgumentException INVALID_FLAG = new IllegalArgumentException("One of the provided flags wasn't valid.");
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final Anathema plugin = (Anathema)((PluginCommand)command).getPlugin();
		ASyncLookupExecutor asyncexec = new ASyncLookupExecutor(plugin, sender, sender.getName(), args);
		asyncexec.run();
		return true;
	}

	public class ASyncLookupExecutor implements Runnable {

		Anathema plugin;
		CommandSender sender;
		String senderName;
		String[] args;
		
		
		public ASyncLookupExecutor(Anathema plugin, CommandSender sender, String senderName, String[] args) {
			super();
			this.plugin = plugin;
			this.sender = sender;
			this.senderName = senderName;
			this.args = args;
		}
		
		public void run() {
			try {
				plugin.sendMessage(sender, runParse(plugin, senderName, args));
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				plugin.sendMessage(sender, e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (APIException e) {
				e.printStackTrace();
                plugin.sendMessage(sender, e.getMessage());
			}
		}
		
	}
	
	public String runParse(Anathema plugin, String sender, String[] args) throws IllegalArgumentException, HttpException, IOException, JSONException, APIException {
		
		if(args.length == 0)
			throw INCORRECT_USAGE;
		String verb = args[0];
		
		boolean inverted = (verb.startsWith("un") || verb.startsWith("del") || verb.startsWith("r")) ? true : false;
		
		String[] cargs = new String[]{};
		if (args.length>1) {
			cargs = Arrays.copyOfRange(args, 1, args.length);
		}
			
		if (verb == "help") {
			
		} else if (verb.endsWith("ban") || verb.endsWith("b")) {
			JunctionAPI api = plugin.getNewClient();
			//api.asUser(sender);
			if (inverted) {
				if(cargs.length<1)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				BanApi.delBan(api, cargs[0], sender);
			} else {
				if(cargs.length<2)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
                String reason = implodeArray(Arrays.copyOfRange(cargs, 1, cargs.length), " ");
				BanApi.addBan(plugin.server, api, cargs[0], sender, reason);
                plugin.kickIfOnline(cargs[0], reason + plugin.getConfig().getString("message.banappend"));
			}
		} else if (verb.endsWith("note") || verb.endsWith("n")) {
			JunctionAPI api = plugin.getNewClient();
			//api.asUser(sender);
			if (inverted) {
				if(cargs.length<1)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				try {
					BanApi.delNote(api, Integer.parseInt(cargs[0]), sender);
				} catch (NumberFormatException e) {
					throw INCORRECT_USAGE;
				}
			} else {
				if(cargs.length<2)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				BanApi.addNote(plugin.server, api, cargs[0], sender, implodeArray(Arrays.copyOfRange(cargs, 1, cargs.length), " "));
			}
		} else if (verb.equals("lookup") || verb.equals("l")) {
			if (cargs.length<1)
				throw INCORRECT_USAGE;
			boolean hasflags = cargs[0].startsWith("-");
			String flags = hasflags ? cargs[0].substring(1) : "";
			if (hasflags&&cargs.length<2)
				throw INCORRECT_USAGE;
			String username = cargs[hasflags ? 1 : 0];
			Scope scope = Scope.LOCAL;
			if (hasflags) {
				cargs = Arrays.copyOfRange(cargs, 1, cargs.length);
				for (char ch : flags.toCharArray()) {
					
					if (ch=='l') {
						scope = Scope.LOCAL;
					} else if (ch=='g') {
						scope = Scope.GLOBAL;
					} else if (ch=='f') {
						scope = Scope.FULL;
					} else {
						throw INVALID_FLAG;
					}
				}
			}
			if (scope == Scope.LOCAL) {
				plugin.contextStore.getUserContext(sender).setDataSource(BanApi.doLocalLookup(plugin.api, username));
			} else if (scope == Scope.GLOBAL) {
				plugin.contextStore.getUserContext(sender).setDataSource(BanApi.doGlobalLookup(plugin.api, username));
			} else if (scope == Scope.FULL) {
				plugin.contextStore.getUserContext(sender).setDataSource(BanApi.doFullLookup(plugin.api, username));
			}
			plugin.contextStore.getUserContext(username).setScope(scope);
			return plugin.contextStore.getUserContext(sender).generateOverview();
		}
		return "Success.";
	}
	
	public boolean isUsernameValid(String username) {
		return USER_REGEX.matcher(username).matches();
	}
	
	public static String implodeArray(String[] inputArray, String glueString) {
		String output = "";
		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);
			for (int i=1; i<inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}
			output = sb.toString();
		}
		return output;
	}
	
	enum Scope {
		LOCAL(), 
		GLOBAL(), 
		FULL();
	}
}
