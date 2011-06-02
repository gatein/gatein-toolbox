package org.sqlman;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConcurrentStatisticCollector extends StatisticCollector {

  /** . */
  private final ConcurrentMap<String, ConcurrentMap<Integer, AtomicLong>> state;

  public ConcurrentStatisticCollector(Configuration config) {
    super(config);

    //
    this.state = new ConcurrentHashMap<String, ConcurrentMap<Integer, AtomicLong>>();
  }

  ConcurrentMap<Integer, AtomicLong> getMap(String kind)
  {
    ConcurrentMap<Integer, AtomicLong> tmp = state.get(kind);
    if (tmp == null)
    {
      tmp = config.buildMap();
      ConcurrentMap<Integer, AtomicLong> phantom = state.putIfAbsent(kind, tmp);
      if (phantom != null)
      {
        tmp = phantom;
      }
    }
    return tmp;
  }

  long getCountValue(String kind, int index) {
    ConcurrentMap<Integer, AtomicLong> tmp = state.get(kind);
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

  void incrementCount(String kind, int index) {
    Map<Integer, AtomicLong> foo = getMap(kind);
    foo.get(index).incrementAndGet();
  }

  void merge(StatisticCollector collector) {
    for (String kind : collector.getKinds()) {
      Map<Integer, AtomicLong> foo = getMap(kind);
      String[] pkgs = config.getPkgs();
      for (int i = 0; i < pkgs.length; i++) {
        long value = collector.getCountValue(kind, i);
        if (value > 0) {
          foo.get(i).addAndGet(value);
        }
      }
    }
  }

  @Override
  void clear() {
    state.clear();
  }
}
