package com.mli.neo.core.utils.page.statistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONStringer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.statistics.StatisticsService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageFilter;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.core.stats.PageViewReport;
import com.mli.neo.core.services.ResourceResolverService;

/**
 * Custom Impression Provider implemetation
 * 
 * @author TTN
 *
 */

@SuppressWarnings("deprecation")
@Component(service = CustomImpressionProvider.class, immediate = true)
public class CustomImpressionProviderImpl implements CustomImpressionProvider {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected static final int MAX_RESULT_COUNT = 3000;

	@Reference
	protected StatisticsService statisticsService;

	@Reference
	protected ResourceResolverService resourceResolverService;

	protected ResourceResolver resourceResolver;

	protected String stat_path;

	/**
	 * Get all popular resources. We use admin session for now to get popular
	 * result some where so that we don't have to run this all the time.
	 */
	@Override
	public Iterator<Resource> getPopularResource(final String root_path, final boolean isDeep, final int num_days) {
		Iterator<Resource> popular_resource_iterator = null;
		try {
			resourceResolver = resourceResolverService.getReadResourceResolver();
			Map<String, Integer> sorted_map = getpoularResourceMap(resourceResolver, root_path, isDeep, num_days);
			Set<Resource> all_popular_resource_set = new HashSet<Resource>();
			for (String each_popular_resource : sorted_map.keySet()) {
				Resource each_popular_resource_object = resourceResolver.resolve(each_popular_resource);
				if (!(each_popular_resource_object instanceof NonExistingResource)) {
					all_popular_resource_set.add(each_popular_resource_object);
				}
			}
			popular_resource_iterator = all_popular_resource_set.iterator();

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (null != resourceResolver && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}

		return popular_resource_iterator;
	}
	/**
	 * Get all popular resources. We use admin session for now to get popular
	 * result some where so that we don't have to run this all the time.
	 */
	@Override
	public List<String> getPopularResourceList(final String root_path, final boolean isDeep, final int num_days) {
		List<String> popular_resource_iterator = null;
		try {
			resourceResolver = resourceResolverService.getReadResourceResolver();
			Map<String, Integer> sorted_map = getpoularResourceMap(resourceResolver, root_path, isDeep, num_days);
			List<String> all_popular_resource_list = new LinkedList<String>();
			for (String each_popular_resource : sorted_map.keySet()) {
				Resource each_popular_resource_object = resourceResolver.resolve(each_popular_resource);
				if (!(each_popular_resource_object instanceof NonExistingResource)) {
					all_popular_resource_list.add(each_popular_resource_object.getPath());
				}
			}
			popular_resource_iterator = all_popular_resource_list;

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (null != resourceResolver && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}

		return popular_resource_iterator;
	}

	/**
	 * Method to get page impression count We use admin session here as well to get
	 * count
	 */

	@Override
	public int getPageImpressionCount(final String page_path, final int num_days) {
		int total_count = 0;
		try {
			resourceResolver = resourceResolverService.getReadResourceResolver();
			total_count = getPageImpressionCount(resourceResolver, page_path, num_days);
		} catch (Exception e) {
			log.error(e.getMessage());

		} finally {
			if (this.resourceResolver != null && resourceResolver.isLive()) {
				resourceResolver.close();
			}
		}
		return total_count;
	}

	/**
	 * Method to get most popular resource
	 */

	@Override
	public Resource getMostPopularResource(final String root_path, final int num_days) {
		Iterator<Resource> most_popular_resource = getPopularResource(root_path, true, num_days);
		if (most_popular_resource != null) {
			while (most_popular_resource.hasNext()) {
				return most_popular_resource.next();
			}
		}
		return null;
	}

	/**
	 * get popular resource based on total count
	 */
	@Override
	public Set<Resource> getPopularResource(final String root_path, final boolean isDeep, final int num_days,
			final int total_count) {
		Iterator<Resource> popular_resources = getPopularResource(root_path, isDeep, num_days);
		Set<Resource> popular_resource_set = null;
		if (popular_resources != null) {
			int temp_count = 0;
			popular_resource_set = new HashSet<Resource>();
			while (popular_resources.hasNext()) {
				// If result is more than total count then break
				if (temp_count > total_count)
					break;
				popular_resource_set.add(popular_resources.next());
				temp_count++;
			}
		}
		return popular_resource_set;
	}

	/**
	 * Utility method to get page impression using resource resolver
	 * 
	 * @param resourceResolver
	 * @param page_path
	 * @param num_days
	 * @return
	 * @throws RepositoryException
	 */
	protected int getPageImpressionCount(ResourceResolver resourceResolver, String page_path, int num_days)
			throws RepositoryException {
		if (null == resourceResolver || StringUtils.isBlank(page_path)) {
			return 0;
		}
		Page page = resourceResolver.resolve(page_path).adaptTo(Page.class);
		stat_path = statisticsService.getPath() + "/pages";
		// use Page view class
		PageViewReport pageViewReport = new PageViewReport(stat_path, page, WCMMode.DISABLED);
		pageViewReport.setPeriod(30);
		// this is were report is ran
		Iterator stats = statisticsService.runReport(pageViewReport);
		int totalPageViews = 0;
		while (stats.hasNext()) {
			Object[] res = (Object[]) stats.next();
			totalPageViews = totalPageViews + Integer.parseInt(res[1].toString());
		}
		log.debug("Total page view for path " + page_path + "  is " + totalPageViews);
		return totalPageViews;
	}

	/**
	 * Get Json string using JsonStringer
	 */
	@Override
	public String getJsonForPopularString(SlingHttpServletRequest httpServletRequest, String root_path, int num_days) {
		JSONStringer jsonStringer = new JSONStringer();
		log.debug("Root path is " + root_path);
		jsonStringer.setTidy(true);
		try {
			jsonStringer.array();
			jsonStringer.object().key("rootpath").value(root_path);
			jsonStringer.key("num_days").value(num_days).endObject();
			Map<String, Integer> all_popular_resource = getpoularResourceMap(httpServletRequest.getResourceResolver(),
					root_path, true, num_days);
			log.debug(all_popular_resource.toString());
			for (Entry<String, Integer> each_resource_entry : all_popular_resource.entrySet()) {
				jsonStringer.object();
				jsonStringer.key("path").value(each_resource_entry.getKey());
				jsonStringer.key("impression_count").value(each_resource_entry.getValue());
				jsonStringer.endObject();
			}
			jsonStringer.endArray();
		} catch (JSONException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		return jsonStringer.toString();
	}

	/**
	 * Helper method to get sorted map for popular resources
	 * 
	 * @param resourceResolver
	 * @param root_path
	 * @param isDeep
	 * @param num_days
	 * @return
	 */
	protected Map<String, Integer> getpoularResourceMap(final ResourceResolver resourceResolver, final String root_path,
			final boolean isDeep, final int num_days) {
		Map<String, Integer> sorted_map = null;
		Map<String, Integer> all_page_impression = null;
		try {
			PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
			Page page = resourceResolver.resolve(root_path).adaptTo(Page.class);
			if (page == null) {
				log.error("Root path is not present " + root_path);
				return null;
			}
			// get all children including subchildren
			Iterator<Page> all_child_pages = page.listChildren(new PageFilter(), isDeep);
			all_page_impression = new HashMap<String, Integer>();
			int all_result_count = 0;
			while (all_child_pages.hasNext()) {
				// If result is more than MAX allowed then break. This is to make sure that
				// performance is not imppacted.
				if (all_result_count >= MAX_RESULT_COUNT) {
					break;
				}
				Page each_page = all_child_pages.next();
				if (null != each_page) {
					int totalPageViews = getPageImpressionCount(resourceResolver, each_page.getPath(), num_days);
					// Only if page view count is more than 0 consider adding them
					if (totalPageViews > 0) {
						log.debug("Adding " + each_page.getPath() + "  to Map  with value " + totalPageViews);
						all_page_impression.put(each_page.getPath(), totalPageViews);
					}
				}
			}

			// Now create resource
			log.debug("Unsorted Popular Map size is " + all_page_impression.size());
			// Once we have whole map need to sort them based on impression
			ValueComparator valueComparator = new ValueComparator(all_page_impression);
			sorted_map = new TreeMap<String, Integer>(valueComparator);
			sorted_map.putAll(all_page_impression);
			log.debug("Soted Popular Map size is " + sorted_map.size());
		} catch (RepositoryException e) {
			log.error(e.getMessage());
		}
		return sorted_map;
	}

}

/**
 * Helper class to sort map based on impression count
 * 
 * @author Yogesh Upadhyay
 *
 */

class ValueComparator implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with equals.
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}