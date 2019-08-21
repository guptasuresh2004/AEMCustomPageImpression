package com.mli.neo.core.utils.page.statistics;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
/**
 * Custom Impression Provider Service
 * @author TTN
 *
 */
public interface CustomImpressionProvider {
  /**
   * Get Iterator of all popular resource
   * @param root_path
   * @param isDeep
   * @param num_days
   * @return {@link Iterator<Resource>}
   */
  public Iterator<Resource> getPopularResource(String root_path, boolean isDeep, int num_days);
  /**
   * Get Page impression count based on page path
   * @param page_path
   * @param num_days
   * @return
   */
  public int getPageImpressionCount(String page_path,int num_days);
  /**
   * Get most popular Resource based on root path.
   * @param root_path
   * @param num_days
   * @return {@link Resource} 
   */
  public Resource getMostPopularResource(String root_path,int num_days);
  /**
   * return set of all popular resources sorted by there impression
   * @param root_path
   * @param isDeep
   * @param num_days
   * @param total_count
   * @return
   */
  public Set<Resource> getPopularResource(String root_path,boolean isDeep,int num_days, int total_count);
  /**
   * Get Json Output of all popular resource under a path
   * Json Output give page path and impression count for all resource under root path sorted by impression count
   * @param httpServletRequest
   * @param root_path
   * @param num_days
   * @return
   */
  public String getJsonForPopularString(SlingHttpServletRequest httpServletRequest, String root_path,int num_days);
  /**
   * Get Iterator of all popular resource
   * @param root_path
   * @param isDeep
   * @param num_days
   * @return {@link Iterator<Resource>}
   */
  public List<String> getPopularResourceList(String root_path, boolean isDeep, int num_days);
}