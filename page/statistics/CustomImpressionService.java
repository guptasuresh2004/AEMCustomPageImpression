package com.mli.neo.core.utils.page.statistics;


import java.util.Date;

import org.apache.sling.api.resource.Resource;
/**
 * Custom Page Impression Service
 * @author TTN
 *
 */
public interface CustomImpressionService {
  /**
   * Record Impression for a path given a date
   * If this method is called multiple time for sam date then value will get overridden
   * Date should always be in form of yyyy-MM-DD
   * @param resourcePath
   * @param date (In form of yyyy-MM-DD)
   * @param count
   */
  public void recordImpression(String resourcePath, String date, long count);
  /**
   * Record Impression for a path given a date
   * If this method is called multiple time for sam date then value will get overridden
   * Date should always be in form of yyyy-MM-DD
   * @param resource
   * @param date (In form of yyyy-MM-DD)
   * @param count
   */
  public void recordImpression(Resource resource, String date, long count);
  /**
   * Record Impression for a path given a date
   * If this method is called multiple time for sam date then value will get overridden
   * @param resource
   * @param date
   * @param count
   */
  public void recordImpression(Resource resource, Date date, long count);
  /**
   * Record Impression for a path given a date
   * Calling this method for same day will increase count of impression by 1
   * @param resource
   * @param date
   */
  public void recordImpression(Resource resource, Date date);
  /**
   * Record Impression for a path given a date
   * Calling this method for same day will increase count of impression by 1
   * Date should be in form of yyyy-MM-DD
   * @param resourcePath
   * @param date (in form of yyyy-MM-DD)
   */
  public void recordImpression(String resourcePath, String date);
  /**
   * Method that will return formated date for impression 
   * @param date
   * @return formatted date in form of yyyy-MM-DD
   */
  public String getFormattedDateForImpression(Date date);
}