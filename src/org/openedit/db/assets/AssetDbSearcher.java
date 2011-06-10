package org.openedit.db.assets;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.db.BaseDbSearcher;
import org.openedit.db.util.BaseDataBeanCreator;
import org.openedit.db.util.DataMapper;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.Category;
import org.openedit.entermedia.search.SearchFilter;
import org.openedit.links.Link;

import com.openedit.OpenEditException;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;
import com.openedit.hittracker.SearchQuery;
import com.openedit.page.Page;
import com.openedit.users.User;
import com.openedit.util.PathUtilities;

public class AssetDbSearcher extends BaseDbSearcher implements ProductSearcher 
{
	private static final Log log = LogFactory.getLog(AssetDbSearcher.class);
	
	protected static final String CATEGORYID = "categoryid";

//	public HitTracker search(String inQuery, String inOrdering) 
//	{
//		throw new OpenEditRuntimeException("Direct query not supported");
//	}
	
	public DataMapper createMapper()
	{
		DataMapper mapper = super.createMapper();
		mapper.setBeanCreator( new BaseDataBeanCreator()
			{
				public Object newInstance()
				{
					return new Asset();
				}
				public void populateExtraData(Data inData)
				{
					getProductDbArchive().loadCategories((Asset)inData);
					getProductDbArchive().loadRelatedProducts((Asset)inData);
				}
			}
		);

		//make sure the needed tables already exists
		getProductDbArchive().createTables();

		return mapper;
	}


	public void deleteFromIndex(Product inProduct) throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}

	public void deleteFromIndex(String inId) throws OpenEditException
	{
		// TODO Auto-generated method stub
		
	}

	public void fieldSearch(WebPageRequest inPageRequest, Cart inCart) throws OpenEditException
	{
		fieldSearch(inPageRequest);
	}

	public ProductArchive getProductArchive()
	{
		// TODO Auto-generated method stub
		return getStore().getProductArchive();
	}

	public MediaArchive getStore()
	{
		// TODO Auto-generated method stub
		return fieldStore;
	}

	public void searchCatalogs(WebPageRequest inPageRequest, Cart inCart) throws Exception
	{
		SearchQuery search = createSearchQuery();
		String catalogId = inPageRequest.getRequestParameter(CATEGORYID);
		if (catalogId == null)
		{
			Page page = inPageRequest.getPage();
			catalogId = page.get(CATEGORYID);
		}
		if (catalogId == null)
		{
			// get it from the path?
			String path = inPageRequest.getPath();
			
			catalogId = PathUtilities.extractPageName(path);
			if(catalogId.endsWith(".draft"))
			{
				catalogId = catalogId.replace(".draft","");
			}
		}
		// Why the content page? Page page = inPageRequest.getContentPage();

		Category catalog = getStore().getCategory(catalogId);
		search.addMatches("category",catalog.getId());
		if (catalog == null)
		{
			if (inPageRequest.getContentPage() == inPageRequest.getPage())
			{
				String val = inPageRequest.findValue("showmissingcategories");
				if(!Boolean.parseBoolean(val)){
				inPageRequest.redirect(getStore().getStoreHome() + "/search/nosuchcatalog.html");
				}
			}
			log.error("No such catalog " + catalogId);
			return;
		}
		inPageRequest.putPageValue("category", catalog); // @deprecated

		inCart.setLastVisitedCatalog(catalog); // this is prone
		// to error

//			boolean includechildren = false;
//			if (catalog.getParentCatalog() == null) // this is the root level
//			{
//				includechildren = true; // since products dont mark themself in the
//				// index catalog
//			}
		String actualid = catalogId;
		if( catalog.getLinkedToCategoryId() != null)
		{
			actualid = catalog.getLinkedToCategoryId();
		}
		
		boolean selected = true;
		if( catalog.getParentCatalog() == null)
		{
			selected = false; //The top level catalog does not count as a selection
		}
		
		SearchFilter not = getStore().getSearchFilterArchive().getSearchFilter(inPageRequest, false, selected,inCart.getStore().getCatalogId());
		if (not != null)
		{
			search.addQuery("filter", not.toFilter(false));
		}
		Link crumb = getStore().buildLink(catalog, inPageRequest.findValue("url-prefix"));
		inPageRequest.putSessionValue("crumb", crumb);

		String sortBy = catalog.get("sortfield");
		search.setSortBy(sortBy);

		//search for product id's, should join them tho
		//StringBuffer sql = new StringBuffer();
		//sql.append(",productcategory " );

//		if( search.getSortBy() != null)
//		{
//			sql.append("ORDER BY ");
//			sql.append(search.getSortBy() );
//		}
//		HitTracker tracker = getDbConnection().search(sql.toString(), getDataMapper());
//		tracker.setSearchQuery(search);
		cachedSearch(inPageRequest, search);

	}

	public void searchExactCatalogs(WebPageRequest inPageRequest, Cart inCart) throws Exception
	{
	}

	public void searchStore(WebPageRequest inPageRequest, Cart inCart) throws Exception
	{
	}

	public void setStore(MediaArchive inStore)
	{
		fieldMediaArchive = inStore;
	}

	public void updateIndex(Product inProduct) throws OpenEditException
	{
	}

	public void updateIndex(List inProducts, boolean inOptimize) throws OpenEditException
	{
	}
	public void flush()
	{
	}
	public String idToPath(String inProductId)
	{
		return null;
	}
	public String createFilter(WebPageRequest inPageRequest, boolean inSelected)
	{
		return null;
	}

	public void deleteFromIndex(HitTracker inOld)
	{
	}

	public void saveData(Object inData, User inUser)
	{
		getDbConnection().saveData((Data)inData, getDataMapper(), inUser);
		
	}
	protected ProductDbArchive getProductDbArchive()
	{
		return (ProductDbArchive)getStore().getProductArchive();
	}
}
