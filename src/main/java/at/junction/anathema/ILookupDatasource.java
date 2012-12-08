package at.junction.anathema;

import java.util.ArrayList;

public interface ILookupDatasource {

	public ArrayList<Ban> getBans();
	
	public ArrayList<Note> getNotes();
	
	public String getUsername();
	
}
