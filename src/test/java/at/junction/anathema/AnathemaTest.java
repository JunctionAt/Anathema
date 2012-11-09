package at.junction.plugin;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for JunctionPlugin.
 */
public class AnathemaTest 
    extends TestCase
{
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
        return new TestSuite(JunctionPluginTest.class);
    }

    public void testTrue()
    {
        assertTrue( true );
    }
}
