package org.sqlman;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Configuration {

  /** . */
  private final Pattern pattern;

  /** . */
  private String[] pkgs;

  Configuration(String pkgList) {

    //
    StringBuilder sb = new StringBuilder("^");
    String[] pkgs;
    if (pkgList != null) {
      pkgs = pkgList.split("\b*,\b*");
      for (int i = 0; i < pkgs.length; i++) {
        String pkg = pkgs[i] = pkgs[i].trim();
        if (i > 0) {
          sb.append("|");
        }
        sb.append("(");
        for (int j = 0; j < pkg.length(); j++) {
          char c = pkg.charAt(j);
          switch (c) {
            case '*':
              sb.append(".*");
              break;
            case ')':
            case '(':
            case '.':
            case ',':
            case ':':
            case '?':
            case '-':
            case '+':
            case '{':
            case '}':
            case '[':
            case ']':
            case '>':
            case '=':
            case '^':
            case '$':
            case '\\':
              sb.append("\\");
              sb.append(c);
              break;
            default:
              sb.append(c);
              break;
          }
        }
        if (pkg.length() > 0) {
          sb.append("\\.");
        }
        sb.append("[^\\.]+)");
      }
    }
    else {
      pkgs = new String[0];
    }
    sb.append("$");

    this.pattern = Pattern.compile(sb.toString());
    this.pkgs = pkgs;
  }

  ConcurrentMap<Integer, AtomicLong> buildMap()
  {
    ConcurrentMap<Integer, AtomicLong> map = new ConcurrentHashMap<Integer, AtomicLong>();
    for (int i = 0;i < pkgs.length;i++)
    {
      map.put(i, new AtomicLong());
    }
    map.put(-1, new AtomicLong());
    return map;
  }

  String[] getPkgs()
  {
    return pkgs;
  }

  Pattern getPattern()
  {
    return pattern;
  }
}
