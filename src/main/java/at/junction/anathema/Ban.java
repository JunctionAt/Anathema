package at.junction.anathema;

public class Ban {
	
	public String reason;
	public String issuer;
	public String system;
	public String server;
	
	Ban(String reason, String issuer, String system, String server) {
		this.reason = reason;
		this.issuer = issuer;
		this.system = system;
		this.server = server;
	}
	
}
