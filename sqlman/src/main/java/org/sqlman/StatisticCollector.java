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

  void log(String kind) {
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
      getStatistic(kind, acc, true).log();
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
        if (l != null) {
          report.append(kind).append("/").append(pkg).append(": ").append(l.getCount()).append("\n");
        }
      }
      Statistic uncaught = getStatistic(kind, -1, false);
      if (uncaught != null) {
        report.append(kind).append("/*: ").append(uncaught.getCount()).append("\n");
      }
    }
    return report;
  }
}
