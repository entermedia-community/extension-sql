package org.openedit.db.accounts;

import java.util.List;

import org.openedit.Data;
import org.openedit.db.BaseDbSearcher;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;
import org.openedit.users.BaseUser;
import org.openedit.users.Group;
import org.openedit.users.User;
import org.openedit.users.UserSearcher;
import org.openedit.util.StringEncryption;

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

	@Override
	public User getUser(String inAccount, boolean inCached)
	{
		// TODO Auto-generated method stub
		return getUser(inAccount);
	}

	@Override
	public StringEncryption getStringEncryption()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encryptPassword(User inUser)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String decryptPassword(User inUser)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
