package org.openedit.store.db;

import java.util.Collection;

import org.entermediadb.asset.BaseCategory;
import org.entermediadb.asset.BaseEnterMediaTest;
import org.entermediadb.asset.Category;
import org.entermediadb.asset.CategoryArchive;
import org.entermediadb.asset.search.AssetSearcher;
import org.openedit.Data;
import org.openedit.WebPageRequest;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.hittracker.HitTracker;
import org.openedit.hittracker.SearchQuery;

public class DbTest extends BaseEnterMediaTest
{

	public void testMediaTypes() throws Exception
	{
		WebPageRequest req = getFixture().createPageRequest("/store/index.html");
		Searcher searcher = getMediaArchive().getSearcherManager().getSearcher("store", "mediatype");
		
		HitTracker tracker = searcher.getAllHits();
		assertTrue(tracker.size() > 1);
	}

	
	public void testDynamicTable() throws Exception
	{
		getMediaArchive().getAssetSearcher();  //this is a chicken and egg problem
		SearcherManager manager = (SearcherManager)getFixture().getModuleManager().getBean("searcherManager");
		Searcher searcher = manager.getSearcher("store", "product");
//		assertTrue( searcher instanceof BaseDbSearcher	);

		HitTracker all = manager.getList("store", "product");
		assertNotNull(all);		
	}
	
	public void testCategorySave() throws Exception
	{
		
//		fieldDbConnection = (DbConnection)getFixture().getModuleManager().getBean("dbConnection");
//		session.beginTransaction();
		
		CategoryArchive archive = getMediaArchive().getCategoryArchive();

		//Category root = archive.getRootCategory();

		//If we close our session then does that mean the objects are not shared?
		Category index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		index = archive.getCategory("index");
		Category index2 = archive.getCategory("index");
		assertEquals("Simple search did not work",index, index2);
		//assertEquals(index, root);

		Category index3 = archive.getCategory("index");
		assertEquals("New session cache did not work",index2.hashCode(), index3.hashCode());

		Category child = archive.getCategory("child1");
		if( child == null)
		{
			child = archive.getRootCategory().addChild(new BaseCategory("child1","New Child"));
			archive.saveCategory(child);
			
		}
		
		assertTrue( index.hasChildren());

		/*
		archive.addChild(new BaseCategory("child" + index.getChildren().size(),"New Child"));
		assertTrue( index.getChildren().size() > 0 );		
		archive.setRootCategory(null);
		Category root = archive.getRootCategory();
		child = (Category)root.getChildren().iterator().next();
		assertNotNull(child.getParentCategory());
		*/
	}


	public void testSearchProducts() throws Exception
	{
		//ProductDbSearcher searcher = (ProductDbSearcher)getFixture().getModuleManager().getBean("productSearcher");
		AssetSearcher searcher  = getMediaArchive().getAssetSearcher();

		SearchQuery q = searcher.createSearchQuery();
		PropertyDetail detail = new PropertyDetail();
		detail.setId("name");
		q.addMatches(detail,"test*");
		HitTracker tracker = searcher.search(q);
		
		assertNotNull(tracker);
		assertTrue(tracker.size()> 0);
		
//		q = searcher.createSearchQuery();
//		q.addMatches("productcategory.categoryid","index","Index categories");
		
		//HitTracker tracker = searcher.search("from Asset prod where prod.categories.id = 'index'");//"from Asset where categories = 'index'", null);
		//HitTracker tracker = searcher.search("from Asset as prod left join prod.id = '1234'");
		//HitTracker tracker = searcher.search("from Asset as prod where prod.categories = '1234'");

		//This works. But is slow since it loads up each products Inventory one at a time
		
		//We need to use create a product HitTracker that only has a few of the fields filed in based on the index thing

		//Or we can find out how to preload the Inventory and Categories
//		List list1 = fieldDbConnection.getCurrentSession("store").createQuery("select product from Asset as product join product.categories as cat where cat.id='index'").list();
//		assertTrue("Sql did not work",list1.size() == 1);
				
//		String sql = "select  *  from products prod join productcategory cat on prod.id=cat.productid where cat.categoryid='child1'";		
//		List list = fieldDbConnection.getCurrentSession("store").createSQLQuery(sql).addEntity(Product.class).list();
//		assertTrue("Sql did not work",list.size() == 1);
//		Asset p = (Product)list.get(0);
		
		q = searcher.createSearchQuery();
		
		detail = new PropertyDetail();
		detail.setId("category");
		q.addMatches(detail,"indexXXXX");
		tracker = searcher.search(q);
		assertTrue("Should be blank",tracker.getTotal()== 0);
//		
//		//from Cat as cat     left join cat.kittens as kitten        with kitten.bodyWeight > 10.0
		q = searcher.createSearchQuery();
		
		detail = new PropertyDetail();
		detail.setId("category");
		q.addMatches(detail,"index");
		tracker = searcher.search(q);
		assertTrue("Should be stuff in it",tracker.getTotal()>0);
//		
//		
//		assertNotNull(tracker);
//		assertTrue(tracker.getTotal()> 0);
		
	}

	public void testCompany() throws Exception
	{
		Searcher searcher = getMediaArchive().getSearcherManager().getSearcher("store", "company");
		
		WebPageRequest req = getFixture().createPageRequest("/store/index.html");
		
		BaseData data = new BaseData();
		//data.setId("123");
		data.setProperty("name", "Test");
		searcher.saveData(data, req.getUser());
		assertNotNull(data.getId());
	}


	public void testReviewer() throws Exception
	{
		Searcher searcher = getMediaArchive().getSearcherManager().getSearcher("store", "reviewers");
		
		WebPageRequest req = getFixture().createPageRequest("/store/index.html");
		
/*		
		<property id="departments" editable="true" list="true">Department</property>
		<property id="contact" editable="true">Contact</property>
		<property id="datesent"  type="date"  format="MM/dd/yyyy" editable="true">Date Sent</property>
		<property id="datereviewed" type="date"  format="MM/dd/yyyy" editable="true">Date Reviewed</property>
		<property id="reviewstatus" editable="true" list="true" >Status</property>
  		<property id="productid" type="id" editable="true">Asset ID</property>
*/
		Data data = searcher.createNewData();
		data.setId("1234");
		data.setProperty("departments", "1");
		data.setProperty("contact", "John Smith");
		data.setProperty("datesent", "12/01/2008");
		data.setProperty("datereviewed", "12/01/2008");
		data.setProperty("reviewstatus", "1");
		data.setProperty("productid", "1234");
		searcher.saveData(data, req.getUser());
		HitTracker tracker = searcher.fieldSearch("productid","1234");
		assertTrue(tracker.size() > 0);
		
		Searcher productsearcher = getMediaArchive().getSearcherManager().getSearcher("store", "product");
		//get me all the products with a review status of 1
		Collection all = productsearcher.fieldSearch("reviewers.reviewstatus", "1");
		assertTrue( all.size() >  0);
		
		
		
	}

	

}
