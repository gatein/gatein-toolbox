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


  abstract long getCountValue(String kind, int index);

  abstract Set<String> getKinds();

  abstract  void incrementCount(String kind, int index);

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
      incrementCount(kind, acc);
    }
  }

  abstract void clear();

  StringBuilder report(StringBuilder report) {
    for (String kind : getKinds())
    {
      String[] pkgs = config.getPkgs();
      for (int i = 0; i < pkgs.length; i++) {
        String pkg = pkgs[i];
        long l = getCountValue(kind, i);
        if (l > 0) {
          report.append(kind).append("/").append(pkg).append(": ").append(l).append("\n");
        }
      }
      long uncaught = getCountValue(kind, -1);
      if (uncaught > 0) {
        report.append(kind).append("/*: ").append(uncaught);
      }
    }
    return report;
  }
}
