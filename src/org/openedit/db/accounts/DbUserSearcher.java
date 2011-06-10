package org.openedit.db.accounts;

import java.util.List;

import org.openedit.Data;
import org.openedit.db.BaseDbSearcher;
import org.openedit.users.UserSearcher;

import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.users.BaseUser;
import com.openedit.users.Group;
import com.openedit.users.User;

public class DbUserSearcher extends BaseDbSearcher implements UserSearcher
{
	protected UserSearcher fieldFallBackSearcher;
	
	public User getUser(String inAccount)
	{
		User existing = (User)searchById(inAccount);
		if( existing == null && getFallBackSearcher() != null)
		{
			existing = getFallBackSearcher().getUser(inAccount);
		}
		return existing;
	}

	public User getUserByEmail(String inEmail)
	{
		SearchQuery query = createSearchQuery();
		query.addMatches("email",inEmail );
		HitTracker tracker = search(query);
		if( tracker.size() > 0)
		{
			return (User)tracker.get(0);
		}
		return null;
	}
	protected String getEntityName()
	{
		return "BaseUser";
	}
	public Data createNewData()
	{
		return new BaseUser();
	}

	public UserSearcher getFallBackSearcher()
	{
		return fieldFallBackSearcher;
	}

	public void setFallBackSearcher(UserSearcher inFallBackSearcher)
	{
		fieldFallBackSearcher = inFallBackSearcher;
	}

	//this may not be correct syntax
	public HitTracker getUsersInGroup(Group inGroup)
	{
		SearchQuery query = createSearchQuery();
		query.addMatches("groupid",inGroup.getId());
		query.setSortBy("namesorted");
		HitTracker tracker = search(query);
		return tracker;
	}

	public void saveUsers(List inUserstosave, User inUser)
	{
		// TODO Auto-generated method stub
		
	}

}
