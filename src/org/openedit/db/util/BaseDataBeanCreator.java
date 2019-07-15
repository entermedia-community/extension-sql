package org.openedit.db.util;

import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.data.BaseData;

public class BaseDataBeanCreator
{
	protected Class fieldDataClass;
	
	public Object newInstance()
	{
		if( fieldDataClass == null)
		{
			return new BaseData();
		}
		try
		{
			return (Data)getDataClass().newInstance();
		}	
		catch (Exception e)
		{
			throw new OpenEditException(e);
		}
	}

	/**
	 * This will pass in a mostly already created bean in case we want to load up relationships
	 * Might consider lazy loading on the getter i.e. getRelatedProducts() -> Calls the database
	 * @param inData
	 */
	public void populateExtraData(Data inData)
	{
		// TODO Auto-generated method stub
		
	}

	public Class getDataClass()
	{
		return fieldDataClass;
	}

	public void setDataClass(Class inDataClass)
	{
		fieldDataClass = inDataClass;
	}
}
