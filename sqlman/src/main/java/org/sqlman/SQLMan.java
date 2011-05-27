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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SQLMan implements SQLManMBean {

  /** Empty arrays. */
  private static final String[] EMPTY_ARRAY = new String[0];

  public static SQLMan getInstance() {
    return instance;
  }

  /** . */
  private static final SQLMan instance = new SQLMan();

  static {
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.registerMBean(instance, ObjectName.getInstance("exo", "service", "sqlman"));
    }
    catch (Exception e) {
      throw new Error("Coult not register sql man mbean", e);
    }

    // Register shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        String report = instance.report();
        System.out.println("-= SQLMan report =-");
        System.out.println(report);
      }
    });
  }

  /** . */
  private final Pattern pattern;

  /** . */
  private final ConcurrentMap<String, ConcurrentMap<Integer, AtomicLong>> map;

  /** . */
  private String[] pkgs;

  private SQLMan() {
    StringBuilder sb = new StringBuilder("^");
    String pkgList = System.getProperty("sqlman.pkgs");
    String[] pkgs;
    if (pkgList != null) {
      pkgs = pkgList.split("\b*,\b*");
      for (int i = 0; i < pkgs.length; i++) {
        String pkg = pkgs[i] = pkgs[i].trim();
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

    //

    //
    this.pattern = Pattern.compile(sb.toString());
    this.pkgs = pkgs;
    this.map = new ConcurrentHashMap<String, ConcurrentMap<Integer, AtomicLong>>();
  }

  private ConcurrentMap<Integer, AtomicLong> getMap(String kind)
  {
    ConcurrentMap<Integer, AtomicLong> tmp = map.get(kind);
    if (tmp == null)
    {
      tmp = new ConcurrentHashMap<Integer, AtomicLong>();
      for (int i = 0;i < pkgs.length;i++)
      {
        tmp.put(i, new AtomicLong());
      }
      ConcurrentMap<Integer, AtomicLong> phantom = map.putIfAbsent(kind, tmp);
      if (phantom != null)
      {
        tmp = phantom;
      }
    }
    return tmp;
  }

  public void log(String kind) {
    if (kind != null)
    {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      for (StackTraceElement frame : stack) {
        String className = frame.getClassName();
        Matcher matcher = pattern.matcher(className);
        if (matcher.matches()) {
          for (int i = 0; i < matcher.groupCount(); i++) {
            String group = matcher.group(1 + i);
            if (group != null) {
              AtomicLong acc = getMap(kind).get(i);
              acc.incrementAndGet();
            }
          }
          break;
        }
      }
    }
  }

  // MBean implementation *********************************************************************************************

  public String[] getPackages() {
    return pkgs.clone();
  }

  public String[] getKinds() {
    return map.keySet().toArray(EMPTY_ARRAY);
  }

  public void printReport() {
    System.out.println(report());
  }

  public long getCountValue(String kind, int index) {
    ConcurrentMap<Integer, AtomicLong> tmp = map.get(kind);
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

  public void clear()
  {
    map.clear();
  }

  public String report() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, ConcurrentMap<Integer, AtomicLong>> entry : map.entrySet())
    {
      for (int i = 0; i < pkgs.length; i++) {
        String pkg = pkgs[i];
        AtomicLong l = entry.getValue().get(i);
        sb.append(entry.getKey()).append("/").append(pkg).append(": ").append(l.get()).append("\n");
      }
    }
    return sb.toString();
  }
}
