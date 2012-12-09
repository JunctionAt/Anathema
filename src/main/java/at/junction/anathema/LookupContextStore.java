package at.junction.anathema;

import java.util.HashMap;

public class LookupContextStore {

	private HashMap<String, LookupContext> contexts = new HashMap<String, LookupContext>();
	
	public LookupContext getUserContext(String username) {
		synchronized(contexts) {
			if(!contexts.containsKey(username)) {
				contexts.put(username, new LookupContext());
			}
			return contexts.get(username);
		}
	}
	
	public boolean removeUserContext(String username) {
		synchronized(contexts) {
			if(contexts.containsKey(username)) {
				contexts.remove(username);
				return true;
			}
			return false;
		}
	}
	
}
