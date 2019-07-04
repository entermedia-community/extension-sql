package org.openedit.db.assets.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.entermediadb.asset.BaseCategory;
import org.entermediadb.asset.Category;
import org.entermediadb.asset.CategoryArchive;
import org.openedit.OpenEditException;
import org.openedit.data.PropertyDetails;
import org.openedit.db.BaseDbSearcher;
import org.openedit.hittracker.HitTracker;
import org.openedit.page.manage.PageManager;

public class CategoryDbArchive extends BaseDbSearcher implements CategoryArchive
{
	private static final Log log = LogFactory.getLog(CategoryDbArchive.class);
	
	protected Category fieldRootCatalog;
	protected PageManager fieldPageManager;
	protected List fieldImageList;

	public Category cacheCategory(Category inCategory)
	{
		if (inCategory == null)
		{
			return null;
		}
		boolean wasInAlready = getCatalogMap().containsKey(inCategory.getId());

		getCatalogMap().put(inCategory.getId(), inCategory);
		if (!wasInAlready && inCategory.hasChildren()) // we dont want an
														// infinite loop so we
														// check to see if it
														// was in already
		{
			for (Iterator iter = inCategory.getChildren().iterator(); iter.hasNext();)
			{
				Category child = (Category) iter.next();
				// log.info("adding " + child);
				cacheCategory(child);
			}
		}
		return inCategory;
	}
	
	public void clearCategories() throws OpenEditException
	{
		//clear the cache
		setRootCategory(null);
		getCatalogMap().clear();
	}

	public void deleteCategory(Category inCatalog) throws OpenEditException
	{
		//getDbConnection().delete(inCatalog);
	}

	public Category getCategory(String inCategory)
	{
//		Session session = getDbConnection().getCurrentSession(getCatalogId());
//		Category category = (Category)session.get(Category.class, inCatalog);
		getRootCategory();
		return (Category) getCatalogMap().get(inCategory);
	
	}

	public Category getCategoryByName(String inCatalogName) throws OpenEditException
	{
		HitTracker categories = getDbConnection().search("select * from categories where name = '" + inCatalogName + "' limit 1", getDataMapper());

		if( categories.size() > 0 )
		{
			return (Category)categories.get(0);
		}
		return null;
	}

	public PropertyDetails getCategoryDetails() throws OpenEditException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Category getRootCategory() 
	{
		if( fieldRootCatalog == null)
		{
			HitTracker categories = getDbConnection().search("select * from categories", getDataMapper());
			if( categories.size() == 0)
			{
				fieldRootCatalog = new BaseCategory("index","Index");
				saveData(fieldRootCatalog, null);
			}
			else
			{
				//load up the children
				Map all = new HashMap(categories.size());
				for (Iterator iterator = categories.iterator(); iterator.hasNext();)
				{
					Category cat = (Category) iterator.next();
					all.put(cat.getId(),cat);
					if( cat.getParentId() == null)
					{
						fieldRootCatalog = cat;
					}
				}
				for (Iterator iterator = categories.iterator(); iterator.hasNext();)
				{
					Category cat = (Category) iterator.next();
					if(cat.getParentId() != null)
					{
						Category parent = (Category)all.get(cat.getParentId());
						parent.addChild(cat);
						
					}
				}
			}
			cacheCategory(fieldRootCatalog);
		}
		return fieldRootCatalog;
	}

	public List listAllCategories() throws OpenEditException
	{
		List all = new ArrayList();
		addCategories(all, getRootCategory());
		return all;
	}

	/**
	 * @param inAll
	 * @param inRootCatalog
	 */
	private void addCategories(List inAll, Category inRootCatalog)
	{
		// TODO Auto-generated method stub
		inAll.add(inRootCatalog);
		for (Iterator iter = inRootCatalog.getChildren().iterator(); iter.hasNext();)
		{
			Category child = (Category) iter.next();
			addCategories(inAll, child);
		}
	}

	public void reloadCategories() throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}

	public void saveAll() throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}

	public void saveCategory(Category inCategory) throws OpenEditException
	{
		//getConnectionPool().save(Category.class.getCanonicalName(), inCategory);
		cacheCategory(inCategory);
		
	}

	public void setRootCategory(Category inRoot) throws OpenEditException
	{
		fieldRootCatalog = inRoot;
	}


	public Map getCatalogMap() 
	{
		return getDataMapper().getCache();
	}

	public Category addChild(Category inCatalog) throws OpenEditException
	{
		getRootCategory().addChild(inCatalog);
		cacheCategory(inCatalog);
		return inCatalog;
	}

	public Category createCategoryTree(String inPath) throws OpenEditException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Category createNewCategory(String inLabel)
	{
		Category cat = new BaseCategory();
		cat.setName(inLabel);
		return cat;
	}
}
