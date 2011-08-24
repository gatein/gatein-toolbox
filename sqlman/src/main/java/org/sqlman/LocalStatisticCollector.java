/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
  private final Map<String, Map<Integer, Statistic>> state;

  /** . */
  private final ConcurrentStatisticCollector parent;

  /** . */
  private final AtomicLong beginMillis;

  /** . */
  private final AtomicLong timeMillis;

  LocalStatisticCollector(ConcurrentStatisticCollector parent, Configuration config) {
    super(config);

    //
    this.parent = parent;
    this.depth = 0;
    this.state = new HashMap<String, Map<Integer, Statistic>>();
    this.beginMillis = new AtomicLong();
    this.timeMillis = new AtomicLong();
  }

  private Map<Integer, Statistic> safeGetMap(String kind)
  {
    Map<Integer, Statistic> tmp = state.get(kind);
    if (tmp == null)
    {
      tmp = config.buildMap();
      state.put(kind, tmp);
    }
    return tmp;
  }

  Statistic getStatistic(String kind, int index, boolean create) {
    if (!create) {
      Map<Integer, Statistic> tmp = state.get(kind);
      if (tmp != null) {
        return tmp.get(index);
      } else {
        return null;
      }
    } else {
      return safeGetMap(kind).get(index);
    }
  }

  @Override
  void log(String kind, long millis) {
    super.log(kind, millis);
    timeMillis.addAndGet(millis);
  }

  Set<String> getKinds() {
    return state.keySet();
  }

  @Override
  void clear() {
    timeMillis.set(0);
    beginMillis.set(0);
    state.clear();
  }

  public int getDepth() {
    return depth;
  }

  void begin(Object context)
  {
    if (depth++ == 0) {
      this.beginMillis.set(System.currentTimeMillis());
      this.context = context;
    }
  }

  void end()
  {
    if (--depth == 0)
    {
      if (state.size() > 0) {
        long totalMillis = System.currentTimeMillis() - beginMillis.get();
        parent.merge(this);
        StringBuilder report = new StringBuilder("-= SQLMan request report =-\n");
        if (context != null) {
          // Compute inherent time
          long inherentMillis = totalMillis - timeMillis.get();
          report.append("request: ").append(context).
            append(" time=").append(totalMillis).
            append(" inherent=").append(inherentMillis).append("\n");
        }
        report(report);
        System.out.println(report);
        clear();
      }
      context = null;
    }
  }
}
