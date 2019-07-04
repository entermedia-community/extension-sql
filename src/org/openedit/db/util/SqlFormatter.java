package org.openedit.db.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.OpenEditRuntimeException;
import org.openedit.data.PropertyDetail;
import org.openedit.users.User;
import org.openedit.users.UserManager;
import org.openedit.util.DateStorageUtil;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

public class SqlFormatter
{
	private static final Log log = LogFactory.getLog(SqlFormatter.class);
	
	protected XmlArchive fieldXmlArchive;
	protected XmlFile fieldDataTypeLookUp;
	protected UserManager fieldUserManager;
	protected Map fieldFormats;
	protected Map fieldDateFormats;
	protected ColumnFormat fieldDefaultFormat;
	
	public String toSqlString(PropertyDetail inDetail, String inValue)
	{
		if( inDetail == null)
		{
			return inValue;
		}
		if( !inDetail.isDataType("boolean") &&  inValue == null)
		{
			return "null";
		}
		ColumnFormat format = getFormat( inDetail);
		boolean escape = format.isEscaped();

		if( escape)
		{
			if( format.getSqlType().equals("DATE") || format.getSqlType().equals("DATETIME"))
			{
				Date obj = DateStorageUtil.getStorageUtil().parseFromStorage(inValue);
				//save in DB specific format
				
				inValue = getDateFormat(format.getDateFormat()).format(obj);
			}
			
			StringBuffer buffer = new StringBuffer(inValue.length() + 2);
			buffer.append('\'');
			buffer.append(inValue);
			buffer.append("' ");
			return buffer.toString();
		}
		else
		{
			if( inDetail.isDataType("boolean"))
			{
				if( Boolean.parseBoolean(inValue))
				{
					inValue = "1";
				}
				else
				{
					inValue = "0";
				}
			}
			return inValue;
		}
	}
	public ColumnFormat getFormat(PropertyDetail inDetail)
	{
		ColumnFormat format = null;
		String type = inDetail.getType();
		if( type != null)
		{
			format = (ColumnFormat)getFormats().get(type);
			if( format == null)
			{
				throw new OpenEditRuntimeException("/openedit/configuration/mapping/*.xml missing data type " + type + " used on " + inDetail.getId());
			}
		}
		if( format == null)
		{
			getFormats();
			format = getDefaultFormat();
		}
		return format;
	}
	public String getSqlType( PropertyDetail inDetail)
	{
		ColumnFormat format = getFormat(inDetail);
		return format.getSqlType();
//		String sqltype = "VARCHAR(255)";
//		Element element = getDataTypeLookUp().getElementById(inDetail.getType());
//		if( element != null)
//		{
//			sqltype = element.attributeValue("sqltype");
//		}
//		return sqltype;
	}
	public XmlFile getDataTypeLookUp()
	{
		if (fieldDataTypeLookUp == null)
		{
			fieldDataTypeLookUp = getXmlArchive().getXml("/system/data/mapping/" + getType() + ".xml");
			
		}
		return fieldDataTypeLookUp;
	}
	private String getType()
	{
		return "mysql";
		/*
		User dbuser = getUserManager().getUser("dbuseraccount");
		
		if( dbuser == null)
		{
			throw new OpenEditRuntimeException("Must have dbuser created with dbmappingtype set");
		}
		String type = dbuser.get("dbmappingtype");
		if( type == null)
		{
			String dbclass = dbuser.get("dbclass");
			if( dbclass != null)
			{
				type = "mysql"; //default
				if( dbclass.startsWith( "net.sourceforge.jtds") )
				{
					type = "mssql";
				}
			}
		}
		return type;
		*/
	}
	public void setDataTypeLookUp(XmlFile inDataTypeLookUp)
	{
		fieldDataTypeLookUp = inDataTypeLookUp;
	}
	public XmlArchive getXmlArchive()
	{
		return fieldXmlArchive;
	}
	public void setXmlArchive(XmlArchive inXmlArchive)
	{
		fieldXmlArchive = inXmlArchive;
	}
	public UserManager getUserManager()
	{
		return fieldUserManager;
	}
	public void setUserManager(UserManager inUserManager)
	{
		fieldUserManager = inUserManager;
	}
	public Map getFormats()
	{
		if (fieldFormats == null)
		{
			fieldFormats = new HashMap();
			for (Iterator iterator = getDataTypeLookUp().getElements().iterator(); iterator.hasNext();)
			{
				Element element = (Element) iterator.next();
				ColumnFormat format = new ColumnFormat();
				format.setEscaped(Boolean.parseBoolean( element.attributeValue("escaped")));
				format.setJavaType(element.attributeValue("id"));
				format.setSqlType(element.attributeValue("sqltype"));
				format.setDateFormat(element.attributeValue("format"));
				fieldFormats.put(format.getJavaType(),format);
			}
			String type = getDataTypeLookUp().getRoot().attributeValue("defaulttype");
			setDefaultFormat((ColumnFormat)getFormats().get(type));
		}
		return fieldFormats;
	}
	public String toCreateTable(String inTableName, PropertyDetail inId)
	{
		String type = getSqlType(inId);
		//Use AUTO Increment?
		if( type.equalsIgnoreCase("INT"))
		{
			String 	sql = null;
			if(getType().equals("mysql")){
			 	sql = "CREATE TABLE " + inTableName + " (id " + type + " NOT NULL AUTO_INCREMENT PRIMARY KEY)";
			}
			else if (getType().equals("mssql")) {
				sql = "CREATE TABLE " + inTableName + " (id " + type + " NOT NULL IDENTITY PRIMARY KEY)";
			}
			return sql;
		}
		else
		{
			String 	sql = "CREATE TABLE " + inTableName + " (id " + type + " NOT NULL PRIMARY KEY)";
			return sql;
			
		}
	}
	public Map getDateFormats()
	{
		if (fieldDateFormats == null)
		{
			fieldDateFormats = new HashMap();
		}
		return fieldDateFormats;
	}
	public DateFormat getDateFormat( String inTemplate)
	{
		DateFormat format = (DateFormat)getDateFormats().get(inTemplate);
		if( format == null)
		{
			format = new SimpleDateFormat(inTemplate);
			getDateFormats().put( inTemplate,format);
		}
		return format;
	}
	public void setDateFormats(Map inDateFormats)
	{
		fieldDateFormats = inDateFormats;
	}
	public ColumnFormat getDefaultFormat()
	{
		return fieldDefaultFormat;
	}
	public void setDefaultFormat(ColumnFormat inDefaultFormat)
	{
		fieldDefaultFormat = inDefaultFormat;
	}
}
