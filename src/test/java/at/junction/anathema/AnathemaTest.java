package at.junction.anathema;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;

import at.junction.anathema.BanApi;
import at.junction.anathema.BanApi.APIException;
import at.junction.api.HttpException;
import at.junction.api.JunctionClient;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for JunctionPlugin.
 */
public class AnathemaTest 
    extends TestCase
{
	
	private final String base = "https://hansihe-dev.junction.at";
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AnathemaTest(final String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(AnathemaTest.class);
    }
    
    public void testLookupAndConstructContext() throws HttpException, IOException, JSONException, APIException
    {
    	final JunctionClient client = new JunctionClient(base, "wiggitywhack", "password");
    	LookupResponse result = BanApi.doFullLookup(client, "notch");
    	assertNotNull(result);
    	LookupContext context = new LookupContext();
    	context.setDataSource(result);
    	String overview = context.generateOverview();
    	assertFalse(overview, overview==null || overview.isEmpty());
    }
    
    public void testBanAddLookupRemove() throws HttpException, IOException, JSONException, APIException, URISyntaxException {
    	final JunctionClient client = new JunctionClient(base, "wiggitywhack", "password");
    	client.asUser("JUnitTest1");
    	BanApi.delBan(client, "JUnitTest2");
    	BanApi.addBan("JUnit_Test", client, "JUnitTest2", "Test reason");
    	LookupResponse result1 = BanApi.getLocalBans(client, "JUnitTest2");
    	assertFalse("No bans found in lookup!", result1.getBans()==null || result1.getBans().size()==0);
    	BanApi.delBan(client, "JUnitTest2");
    	LookupResponse result2 = BanApi.getLocalBans(client, "JUnitTest2");
    	assertFalse("Found ban after delBan!", result2.getBans()!=null);
    }
}
