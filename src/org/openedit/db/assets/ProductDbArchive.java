package org.openedit.db.assets;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.data.BaseArchive;
import org.openedit.data.PropertyDetails;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.db.BaseDbSearcher;
import org.openedit.db.util.DbConnection;
import org.openedit.entermedia.Category;

import com.openedit.OpenEditRuntimeException;
import com.openedit.hittracker.HitTracker;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;
import com.openedit.util.IntCounter;

public class ProductDbArchive extends BaseArchive implements ProductArchive
{
	private static final Log log = LogFactory.getLog(CategoryDbArchive.class);
	protected IntCounter fieldIntCounter;
	protected DbConnection fieldDbConnection;
	protected SearcherManager fieldSearcherManager;
	protected PageManager fieldPageManager;
	
	public Searcher getRelatedSearcher(String inSearchType)
	{
		Searcher searcher = getSearcherManager().getSearcher(getCatalogId(), inSearchType);  //productcategory
		if( searcher instanceof BaseDbSearcher)
		{
			BaseDbSearcher dbsearcher = (BaseDbSearcher)searcher;
			if( dbsearcher.getDataMapper().getPropertyDetails().getDetail("productid") == null )
			{
				throw new OpenEditRuntimeException(inSearchType + "properties.xml must contain productid column");
			}
		}
		return searcher;
	}
	
	protected AssetDbSearcher getProductDbSearcher()
	{
		AssetDbSearcher searcher = (AssetDbSearcher)getStore().getProductSearcher();
		return searcher;
	}

