package org.sqlman;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Statistic {

  /** . */
  private final LongStatistic size = new LongStatistic();

  /** . */
  private final LongStatistic timeMillis = new LongStatistic();

  /** . */
  private final LongStatistic minMillis = new LongStatistic(Long.MAX_VALUE);

  /** . */
  private final LongStatistic maxMillis = new LongStatistic();

  public boolean isEmpty() {
    return size.get() > 0;
  }

  public long getSize() {
    return size.get();
  }

  public long getTimeMillis() {
    return timeMillis.get();
  }

  void log(long millis) {
    size.incrementAndGet();
    if (millis > 0) {
      timeMillis.addAndGet(millis);
      minMillis.setIfLesser(millis);
      maxMillis.setIfGreater(millis);
    }
  }

  void merge(Statistic that) {
    long v1 = that.size.get();
    if (v1 > 0) {
      size.addAndGet(v1);
      timeMillis.addAndGet(that.timeMillis.get());
      minMillis.setIfLesser(that.minMillis.get());
      maxMillis.setIfGreater(that.maxMillis.get());
    }
  }

  void report(StringBuilder sb) {
    sb.append("count=").append(size.get()).
       append(",time=").append(timeMillis.get()).
       append(",min=").append(minMillis.get()).
       append(",max=").append(maxMillis.get());
  }
}
