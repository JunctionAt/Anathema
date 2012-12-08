package at.junction.anathema;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.lang.Character;

import org.bukkit.command.*;
import org.json.JSONException;

import at.junction.anathema.BanApi.APIException;
import at.junction.api.HttpException;
import at.junction.api.JunctionAPI;

public class AnathemaCommandExecutor implements CommandExecutor {
	
	final Pattern USER_REGEX = Pattern.compile("[0-9A-Za-z_]{1,16}");
	
	final IllegalArgumentException INCORRECT_USAGE = new IllegalArgumentException("Invalid usage. Please use the verb 'help' to see usage.");
	final IllegalArgumentException INVALID_USERNAME = new IllegalArgumentException("Invalid username.");
	final IllegalArgumentException INVALID_FLAG = new IllegalArgumentException("One of the provided flags wasn't valid.");
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final Anathema plugin = (Anathema)((PluginCommand)command).getPlugin();
		try {
			plugin.sendMessage(sender, runParse(plugin, sender.getName(), args));
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (APIException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String runParse(Anathema plugin, String sender, String[] args) throws IllegalArgumentException, HttpException, IOException, JSONException, APIException {
		
		if(args.length > 0)
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
			api.asUser(sender);
			if (inverted) {
				if(cargs.length<1)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				BanApi.delBan(api, cargs[0]);
			} else {
				if(cargs.length<2)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				BanApi.addBan(plugin, api, cargs[0], implodeArray(Arrays.copyOfRange(cargs, 1, cargs.length), " "));
			}
		} else if (verb.endsWith("note") || verb.endsWith("n")) {
			JunctionAPI api = plugin.getNewClient();
			api.asUser(sender);
			if (inverted) {
				if(cargs.length<1)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				try {
					BanApi.delNote(api, Integer.parseInt(cargs[0]));
				} catch (NumberFormatException e) {
					throw INCORRECT_USAGE;
				}
			} else {
				if(cargs.length<2)
					throw INCORRECT_USAGE;
				if(!isUsernameValid(cargs[0]))
					throw INVALID_USERNAME;
				BanApi.addNote(plugin, api, cargs[0], implodeArray(Arrays.copyOfRange(cargs, 1, cargs.length), " "));
			}
		} else if (verb.equals("lookup") || verb.equals("l")) {
			if (cargs.length<1)
				throw INCORRECT_USAGE;
			boolean hasflags = cargs[0].startsWith("-");
			String flags = hasflags ? args[0].substring(1) : "";
			if (hasflags&&cargs.length<2)
				throw INCORRECT_USAGE;
			String username = cargs[hasflags ? 1 : 0];
			Scope scope = Scope.LOCAL;
			if (hasflags) {
				cargs = Arrays.copyOfRange(cargs, 1, cargs.length);
				for (char ch: flags.toCharArray()) {
					if (ch==new Character('l')) {
						scope = Scope.LOCAL;
					} else if (ch==new Character('g')) {
						scope = Scope.GLOBAL;
					} else if (ch==new Character('f')) {
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
			return plugin.contextStore.getUserContext(sender).generateOverview();
		}
		return null;
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
