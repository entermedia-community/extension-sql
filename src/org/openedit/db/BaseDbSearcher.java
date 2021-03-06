package org.openedit.db;

import java.io.File;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.dom4j.Element;
import org.entermediadb.asset.search.DataConnector;
import org.openedit.Data;
import org.openedit.ModuleManager;
import org.openedit.OpenEditException;
import org.openedit.WebPageRequest;
import org.openedit.data.BaseSearcher;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.db.util.DataMapper;
import org.openedit.db.util.DbConnection;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.hittracker.Term;
import org.openedit.users.User;
import org.openedit.xml.ElementData;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

/**
 * Searcher - Public API
 * DbConnection - DB API requires a mapper and the data
 * ConnectionPool used by DBConnection
 * @author cburkey
 */

public class BaseDbSearcher extends BaseSearcher implements DataConnector 
{
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(BaseDbSearcher.class);
	protected DbConnection fieldDbConnection;
	protected DataMapper fieldDataMapper;
	protected org.openedit.ModuleManager fieldModuleManager;
	protected XmlArchive fieldXmlArchive;
	
	/**
	 * Returning a Data object
	 */
	public Object searchById(String inId)
	{
		return getDbConnection().searchById(inId,getDataMapper());
	}
	protected Object createObject(String inId, ResultSet inSet)
	{
		Object  obj = (Data)getDataMapper().getCached(inId);
		if( obj == null )
		{
			obj = getDataMapper().createNewBean();
		}
		getDataMapper().readInto(inSet, obj);
		return obj;
	}
	public HitTracker search(SearchQuery inQuery)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("select " + getSearchType() + ".* from " + getSearchType() );

		//append table joins if needed
		for (Iterator iterator = inQuery.getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			PropertyDetail detail = inQuery.getDetail(term.getId()); //search for category
			if( detail != null && "textjoin".equals(detail.getType() ))   //externalid is DB name. We need to know the id
			{
				sql.append( ", " );
				String table = detail.getExternalId();
				if( table == null)
				{
					table = term.getId();
				}
				table = table.substring(0,table.indexOf('.') );					

				sql.append( table );   //just needed the table in there for now
			}
		}		
		sql.append(' ');
		StringBuffer where = new StringBuffer();
		PropertyDetails fields = getDataMapper().getPropertyDetails();
		
