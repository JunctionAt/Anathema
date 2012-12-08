package at.junction.anathema;

public class Note {
	
	public String note;
	public String issuer;
	public String system;
	public String server;
	public int id;
	
	Note(String note, String issuer, String system, String server, int id) {
		this.note = note;
		this.issuer = issuer;
		this.system = system;
		this.server = server;
		this.id = id;
	}
}
