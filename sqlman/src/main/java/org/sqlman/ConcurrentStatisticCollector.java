/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
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
      if (tmp != null) {
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
