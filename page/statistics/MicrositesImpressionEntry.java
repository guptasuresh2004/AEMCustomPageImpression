package com.mli.neo.core.utils.page.statistics;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.crx.statistics.Entry;
import com.day.crx.statistics.PathBuilder;
/**
 * Custom Impression Entry
 * @authorTTN
 *
 */
public class MicrositesImpressionEntry extends Entry {
  /** default log */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** Name of the property that contains the view count */
  public static final String VIEWS = "views";

  /** Name of the property that contains the rolling week count */
  public static final String ROLLING_WEEK_COUNT = "rollingWeekViews";

  /** Name of the property that contains the rolling month count */
  public static final String ROLLING_MONTH_COUNT = "rollingMonthViews";

  /** The page */
  private final String pagePath;

  private final long count;

  public MicrositesImpressionEntry(String pathPrefix, String pagePath,
      String date, long count) {
    super(pathPrefix);
    this.pagePath = pagePath;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    try {
      this.setTimestamp(format.parse(date).getTime());
    } catch (ParseException e) {
      log.error("error while parsing date for impressionsentry", e);
    }
    this.count = count;
  }

  @Override
  protected PathBuilder getPathBuilder() {
    return new ImpressionsPathBuilder(pagePath);
  }

  @Override
  public void write(Node node) throws RepositoryException {
    log.info("writing impressions node " + node.getPath());

    // If Node alredy have count property and it is increment by 1 then
    // increment view by 1
    if (this.count == 1) {
      if (node.hasProperty(VIEWS)) {
        long currentCount = node.getProperty(VIEWS).getLong();
        node.setProperty(VIEWS, currentCount + 1);
      }else {
          node.setProperty(VIEWS, count);
      }
      
    } else {
      node.setProperty(VIEWS, count);
    }

    // set month value
    Node month = node.getParent();
    NodeIterator dayIter = month.getNodes();
    long monthCount = 0;
    while (dayIter.hasNext()) {
      Node tmp = dayIter.nextNode();
      if (tmp.hasProperty(VIEWS)) {
        monthCount += tmp.getProperty(VIEWS).getLong();

      }
    }
    month.setProperty(VIEWS, monthCount);

    // set year value
    Node year = month.getParent();
    NodeIterator monthIter = year.getNodes();
    long yearCount = 0;
    while (monthIter.hasNext()) {
      Node tmp = monthIter.nextNode();
      if (tmp.hasProperty(VIEWS)) {
        yearCount += tmp.getProperty(VIEWS).getLong();

      }
    }
    year.setProperty(VIEWS, yearCount);

    // set cumulative values for week and month
    node.setProperty(ROLLING_WEEK_COUNT, getCumulativeCount(node, 7, VIEWS));
    node.setProperty(ROLLING_MONTH_COUNT, getCumulativeCount(node, 30, VIEWS));
  }

  /**
   * Calculates the cumulative view count on the <code>node</code>.
   * 
   * @param node
   *          the node where to update the cumulative view count
   * @param numDays
   *          the number of days back in time that are cumulated
   * @param propertyName
   *          the name of the count property
   * @throws RepositoryException
   *           if an error occurs while reading or updating.
   */
  private long getCumulativeCount(Node node, int numDays, String propName)
      throws RepositoryException, ValueFormatException {
    long viewCount = 0;
    Session session = node.getSession();
    PathBuilder builder = getPathBuilder();
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(getTimestamp());
    MicrositesImpressionEntry entry = new MicrositesImpressionEntry(
        getPathPrefix(), pagePath, "1970-01-01", 0);
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < numDays; i++) {
      // re-use buffer
      buffer.setLength(0);

      entry.setTimestamp(date.getTimeInMillis());
      builder.formatPath(entry, buffer);
      String path = buffer.toString();
      try {
        Item item = session.getItem(path);
        if (item.isNode()) {
          Node n = (Node) item;
          if (n.hasProperty(propName)) {
            viewCount += n.getProperty(propName).getLong();
          }
        }
      } catch (PathNotFoundException e) {
        // no statistics found for that day
      }

      // go back one day
      date.add(Calendar.DAY_OF_MONTH, -1);
    }
    return viewCount;
  }
}