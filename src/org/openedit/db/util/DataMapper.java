package org.openedit.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;

import com.openedit.OpenEditRuntimeException;
import com.openedit.users.User;


/**
 * Used to map DB results into BaseData objects (or any Class) setNewBeanClass
 * Requires that PropertyDetails are set for this searchtype
 * @author cburkey
 *
 */

public class DataMapper
{
	private static final Log log = LogFactory.getLog(DataMapper.class);
	
	protected PropertyDetails fieldPropertyDetails;
	protected String fieldCatalogId;
	protected String fieldSearchType;
	
	protected PropertyDetail[] fieldPropertyDetailList;
	protected Map fieldCache;

	protected SqlFormatter fieldSqlFormatter;
	
	protected BaseDataBeanCreator fieldBeanCreator;
	
	
	public DataMapper()
	{
	}
	
	/**
	 * This is the main API
	 * @param inSet
	 * @param inData
	 */
	public void readInto(ResultSet inSet, Object inData)
	{
		try
		{
			Data data = (Data)inData;
			//get all the properties out of this resultset that are indexed (stored?)
			for (int i = 0; i < getPropertyDetailsList().length; i++)
			{
				PropertyDetail detail = getPropertyDetailsList()[i];
				if( !"textjoin".equals( detail.getType()) )
				{
					String col = detail.getId();
					data.setProperty(col, inSet.getString(col));
				}
			}
			getCache().put(data.getId(),data);
		}
		catch (SQLException e)
		{
			throw new OpenEditRuntimeException(e);
		}
	}
	
	/**
	 * Only reload this list when needed
	 * @return
	 */
	
	public PropertyDetail[] getPropertyDetailsList()
	{
		if (fieldPropertyDetailList == null)
		{
			List all = getPropertyDetails().getDetails();
			fieldPropertyDetailList = 	(PropertyDetail[])all.toArray(new PropertyDetail[all.size()]);
		}
		return fieldPropertyDetailList;
	}

	public PropertyDetails getPropertyDetails()
	{
		return fieldPropertyDetails;
	}

	public void setPropertyDetails(PropertyDetails inPropertyDetails)
	{
		if( inPropertyDetails != fieldPropertyDetails)
		{
			fieldPropertyDetailList = null;
		}
		fieldPropertyDetails = inPropertyDetails;
	}
	public Object getCached(String inId)
	{
		return getCache().get(inId);
	}
	
	public Data populateData(String id, ResultSet inValues)
	{
		Data  row = (Data)getCached(id);
		boolean created = false;
		if( row == null )
		{
			row = createNewBean();
			created = true;
		}
		readInto(inValues, row);
		if( created )
		{
			getBeanCreator().populateExtraData(row);
		}

		return row;
	}


	
	public Data createNewBean()
	{
		return (Data)getBeanCreator().newInstance();
	}
	
	public Map getCache()
	{
		if (fieldCache == null)
		{
			//Is more likely to keep values and keys since it does not require a reference but will drop them if we run out of mem
			fieldCache = new ReferenceMap(ReferenceMap.SOFT,ReferenceMap.SOFT);
		}
		return fieldCache;
	}

	public String getCatalogId()
	{
		return fieldCatalogId;
	}

	public void setCatalogId(String inCatalogId)
	{
		fieldCatalogId = inCatalogId;
	}

	public String getSearchType()
	{
		return fieldSearchType;
	}

	public void setSearchType(String inFieldName)
	{
		fieldSearchType = inFieldName;
	}

	public void createTable(Connection con)
	{
		String searchtype = getSearchType();
		PropertyDetails details = getPropertyDetails();
		createTable(con, searchtype, details);
	}

