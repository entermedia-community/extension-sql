package org.openedit.db.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openedit.Data;

import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;

public class DbRowTracker extends HitTracker
{
	protected ResultSet fieldResultSet;
	protected Connection fieldConnection;
	protected List fieldAllRows; //cache the rows of data as I create them until I run out?
	protected Integer fieldSize;
	protected DataMapper fieldDataMapper;
	
	public DbRowTracker()
	{
	}
	
	public DbRowTracker(ResultSet inRset, DataMapper inDataMapper, Connection inCon)
	{
		setResultSet(inRset);
		setDataMapper(inDataMapper);
		setConnection(inCon);
	}

	public boolean contains(Object inHit)
	{
		return getAllRows().contains(inHit);
	}
	public String getValue(Object inHit, String inKey)
	{
		Data data = (Data)inHit;
		return data.get(inKey);
	}

	public Data get(int inCount)
	{
		return (Data)getAllRows().get(inCount);
	}

	public Iterator iterator()
	{
		if( fieldAllRows != null)
		{
			return getAllRows().iterator();
		}
		return runiterator();
	}
	
	public Iterator runiterator()
	{
		//Reset?
		
		//We need to be one behind so we know if there is a hasNext
		boolean hasNext = moveNext();

		if( !hasNext)
		{
			return Collections.EMPTY_LIST.iterator();
		}

		Iterator iter = new Iterator()
		{
			Data fieldCurrentRow = createRow();
			public boolean hasNext()
			{
				return fieldCurrentRow != null;
			}

			public Object next()
			{
				Data lastRow = fieldCurrentRow;
				if( moveNext() )
				{
					fieldCurrentRow = createRow();
				}
				else
				{
					fieldCurrentRow = null;
				}
				return lastRow;
			}

			public void remove() {} //Not supported
		};
		return iter;
	}
	protected boolean moveNext()
	{
		try
		{
			return getResultSet().next();
		}
		catch (SQLException e)
		{
			throw new OpenEditRuntimeException(e);
		}
	}

	protected Data createRow()
	{
		try
		{
			String id = getResultSet().getString("id");
			Data row = getDataMapper().populateData(id, getResultSet());
			return row;
		}
		catch ( SQLException  ex)
		{
			throw new OpenEditRuntimeException(ex);
		}
	}

	public int size()
	{
		if( fieldSize == null)
		{
			fieldSize  = new Integer( getAllRows().size() );
		}
		return fieldSize.intValue();
	}

	public Data toData(Object inHit)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getResultSet()
	{
		return fieldResultSet;
	}

	public void setResultSet(ResultSet inResultSet)
	{
		fieldResultSet = inResultSet;
	}

	public Connection getConnection()
	{
		return fieldConnection;
	}

	public void setConnection(Connection inConnection)
	{
		fieldConnection = inConnection;
	}

	public List getAllRows()
	{
		if( fieldAllRows == null)
		{
			fieldAllRows = new ArrayList();
			for (Iterator iterator = runiterator(); iterator.hasNext();)
			{
				Object row = iterator.next();
				fieldAllRows.add(row);
			}
			setSize(new Integer(fieldAllRows.size()));
		}
		return fieldAllRows;
	}

	public void setAllRows(List inLoadedRows)
	{
		fieldAllRows = inLoadedRows;
	}

	public void setSize(Integer inSize)
	{
		fieldSize = inSize;
	}

	public DataMapper getDataMapper()
	{
		return fieldDataMapper;
	}

	public void setDataMapper(DataMapper inDataMapper)
	{
		fieldDataMapper = inDataMapper;
	}
}
