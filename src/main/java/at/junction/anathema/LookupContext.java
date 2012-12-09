package at.junction.anathema;

import java.util.ArrayList;

public class LookupContext {

	/*
	static final int 	GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 55 
	static final int 	AVERAGE_CHAT_PAGE_WIDTH = 65
	static final int 	OPEN_CHAT_PAGE_HEIGHT = 20
	static final int 	CLOSED_CHAT_PAGE_HEIGHT = 10
	*/
	
	private ArrayList<Ban> bans;
	private int banCount;
	private ArrayList<Note> notes;
	private int noteCount;
	private String username;
	
	public void setDataSource(ILookupDatasource data) {
		bans = data.getBans();
		banCount = bans==null ? 0 : bans.size();
		notes = data.getNotes();
		noteCount = notes==null ? 0 : notes.size();
		username = data.getUsername();
	}
	
	public String generateOverview() {
		String response = "-------- LOOKUP START --------";
		if(banCount!=0 || noteCount!=0) {
			response = response + "\n" + banCount + " bans and " + noteCount + " notes found on " + username + ":";
			response = response + "\nBans:";
			response = response + genBanList(3);
			response = response + "\nNotes:";
			response = response + genNoteList(3);
		} else {
			response = response + "\nNothing found on the user " + username;
		}
		return response + "\n--------- LOOKUP END ---------";
	}
	
	private String genBanList(int max) {
		String response = "";
		if(banCount!=0) {
			int displaycount = banCount>max ? max : banCount;
			for(int i = 0; i<displaycount; i++) {
				Ban currban = bans.get(i);
				response = 	response + 
							"\n" + "\"" + 
							shortenText(currban.reason, 18 + (16-currban.issuer.length()), "[..]") + 
							"\" by " + 
							currban.issuer + 
							" on " + 
							shortenText(currban.server, 6, "_") + 
							"/" + 
							shortenText(currban.system, 4, "");
			} //Prep: 1 - Text: 18 - Binder: 5 - Issuer: 16 - Binder: 4 - Server: 6 - Binder: 1 - System: 4
		} else {
			return "\nNo bans.";
		}
		return response;
	}
	
	private String genNoteList(int max) {
		String response = "";
		if(noteCount!=0) {
			int displaycount = noteCount>max ? max : noteCount;
			for(int i = 0; i<displaycount; i++) {
				Note currnote = notes.get(i);
				response = 	response + 
							"\n" + "\"" + 
							shortenText(currnote.note, 18 + (16-currnote.issuer.length()), "[..]") + 
							"\" by " + 
							currnote.issuer + 
							" on " + 
							shortenText(currnote.server, 6, "_") + 
							"/" + 
							shortenText(currnote.system, 4, "");
			} //Prep: 1 - Text: 18 - Binder: 5 - Issuer: 16 - Binder: 4 - Server: 6 - Binder: 1 - System: 4
		} else {
			return "\nNo notes.";
		}
		return response;
	}
	
	private String shortenText(String text, int maxlen, String appendor) {
		if(text != null) {
			return text.length() > maxlen ? text.substring(0, maxlen-appendor.length()) + appendor : text;
		}
		return "";
	}
	
}