		for (Iterator iterator = inQuery.getTerms().iterator(); iterator.hasNext();)
		{
			Term term = (Term) iterator.next();
			if( where.length() > 0)
			{
				where.append( " AND ");
			}
			//First the field name
			if(fields != null && term.getId().equals("description") )
			{
				if( "all".equals( term.getValue() ) )
				{
					break;
				}
				//loop over all the index fields with an OR statement? Yuck...					
				for (Iterator iterator2 = fields.findKeywordProperties().iterator(); iterator2.hasNext();)
				{
					PropertyDetail keyword = (PropertyDetail) iterator2.next();
					where.append( keyword.getId() );
					if( term.getValue().indexOf("*") > -1 || term.getValue().indexOf('%') > -1)
					{
						String val = term.getValue().replace('*', '%');
						where.append(" like '" + val + "'");					
					}
					else
					{
						where.append(" like '%" + term.getValue()+ "%'");
					}
					if( iterator2.hasNext())
					{
						where.append(" OR ");
					}
				}
			}
			else
			{
				PropertyDetail  detail = getDetail(term.getId());
				if( detail != null && "textjoin".equals(detail.getType() ) )   //externalid is DB name. We need to know the id
				{					
					where.append(detail.getQuery());
					where.append(" AND " );

					String joinid = detail.getExternalId();
					if( joinid == null)
					{
						joinid = term.getId();
					}
					where.append( joinid );
					where.append( getDataMapper().toEqualsSqlString(detail, term.getValue()) );
						//+ "LIKE '" + term.getValue() + "'");
				}
				else if( term.getValue().indexOf("*") > -1)
				{
					where.append(term.getId());
					String val = term.getValue().replace('*', '%');
					where.append(" like '" + val + "'");					
				}
				else
				{
					where.append(term.getId());
					where.append( getDataMapper().toEqualsSqlString(detail, term.getValue()) );
				}
			}
			where.append(" ");					
		}
		if( where.length() > 0)
		{
			sql.append(" where ");
			sql.append(where.toString());
		}
		String sort = inQuery.getSortBy();
		if( sort != null)
		{
			if( sort.endsWith("Up"))
			{
				sql.append( " order by " + sort.substring(0,sort.length() - 2));				
			}
			if( sort.endsWith("Down"))
			{
				sql.append( " order by " + sort.substring(0,sort.length() - 2) + " desc");				
			}
		}
		HitTracker tracker = getDbConnection().search(sql.toString(), getDataMapper());
		tracker.setSearcher(this);
		tracker.setSearchQuery(inQuery);
		log.info("final query: " + sql.toString());
		return tracker;
	}

	public SearchQuery createSearchQuery()
	{
		SearchDbQuery query = new SearchDbQuery();
		query.setPropertyDetails(getPropertyDetails());
		query.setCatalogId(getCatalogId());
		query.setSearcherManager(getSearcherManager());
		return query;
	}

	public HitTracker getAllHits()
	{
		return search(createSearchQuery());
	}

	public HitTracker getAllHits(WebPageRequest inReq)
	{
		return search(createSearchQuery());
	}

	public String getIndexId()
	{
		//in Case our list of properties is reloaded
		return String.valueOf( getDataMapper().getPropertyDetails().hashCode());
	}

	public void clearIndex()
	{
		// Done by the DBMS
	}

	public void reIndexAll() throws OpenEditException
	{
		// Done by the DBMS

	}

	public DbConnection getDbConnection()
	{
		return fieldDbConnection;
	}

	public void setDbConnection(DbConnection inConnection)
	{
		fieldDbConnection = inConnection;
	}
	public void delete(Data inData, User inUser)
	{
		delete(inData.getId());
		
	}
	public void delete(String inId)
	{
		getDbConnection().delete(getDataMapper(), inId);
	}
	
	public void deleteAll(User inUser)
	{
		throw new OpenEditException("not implemented");
	}
	public void saveAllData(List inAll, User inUser)
	{
		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			saveData(data, inUser);
		}
	}

	public DataMapper getDataMapper()
	{
		if (fieldDataMapper == null)
		{
			fieldDataMapper = createMapper();
			fieldDataMapper.setCatalogId(getCatalogId());
			fieldDataMapper.setSearchType(getSearchType());
			PropertyDetails details = getPropertyDetailsArchive().getPropertyDetailsCached(getSearchType());
			if( details == null)
			{
				details = new PropertyDetails();
				PropertyDetail detail = new PropertyDetail();
				detail.setId("id");
				details.addDetail(detail);
				detail = new PropertyDetail();
				detail.setId("name");
				details.addDetail(detail);
			}

			fieldDataMapper.setPropertyDetails(details);
/*
			try
			{
				//fieldDataMapper.createTable(getDbConnection().getConnection(fieldDataMapper));
				//load up the lists data?
				//importLegacyData();
			}
			catch (Exception ex)
			{
				log.info("Lack permissions to modify table " + getSearchType());
			}
*/			
		}
		return fieldDataMapper;
	}

	protected void importLegacyData()
	{
		//load up any old XML List/Product/Category data and add (0nly add data) it into the database
		//			XmlFile file = getP
		if( getXmlArchive() != null )
		{
			XmlFile file = getXmlArchive().getXml("/" + getCatalogId() + "/configuration/lists/" + getSearchType() + ".xml");
			if( file != null && file.isExist())
			{
				PropertyDetail id = getDetail("id");
				for (Iterator iterator = file.getRoot().elementIterator(); iterator.hasNext();)
				{
					Element row = (Element) iterator.next();
					ElementData data = new ElementData(row);
					if( id != null && "id".equals( id.getType() ) )
					{
						//needs to use auto increment
						data.setId(null);
					}
					saveData(data, null);
				}
				getXmlArchive().deleteXmlFile(file);
				file.setPath("/" + getCatalogId() + "/configuration/lists/imported" + getSearchType() + ".xml");
				getXmlArchive().saveXml(file, null);
			}
		}
	}
	public DataMapper createMapper()
	{
		DataMapper mapper = (DataMapper)getModuleManager().getBean(getCatalogId(), "dataMapper");
		
		return mapper;
	}
	public void setDataMapper(DataMapper inDataMapper)
	{
		fieldDataMapper = inDataMapper;
	}
	public Data createNewData()
	{
		return (Data)getDataMapper().createNewBean();
	}
	public void saveData(Data inData, User inUser)
	{
		Data data = (Data)inData;
		getDbConnection().saveData(data, getDataMapper(), inUser);
		
	}
	public ModuleManager getModuleManager()
	{
		return fieldModuleManager;
	}
	public void setModuleManager(ModuleManager inModuleManager)
	{
		fieldModuleManager = inModuleManager;
	}
	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}
	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}
	public void setDataClass(String inClassName)
	{
		//Just need to use another Data type
		Class cl = null;
		try
		{
			cl = Class.forName(inClassName);
		}
		catch (ClassNotFoundException e)
		{
			throw new OpenEditException(e);
		}
		getDataMapper().getBeanCreator().setDataClass(cl);
		
	}
	public void saveAllData(Collection<Data> inAll, User inUser)
	{
		for (Iterator iterator = inAll.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			saveData(data, inUser);
		}
		
	}
	@Override
	public void deleteFromIndex(String inId)
	{
		delete(inId);
	}
	@Override
	public void deleteFromIndex(HitTracker inOld)
	{
		for (Iterator iterator = inOld.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			delete(data.getId());
		}
	}
	@Override
	public void updateIndex(Data inOne)
	{
		saveData(inOne,	null);
	}

	@Override
	public void flush()
	{
		//not needed for sql
		
	}
	@Override
	public void setRootDirectory(File inRoot)
	{
		//not needed for SQL
		
	}
	@Override
	public Data getDataBySourcePath(String inSourcePath)
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Data getDataBySourcePath(String inSourcePath, boolean inAutocreate)
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void updateIndex(Collection<Data> inBuffer, User inUser)
	{
		// TODO Auto-generated method stub
		
	}
}
