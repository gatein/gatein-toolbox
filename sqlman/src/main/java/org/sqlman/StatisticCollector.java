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

import java.util.Set;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class StatisticCollector {

  /** . */
  protected final Configuration config;

  StatisticCollector(Configuration config) {
    this.config = config;
  }


  abstract Statistic getStatistic(String kind, int index, boolean create);

  abstract Set<String> getKinds();

  void log(String kind, long millis) {
    if (kind != null)
    {
      int acc = -1;
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      for (StackTraceElement frame : stack) {
        String className = frame.getClassName();
        Matcher matcher = config.getPattern().matcher(className);
        if (matcher.matches()) {
          for (int i = 0; i < matcher.groupCount(); i++) {
            String group = matcher.group(1 + i);
            if (group != null) {
              acc = i;
            }
          }
          break;
        }
      }

      //
      getStatistic(kind, acc, true).log(millis);
    }
  }

  abstract void clear();

  StringBuilder report(StringBuilder report) {
    for (String kind : getKinds())
    {
      String[] pkgs = config.getPkgs();
      for (int i = 0; i < pkgs.length; i++) {
        String pkg = pkgs[i];
        Statistic l = getStatistic(kind, i, false);
        if (l != null && l.getSize() > 0) {
          report.append(kind).append("/").append(pkg).append(": ");
          l.report(report);
          report.append("\n");
        }
      }
      Statistic uncaught = getStatistic(kind, -1, false);
      if (uncaught != null && uncaught.getSize() > 0) {
        report.append(kind).append("/*").append(": ");
        uncaught.report(report);
        report.append("\n");
      }
    }
    return report;
  }
}
