package com.mli.neo.core.utils.page.statistics;

import com.day.crx.statistics.Entry;
import com.day.crx.statistics.PathBuilder;
/**
 * Custom Impression Path Builder
 * @author TTN
 *
 */
public class ImpressionsPathBuilder extends PathBuilder {
  /** The name of the node that contains the statistical data about a page */
  public static final String STATS_NAME = ".stats";

  /** The path of the page. */
  private final String path;

  /** Default constructor */
  public ImpressionsPathBuilder(String path) {
    super("yyyy/MM/dd");
    this.path = path;
  }

  /**
   * Formats the path for a {@link ImpressionsEntry} instance.
   * 
   * @param entry
   *          a {@link ImpressionsEntry} instance
   * @param buffer
   *          where to write the path to
   */
  public void formatPath(Entry entry, StringBuffer buffer) {
    MicrositesImpressionEntry pv = (MicrositesImpressionEntry) entry;
    buffer.append(pv.getPathPrefix());
    buffer.append(path);
    buffer.append("/").append(STATS_NAME).append("/");

    // add date nodes as specified in constructor pattern
    super.formatPath(pv, buffer);
  }
}