	public void createTable(Connection con, String searchtype, PropertyDetails details)
	{
		try
		{
			Statement smt = con.createStatement();
			PropertyDetail detail = details.getDetail("id");
			if(detail  == null)
			{
				throw new OpenEditRuntimeException("You must declare an id column for DB work");
			}
			log.info("Creating table for " + searchtype);
			String sql = getSqlFormatter().toCreateTable(searchtype, detail);
			smt.executeUpdate(sql);
		}
		catch ( SQLException ex)
		{
			//might already exists
			//ex.printStackTrace();
			if( !ex.toString().contains("already exist"))
			{
				log.info(ex);
			}
		}

		
		for (Iterator iterator = details.getDetails().iterator(); iterator.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail)iterator.next();
			if( detail.getType() == null || !detail.getType().endsWith("join"))
			{
				String type = getSqlType(detail);
				String  sql = null;
				try
				{
					//ID's cant start with numbers
					if( Character.isDigit( detail.getId().charAt(0) ) )
					{
						throw new OpenEditRuntimeException("Tables can't stat with a number " + searchtype +  " " + detail.getId());
					}
					sql = "ALTER TABLE "+searchtype+" ADD "+detail.getId()+" "+type + " DEFAULT NULL;";
	
					Statement smt = con.createStatement();			
					smt.executeUpdate(sql);				
				} 
				catch ( SQLException ex)
				{
					//might already exists
					//ex.printStackTrace();
					if( !ex.toString().contains("Duplicate column") && !ex.toString().contains("Column names in each table must be unique"))
					{
						log.error("error:" + sql);
						log.info(ex);
					}
				}
			}
		}
	}

	
	public String toSqlString(PropertyDetail inDetail, String inValue)
	{
		return getSqlFormatter().toSqlString(inDetail, inValue);
	}

	public String getSqlType(PropertyDetail inDetail)
	{
		return getSqlFormatter().getSqlType(inDetail);
	}
	
	public SqlFormatter getSqlFormatter()
	{
		return fieldSqlFormatter;
	}

	public void setSqlFormatter(SqlFormatter inSqlFormatter)
	{
		fieldSqlFormatter = inSqlFormatter;
	}
	
	//INSERT INTO table_name (column1, column2, column3,...)
	//VALUES (value1, value2, value3,...)
	
	//TODO: Handle more data types
	//create more SQL based on this type

	public String toInsert(Data data, User inUser)
	{
		StringBuffer sqlinsert = new StringBuffer("INSERT INTO ");
		
		sqlinsert.append(getSearchType());
		sqlinsert.append(" (");
		PropertyDetail[] all = getPropertyDetailsList();
		
		StringBuffer ids = new StringBuffer();
		StringBuffer values = new StringBuffer();
		for (int i = 0; i < all.length; i++)
		{
			String id = all[i].getId();
			String value = data.get(id);
			if( value != null)
			{
				if( ids.length() > 0 )
				{
					ids.append(',');
					values.append(',');
				}
				ids.append(id);
				values.append( toSqlString(all[i], value) );
				//values.append("'" + value + "'");
			}
		}
		sqlinsert.append(ids.toString());
		sqlinsert.append(") VALUES (");
		sqlinsert.append(values.toString());
		sqlinsert.append(" )");
		return sqlinsert.toString();
	}

	public String toUpdate(Data inData, User inUser)
	{
		//Just try an update first. If no update then do the insert
		StringBuffer sqlinsert = new StringBuffer("UPDATE ");
		sqlinsert.append(getSearchType());
		sqlinsert.append(" SET ");
		
//		SET column1=value, column2=value2,...		WHERE some_column=some_value");

		PropertyDetail[] all = getPropertyDetailsList();
		
		StringBuffer values = new StringBuffer();
		Data data = inData;
		for (int i = 0; i < all.length; i++)
		{
			String id = all[i].getId();
			if( "id".equals( id) )
			{
				continue;
			}
			String value = data.get(id);
			if( value != null)
			{
				if( values.length() > 0 )
				{
					values.append(',');
				}
				values.append(id);
				values.append("=");
				values.append( toSqlString(all[i], value) );
			}
		}
		sqlinsert.append( values.toString());
		sqlinsert.append(" WHERE ");
		addId(sqlinsert, data.getId());
		log.info("final insert: " + sqlinsert.toString());
		return sqlinsert.toString();
	}

	protected void addId(StringBuffer sqlinsert, String inId)
	{

		PropertyDetail detail = getPropertyDetails().getDetail("id");
		sqlinsert.append(detail.getId());
		
		if( "id".equals( detail.getType()) )
		{
			sqlinsert.append("=");
			sqlinsert.append(inId);
		}		
		else
		{
			sqlinsert.append(" LIKE '");
			sqlinsert.append(inId);
			sqlinsert.append("'");
		}
	}

	public BaseDataBeanCreator getBeanCreator()
	{
		if (fieldBeanCreator == null)
		{
			fieldBeanCreator = new BaseDataBeanCreator();
		}
		return fieldBeanCreator;
	}

	public void setBeanCreator(BaseDataBeanCreator inBeanCreator)
	{
		fieldBeanCreator = inBeanCreator;
	}

	public String toSelect(String inId)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("select * from " + getSearchType() );
		sql.append(" WHERE ");
		addId(sql, inId);
		return sql.toString();
	}

	public String toEqualsSqlString(PropertyDetail detail, String inValue) 
	{
		StringBuffer sql = new StringBuffer();
		
		ColumnFormat format = getSqlFormatter().getFormat( detail);
		boolean escape = format.isEscaped();

		if( escape )
		{
			sql.append(" LIKE ");
		}		
		else
		{
			sql.append("=");
		}
		sql.append(toSqlString(detail, inValue));

		return sql.toString();
	}

	public String toDelete(String inId)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM " + getSearchType());
		//sql.append(" WHERE id = '1'");
		//sql.append("select * from " + getSearchType() );
		sql.append(" WHERE ");
		addId(sql, inId);
		return sql.toString();
	}

}
