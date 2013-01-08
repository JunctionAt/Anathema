package at.junction.anathema;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.junction.api.*;
import at.junction.anathema.LookupResponse;

public class BanApi {
	
	//Fuck you compiler
	BanApi() {}
	
	private static LookupResponse parseJSONResponse(JSONObject json, String username) throws JSONException, APIException {
		if(!json.getBoolean("success")) {
			if(json.has("error"))
				throw new APIException(json.getString("error"));
			throw new APIException();
		}
		ArrayList<Note> notelist;
		ArrayList<Ban> banlist;
		if(!json.has("notes")) {
			notelist = null;
		} else {
			JSONArray jsonnotes = json.getJSONArray("notes");
			if(jsonnotes.length()==0)
			{
				notelist = null;
			} else {
				notelist = new ArrayList<Note>();
				for(int i=0; i<jsonnotes.length(); i++) {
					JSONObject current = jsonnotes.getJSONObject(i);
					notelist.add(new Note(current.getString("note"), current.getString("issuer"), current.has("system") ? current.getString("system") : null, current.getString("server"), current.has("id") ? current.getInt("id") : 0));
				}
			}
		}
		if(!json.has("bans")) {
			banlist = null;
		} else {
			JSONArray jsonbans = json.getJSONArray("bans");
			if(jsonbans.length()==0)
			{
				banlist = null;
			} else {
				banlist = new ArrayList<Ban>();
				for(int i=0; i<jsonbans.length(); i++) {
					JSONObject current = jsonbans.getJSONObject(i);
					banlist.add(new Ban(current.getString("reason"), current.getString("issuer"), current.has("system") ? current.getString("system") : null, current.getString("server")));
				}
			}
		}
		return new LookupResponse(banlist, notelist, username);
	}
	
	public static void addBan(String server, JunctionAPI api, String target, String issuer, String reason) throws HttpException, IOException, JSONException, APIException {
		JSONObject json = new JSONObject(api.addBan(target, issuer, reason, server));
		if(json.getBoolean("success")) {
			return;
		}
		if(json.has("error")) {
			throw new APIException(json.getString("error"));
		}
		throw new APIException();
	}
	
	public static void delBan(JunctionAPI api, String target) throws HttpException, IOException, JSONException, APIException {
		JSONObject json = new JSONObject(api.delBan(target));
		if(json.getBoolean("success")) {
			return;
		}
		if(json.has("error")) {
			throw new APIException(json.getString("error"));
		}
		throw new APIException();
	}
	
	public static void addNote(String server, JunctionAPI api, String target, String issuer, String note) throws HttpException, IOException, JSONException, APIException {
		JSONObject json = new JSONObject(api.addNote(target, issuer, note, server));
		if(json.getBoolean("success")) {
			return;
		}
		if(json.has("error")) {
			throw new APIException(json.getString("error"));
		}
		throw new APIException();
	}
	
	public static void delNote(JunctionAPI api, int id) throws HttpException, IOException, JSONException, APIException {
		JSONObject json = new JSONObject(api.delNote(id));
		if(json.getBoolean("success")) {
			return;
		}
		if(json.has("error")) {
			throw new APIException(json.getString("error"));
		}
		throw new APIException();
	}
	
	public static LookupResponse getLocalBans(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.getLocalBans(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse getLocalNotes(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.getLocalBans(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse doLocalLookup(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.doLocalLookup(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse getGlobalBans(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.getGlobalBans(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse getGlobalNotes(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.getLocalBans(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse doGlobalLookup(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.doGlobalLookup(username));
		return parseJSONResponse(json, username);
	}
	
	public static LookupResponse doFullLookup(JunctionAPI api, String username) throws HttpException, JSONException, IOException, APIException {
		JSONObject json = new JSONObject(api.doFullLookup(username));
		return parseJSONResponse(json, username);
	}
	
	public static boolean isBanned(JunctionAPI api, String username) {
		return false;
	}

	
	static class APIException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		public APIException()
		{
			super("unknown");
		}
		
		public APIException(String message)
		{
			super(message);
		}
	}
	
}
