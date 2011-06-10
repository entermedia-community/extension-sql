package org.openedit.db.accounts;

import org.openedit.Data;
import org.openedit.db.BaseDbSearcher;
import org.openedit.users.GroupSearcher;

import com.openedit.users.BaseGroup;
import com.openedit.users.Group;

public class DbGroupSearcher extends BaseDbSearcher implements GroupSearcher
{

	protected GroupSearcher fieldFallBackSearcher;

	
	public Group getGroup(String inGroupId)
	{
		Group found =  (Group)searchById(inGroupId);
		if( found == null)
		{
			found = getFallBackSearcher().getGroup(inGroupId);
		}
		return found;
	}
	protected String getEntityName()
	{
		return "BaseGroup";
	}
	public GroupSearcher getFallBackSearcher()
	{
		return fieldFallBackSearcher;
	}
	public void setFallBackSearcher(GroupSearcher inFallBackSearcher)
	{
		fieldFallBackSearcher = inFallBackSearcher;
	}
	public Data createNewData()
	{
		return new BaseGroup();
	}
}
