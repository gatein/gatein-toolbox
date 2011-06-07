package org.sqlman;

import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Statistic {

  /** . */
  private final AtomicLong count = new AtomicLong();

  public long getCount() {
    return count.get();
  }

  void log() {
    count.incrementAndGet();
  }

  void merge(Statistic that) {
    long v = that.count.get();
    if (v > 0) {
      count.addAndGet(v);
    }
  }
}