	public void deleteProduct(Asset inProduct) throws OpenEditException
	{
		//getDbConnection().delete(inProduct);
	}
	public Asset getProduct(String inId) throws OpenEditException
	{
		Asset product = (Product)getProductDbSearcher().searchById(inId);
		return product;
	}
	public ProductPathFinder getProductPathFinder()
	{
		return getStore().getProductPathFinder();
	}
	public PropertyDetails getPropertyDetails() 
	{
		return getStore().getFieldArchive().getPropertyDetails("product");
	}
	public Store getStore()
	{
		// TODO Auto-generated method stub
		return fieldStore;
	}
	public List listAllProductIds()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String loadDescription(Asset inProduct) throws OpenEditException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String nextProductNumber() throws OpenEditException
	{
		String countString = String.valueOf(getIntCounter().incrementCount());
		return countString;
	}
	public void saveBlankProductDescription(Asset inProduct) throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}
	public void saveProduct(Asset inProduct) throws OpenEditException
	{
		saveProduct( inProduct,null);
	}	
	public void saveProduct(Asset inProduct, User inUser) throws OpenEditException
	{
		if( inProduct.getId() == null)
		{
			int id = getIntCounter().incrementCount();
			inProduct.setId(String.valueOf( id ) );
		}
		getProductDbSearcher().saveData(inProduct, inUser);
		//getDbConnection().saveData(inProduct,getDataMapper(),inUser);	

		saveCategories(inProduct, inUser);
		saveRelatedProducts(inProduct, inUser);
		
	}


	protected void saveCategories(Asset inProduct, User inUser)
	{
		Searcher productcategory = getRelatedSearcher("productcategory");
		//deal with category join data here
		HitTracker tracker = productcategory.fieldSearch("productid", inProduct.getId());
		//find out what is removed and added
		List toadd = new ArrayList(inProduct.getCategories().size());
		for (Iterator iterator = inProduct.getCategories().iterator(); iterator.hasNext();)
		{
			Category cat = (Category) iterator.next();
			toadd.add( cat.getId());
		}
		List toremove = new ArrayList(tracker.size());
		for (Iterator iterator = tracker.iterator(); iterator.hasNext();)
		{
			Object hit = (Object) iterator.next();
			String existingcatid = tracker.getValue(hit, "categoryid");
			if( !toadd.remove(existingcatid) )
			{
				toremove.add(hit);
			}
		}
		//Add them back in as needed
		for (Iterator iterator = toremove.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			productcategory.delete(data, inUser);
		}
		for (Iterator iterator = toadd.iterator(); iterator.hasNext();)
		{
			String catid  = (String) iterator.next();
			Data data = productcategory.createNewData();
			data.setProperty("categoryid", catid);
			data.setProperty("productid", inProduct.getId());
			productcategory.saveData(data, inUser);
		}
	}

	public void saveProductDescription(Asset inProduct, String inDescription) throws OpenEditException
	{
		//save to HTML on the file system
	}
	public void setStore(Store inStore)
	{
		fieldStore = inStore;
	}
	public IntCounter getIntCounter()
	{
		if( fieldIntCounter == null)
		{
			fieldIntCounter = new IntCounter();
			fieldIntCounter.setLabelName("productIdCount");
			File file = new File(getStore().getStoreDirectory(), "configuration/product.properties");
			fieldIntCounter.setCounterFile(file);
		}
		return fieldIntCounter;
	}
	public Asset getProductBySourcePath(String inSourcePath) throws OpenEditException
	{
		//search by source path
		HitTracker tracker = getStore().getProductSearcher().fieldSearch("sourcepath",inSourcePath);
		if( tracker.size() > 0)
		{
			return (Product)tracker.get(0);
		}
		return null;
	}
	public String buildXconfPath(Asset inProduct)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public DbConnection getDbConnection()
	{
		return fieldDbConnection;
	}
	public void setDbConnection(DbConnection inDbConnection)
	{
		fieldDbConnection = inDbConnection;
	}

	public void clearProduct(Asset inProduct)
	{
		// TODO Auto-generated method stub
		
	}
	public void clearProducts()
	{
		// TODO Auto-generated method stub
		
	}

	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public PageManager getPageManager()
	{
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager)
	{
		fieldPageManager = inPageManager;
	}

	public void createTables()
	{
		getRelatedSearcher("productcategory");
		getRelatedSearcher("productrelated");
	}
	protected void loadCategories(Asset inData)
	{
		//need to tell the product about the categories
		
	}
	protected void loadRelatedProducts(Asset inProduct)
	{
		Searcher productrelated = getSearcherManager().getSearcher(getCatalogId(), "productrelated");
		//deal with related product table join data here
		HitTracker tracker = productrelated.fieldSearch("productid", inProduct.getId());
		inProduct.setRelatedProducts(tracker);
		//Add them back in
		//maybe the tracker should create the types for me? Yup
				
	}
	protected void saveRelatedProducts(Asset inProduct, User inUser)
	{
		Searcher productrelated = getRelatedSearcher("productrelated");
		//deal with category join data here
		HitTracker tracker = productrelated.fieldSearch("productid", inProduct.getId());
		//find out what is removed and added
		Map toadd = new HashMap(inProduct.getRelatedProducts().size());
		for (Iterator iterator = inProduct.getRelatedProducts().iterator(); iterator.hasNext();)
		{
			RelatedAsset related = (RelatedProduct) iterator.next();
			toadd.put(related.getProductId(), related);
		}
		//COMPARE the old values that may need to be deleted
		List toremove = new ArrayList(tracker.size());
		for (Iterator iterator = tracker.iterator(); iterator.hasNext();)
		{
			Object hit = (Object) iterator.next();
			String existingproductid = tracker.getValue(hit, "productid");
			if( !toadd.containsKey(existingproductid) )
			{
				toremove.add(hit);
			}
		}
		//Add the new ones
		for (Iterator iterator = toremove.iterator(); iterator.hasNext();)
		{
			Data data = (Data) iterator.next();
			productrelated.delete(data, inUser);
		}
		for (Iterator iterator = toadd.keySet().iterator(); iterator.hasNext();)
		{
			String productid  = (String) iterator.next();
			RelatedAsset related = (RelatedProduct)toadd.get(productid);
			productrelated.saveData(related, inUser);
		}
	}

}
