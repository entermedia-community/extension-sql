package org.openedit.db.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;

import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;
import com.openedit.users.User;

public class DbConnection
{
	protected ConnectionPool fieldConnectionPool;
	private static final Log log = LogFactory.getLog(DbConnection.class);
	
	public ConnectionPool getConnectionPool()
	{
		return fieldConnectionPool;
	}

	public void setConnectionPool(ConnectionPool inConnectionPool)
	{
		fieldConnectionPool = inConnectionPool;
	}
	
	public void saveData(Data inData, DataMapper inMapper, User inUser)
	{
		Data data = (Data)inData;
		if( data.getId() != null)
		{
			String sqlupdate = inMapper.toUpdate(data, inUser);
		
			int count = runSql(inMapper, sqlupdate);
			if( count == 0)
			{
				insertData(inData, inMapper, inUser);
			}
		}
		else
		{
			insertData(data, inMapper, inUser);
		}
	}

	public int insertData(Data inData, DataMapper inMapper, User inUser)
	{
		String sqlinsert = inMapper.toInsert(inData, inUser);
			
		Connection con = null;
		try
		{
			con  =  getConnection(inMapper);
			PreparedStatement smt = con.prepareStatement(sqlinsert, Statement.RETURN_GENERATED_KEYS);
			
		
			log.info("Insert: " + sqlinsert);
			int count = smt.executeUpdate();
			//executeUpdate(sqlinsert);
			if(inData.getId() == null && count == 1)
			{
				ResultSet rs = smt.getGeneratedKeys();
				if( rs != null && rs.next() )
				{
					String id = rs.getString(1);
					inData.setId(id);
				}
			}
			return count;
		}
		catch ( SQLException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
		finally
		{
			getConnectionPool().close(con); //I think this auto closes the statements
		}
		
		
    	
		
		
		
	}

	protected int runSql(DataMapper inMapper, String sqlinsert)
	{
		Connection con = null;
		try
		{
			con  =  getConnection(inMapper);
			Statement smt = con.createStatement();
			//log.info(sqlinsert);
			int count = smt.executeUpdate(sqlinsert);
			return count;
		}
		catch ( SQLException ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
		finally
		{
			getConnectionPool().close(con); //I think this auto closes the statements
		}
	}
	
	
	
	
	
	/**
	 * Returning a Data object
	 */
	public Object searchById(String inId, DataMapper inMapper)
	{
		Object object = inMapper.getCached(inId);
		if( object != null)
		{
			return object;
		}
		//StringBuffer sql = new StringBuffer();
		String sql = inMapper.toSelect(inId);
		//sql.append("select * from " + inMapper.getSearchType() + " where id  = " + inId);
		Connection con = null;
		try
		{
			con  =  getConnection(inMapper);
			Statement smt = con.createStatement();
			ResultSet rset = smt.executeQuery(sql);
			if( rset.next())
			{
				object = inMapper.createNewBean();
				inMapper.readInto(rset, object);
			}
		}
		catch( SQLException ex)
		{
			log.error(sql);
			throw new OpenEditRuntimeException(ex);
		}
		finally
		{
			getConnectionPool().close(con);
		}
		return object;
	
	}
	public HitTracker search(String inSql, DataMapper inMapper)
	{
		Connection con = null;
		DbRowTracker tracker = null;
		try
		{
			con  =  getConnection(inMapper);
			Statement smt = con.createStatement();
			ResultSet rset = smt.executeQuery(inSql);
			tracker = new DbRowTracker(rset, inMapper, con); //We need to close this connection once data is all done
			
			tracker.size(); //Pulls all the records down for now
			return tracker;
		}
		catch ( SQLException ex)
		{
			log.info("failed to run: " + inSql);
			throw new OpenEditRuntimeException(ex);
		}
		finally
		{
			if( tracker != null)
			{
				log.info("ran: " + inSql + " found " + tracker.size());
			}
			getConnectionPool().close(con);
		}
	}

	public Connection getConnection(DataMapper inMapper)
	{
		Connection con = getConnectionPool().instance(inMapper.getCatalogId());
		return con;
	}

	
}
