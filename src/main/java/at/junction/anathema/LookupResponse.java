package at.junction.anathema;

import java.util.ArrayList;

public class LookupResponse implements ILookupDatasource {
	
	private final ArrayList<Ban> bans;
	private final ArrayList<Note> notes;
	private final String username;
	
	LookupResponse(ArrayList<Ban> banlist, ArrayList<Note> notelist, String username) {
		bans = banlist;
		notes = notelist;
		this.username = username;
	}
	
	public ArrayList<Ban> getBans() {
		return bans;
	}
	
	public ArrayList<Note> getNotes() {
		return notes;
	}

	public String getUsername() {
		return username;
	}
	
}
