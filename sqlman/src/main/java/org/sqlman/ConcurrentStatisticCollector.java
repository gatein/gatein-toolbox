package org.sqlman;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ConcurrentStatisticCollector extends StatisticCollector {

  /** . */
  private final ConcurrentMap<String, ConcurrentMap<Integer, Statistic>> state;

  public ConcurrentStatisticCollector(Configuration config) {
    super(config);

    //
    this.state = new ConcurrentHashMap<String, ConcurrentMap<Integer, Statistic>>();
  }

  private ConcurrentMap<Integer, Statistic> safeGetMap(String kind)
  {
    ConcurrentMap<Integer, Statistic> tmp = state.get(kind);
    if (tmp == null)
    {
      tmp = config.buildMap();
      ConcurrentMap<Integer, Statistic> phantom = state.putIfAbsent(kind, tmp);
      if (phantom != null)
      {
        tmp = phantom;
      }
    }
    return tmp;
  }

  Statistic getStatistic(String kind, int index, boolean create) {
    if (!create) {
      ConcurrentMap<Integer, Statistic> tmp = state.get(kind);
      if (tmp != null)
      {
        return tmp.get(index);
      }
      return null;
    } else {
      return safeGetMap(kind).get(index);
    }
  }

  Set<String> getKinds() {
    return state.keySet();
  }

  void merge(StatisticCollector collector) {
    for (String kind : collector.getKinds()) {
      Map<Integer, Statistic> foo = safeGetMap(kind);
      String[] pkgs = config.getPkgs();
      for (int i = 0; i < pkgs.length; i++) {
        Statistic value = collector.getStatistic(kind, i, false);
        if (value != null) {
          foo.get(i).merge(value);
        }
      }
    }
  }

  @Override
  void clear() {
    state.clear();
  }
}
