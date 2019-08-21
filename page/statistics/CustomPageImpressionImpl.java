package com.mli.neo.core.utils.page.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.statistics.StatisticsService;
import com.mli.neo.core.services.ResourceResolverService;
/**
 * 
 * @author TTN
 *
 */
@SuppressWarnings("deprecation")
@Component(service = CustomImpressionService.class, immediate = true)
public class CustomPageImpressionImpl implements CustomImpressionService {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
  
  private static final String STATISTICS_PATH = "/pages";


  @Reference
  private StatisticsService statisticsService;

  @Reference
  private ResourceResolverService resourceResolverService;

  private ResourceResolver resourceResolver;

  private String statisticsPath;

  /**
   * Record Impression Method
   * It essentially create Impression Entry and add through OOTB service
   */
  @Override
  public void recordImpression(String resourcePath, String date, long count) {
    Resource resource;
    ResourceResolver resourceResolver = null;
    try {
      resourceResolver = getAdminResourceResolver();
      resource = resourceResolver.resolve(resourcePath);
      if(resource != null){
    	  MicrositesImpressionEntry customImpressionEntry =  new MicrositesImpressionEntry(statisticsPath, resource.getPath(), date, count);
        statisticsService.addEntry(customImpressionEntry);
        
      }
    } catch (Exception e) {
      log.error("There is some error while recording page impression {}",e.getMessage());
      e.printStackTrace();
    }  finally{
        closeResourceResolver(resourceResolver);
    }


  }

  @Override
  public void recordImpression(Resource resource, String date, long count) {
    if(null!=resource){
      recordImpression(resource.getPath(), date,count);
    }else{
      log.error("Resource Provided is Null ");
    }

  }

  @Override
  public void recordImpression(Resource resource, Date date, long count) {
    recordImpression(resource, getFormattedDateForImpression(date),count);

  }

  @Override
  public void recordImpression(Resource resource, Date date) {
    recordImpression(resource, getFormattedDateForImpression(date),1);

  }

  @Override
  public void recordImpression(String resourcePath, String date) {
    recordImpression(resourcePath, date,1);

  }
  
  private synchronized ResourceResolver getAdminResourceResolver() throws LoginException{
    return resourceResolverService.getWriteResourceResolver();
  }
  
  private synchronized void closeResourceResolver(ResourceResolver resourceResolver){
    if(null!=resourceResolver && resourceResolver.isLive()){
      resourceResolver.close();
    }
  }
  
  public String getFormattedDateForImpression(Date date){
    if(date!=null){
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return simpleDateFormat.format(date);
    }
    return null;
  }

  @Activate
  protected void activate(ComponentContext ctx) {
    statisticsPath = statisticsService.getPath() + STATISTICS_PATH;
  }

  @Deactivate
  protected void deactivate(ComponentContext ctx) {
    if (resourceResolver != null && resourceResolver.isLive()) {
      resourceResolver.close();
    }
  }

}