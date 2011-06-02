package org.sqlman;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class LocalStatisticCollector extends StatisticCollector {

  /** . */
  private Object context;

  /** . */
  private int depth;

  /** . */
  private final Map<String, Map<Integer, AtomicLong>> state;

  /** . */
  private final ConcurrentStatisticCollector parent;

  LocalStatisticCollector(ConcurrentStatisticCollector parent, Configuration config) {
    super(config);

    //
    this.parent = parent;
    this.depth = 0;
    this.state = new HashMap<String, Map<Integer, AtomicLong>>();
  }

  Map<Integer, AtomicLong> getMap(String kind)
  {
    Map<Integer, AtomicLong> tmp = state.get(kind);
    if (tmp == null)
    {
      tmp = config.buildMap();
      state.put(kind, tmp);
    }
    return tmp;
  }

  long getCountValue(String kind, int index) {
    Map<Integer, AtomicLong> tmp = state.get(kind);
    if (tmp != null)
    {
      AtomicLong ac = tmp.get(index);
      if (ac != null)
      {
        return ac.get();
      }
    }
    return -1;
  }

  Set<String> getKinds() {
    return state.keySet();
  }

  void incrementCount(String kind, int index)
  {
    Map<Integer, AtomicLong> foo = getMap(kind);
    foo.get(index).incrementAndGet();
  }

  @Override
  void clear() {
    state.clear();
  }

  public int getDepth() {
    return depth;
  }

  void begin(Object context)
  {
    if (depth++ == 0) {
      this.context = context;
    }
  }

  void end()
  {
    if (--depth == 0)
    {
      if (state.size() > 0) {
        parent.merge(this);
        StringBuilder report = new StringBuilder("-= SQLMan request report =-\n");
        if (context != null) {
          report.append("request: ").append(context).append("\n");
        }
        report(report);
        System.out.println(report);
        clear();
      }
      context = null;
    }
  }
}
