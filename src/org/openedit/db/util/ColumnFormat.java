package org.openedit.db.util;

public class ColumnFormat
{
	protected boolean fieldEscaped;
	protected String fieldSqlType;
	protected String fieldJavaType;
	protected String fieldDateFormat;
	
	public boolean isEscaped()
	{
		return fieldEscaped;
	}
	public void setEscaped(boolean inEscaped)
	{
		fieldEscaped = inEscaped;
	}
	public String getSqlType()
	{
		return fieldSqlType;
	}
	public void setSqlType(String inSqlType)
	{
		fieldSqlType = inSqlType;
	}
	public String getJavaType()
	{
		return fieldJavaType;
	}
	public void setJavaType(String inJavaType)
	{
		fieldJavaType = inJavaType;
	}
	public String getDateFormat()
	{
		return fieldDateFormat;
	}
	public void setDateFormat(String inDateFormat)
	{
		fieldDateFormat = inDateFormat;
	}
	
}
