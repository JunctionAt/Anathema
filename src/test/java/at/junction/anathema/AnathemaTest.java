package at.junction.anathema;

import java.io.IOException;

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
	
	private final String base = "https://wiggitywhack-dev.junction.at";
	
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

    public void testTrue()
    {
        assertTrue( true );
    }
    
    public void testLookup() throws HttpException, IOException, JSONException, APIException
    {
    	final JunctionClient client = new JunctionClient(base, "wiggitywhack", "password");
    	LookupResponse result = BanApi.doFullLookup(client, "testuser");
    	assertNotNull(result);
    }
}
