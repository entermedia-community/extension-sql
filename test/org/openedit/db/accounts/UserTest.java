package org.openedit.db.accounts;

import org.entermediadb.asset.modules.AdminModule;
import org.openedit.BaseTestCase;
import org.openedit.WebPageRequest;
import org.openedit.data.SearcherManager;
import org.openedit.users.Group;
import org.openedit.users.GroupSearcher;
import org.openedit.users.User;
import org.openedit.users.UserSearcher;

public class UserTest extends BaseTestCase
{
	public void testGroup() throws Exception
	{
		SearcherManager manager = (SearcherManager)getFixture().getModuleManager().getBean("searcherManager");
		GroupSearcher searcher = (GroupSearcher)manager.getSearcher("openedit", "group");
		Group testgroup  = searcher.getGroup("testdbgroup");
		if( testgroup == null)
		{
			testgroup = (Group)searcher.createNewData();
			testgroup.setId("testdbgroup");
			testgroup.setName("Test Group");
			testgroup.addPermission("something");
			searcher.saveData( testgroup, null);
		}
	}
	
	public void testUser() throws Exception
	{
		SearcherManager manager = (SearcherManager)getFixture().getModuleManager().getBean("searcherManager");
		UserSearcher searcher = (UserSearcher)manager.getSearcher("openedit", "user");
		User testuser  = searcher.getUser("testdbuser");
		if( testuser == null)
		{
			testuser = (User)searcher.createNewData();
			testuser.setUserName("testdbuser");
			testuser.setPassword("1234");
			GroupSearcher gsearcher = (GroupSearcher)manager.getSearcher("openedit", "group");
			Group testgroup  = gsearcher.getGroup("testdbgroup");
			testuser.addGroup(testgroup);
			searcher.saveData(testuser, null);
		}
		AdminModule module = (AdminModule)getModule("Admin");
		WebPageRequest req = getFixture().createPageRequest();
		req.setRequestParameter("accountname","testdbuser");
		req.setRequestParameter("password","1234");
		req.setUser(null);
		module.login(req);
		assertNotNull(req.getUser());
		assertTrue( req.getUser().hasPermission("something"));
	}
	
}
