/*
 * Created on Aug 2, 2006
 */
package org.openedit.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openedit.OpenEditRuntimeException;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.util.PathUtilities;

public class ConnectionPool
{
	protected Map fieldConnectionFactories;
	protected UserManager fieldUserManager;
	
    public ConnectionPool() 
    {
    	
    }

    /**
     * Ok here is the plan:
     * 0. Create a dbuseraccount account to store most data in. You can also setup additional databases by appending the catalog id to the username such as: dbuseraccount.catalogid 
     * 1. We need to pass in the catalog id for unique catalog based connections. (each catalog should have it's own DB so that list can very by catalog)
     * 2. We find an open connection then pass that into the HitTracker for long running search. Once that search is to the end of list, or rerun we need to close that connection and get a fresh one
     * 3. For quick searches we can just open and close a connection. I assume the validation only happens after a timeout
     * @param inCatalogId
     * @return
     */
    public Connection instance(String inCatalogId) 
    {
    		if( getConnectionFactories().get(inCatalogId) == null)
    		{
    			synchronized (getConnectionFactories())
				{
    				String id = "dbuseraccount." + inCatalogId;
    				id = PathUtilities.makeId(id).toLowerCase();
    				User dbaccount = getUserManager().getUser(id);
    				if( dbaccount == null)
    				{
    					dbaccount = getUserManager().getUser("dbuseraccount");
    				}
    				if( dbaccount == null || dbaccount.get("dbusername") == null || dbaccount.get("dburl") == null)
    				{
    					throw new OpenEditRuntimeException("Please create user " + id + " with dbclass, dbusername and dbrul properties");
    				}
    				String dbclass = dbaccount.get("dbclass");
    				try
					{
						Class.forName(dbclass);
					}
					catch (ClassNotFoundException e)
					{
						throw new OpenEditRuntimeException(e);
					}
    				String dbusername = dbaccount.get("dbusername");
    				String url = dbaccount.get("dburl");
    				
		    		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory( url, dbusername, getUserManager().decryptPassword(dbaccount));
		
		    		GenericObjectPool connectionPool = new GenericObjectPool();
		
		    		// null can be used as parameter because this parameter is set in 
		    		// PoolableConnectionFactory when creating a new PoolableConnection
		    		KeyedObjectPoolFactory statementPool = new GenericKeyedObjectPoolFactory(null);
		
		    		final boolean defaultReadOnly = false;
		    		final boolean defaultAutoCommit = true;
		    		final String validationQuery = null; //I think the default is to set auto commit to true
		    		PoolableConnectionFactory factory = new PoolableConnectionFactory(connectionFactory, connectionPool, statementPool,
		    		      validationQuery, defaultReadOnly, defaultAutoCommit);
		    		
		    		PoolingDriver driver = new PoolingDriver();
		    		driver.registerPool(inCatalogId,connectionPool);
		    		
		    		getConnectionFactories().put(inCatalogId, connectionFactory);
				}
    		}
    		Connection conn = null;
			try
			{
				conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + inCatalogId);
			}
			catch (SQLException e)
			{
				throw new OpenEditRuntimeException(e);
			}
    		return conn;
   	}

	public Map getConnectionFactories()
	{
		if( fieldConnectionFactories == null)
		{
			fieldConnectionFactories = new HashMap();
		}
		return fieldConnectionFactories;
	}

	public void setConnectionFactories(Map inConnectionFactories)
	{
		fieldConnectionFactories = inConnectionFactories;
	}

	public UserManager getUserManager()
	{
		return fieldUserManager;
	}

	public void setUserManager(UserManager inUserManager)
	{
		fieldUserManager = inUserManager;
	}
	
	public void close(Connection inCon)
	{
		if( inCon != null)
		{
			try
			{
				inCon.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		
	}
}
	

