/*
 * Created on Mar 24, 2004
 */
package org.openedit.store.db;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author jguerre
 * 
 */
public class AllTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite("tracker Test");
		suite.addTestSuite(DbTest.class);
		return suite;
	}
}
