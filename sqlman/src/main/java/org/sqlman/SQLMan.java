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
        StringBuilder report = new StringBuilder("-= SQLMan aggregated report =-\n");
        instance.shared.report(report);
        System.out.println(report);
      }
    });
  }

  /** . */
  private final Configuration config;

  /** . */
  private final ConcurrentStatisticCollector shared;

  private SQLMan() {
    String pkgList = System.getProperty("sqlman.pkgs");

    //
    this.config = new Configuration(pkgList);
    this.shared = new ConcurrentStatisticCollector(config);
  }

  public void log(String kind) {
    LocalStatisticCollector local = localCollector.get();
    if (local.getDepth() > 0) {
      local.log(kind);
    } else {
      shared.log(kind);
    }
  }

  private final ThreadLocal<LocalStatisticCollector> localCollector = new ThreadLocal<LocalStatisticCollector>() {

    @Override
    protected LocalStatisticCollector initialValue() {
      return new LocalStatisticCollector(shared, config);
    }
  };

  public void begin(Object context)
  {
    localCollector.get().begin(context);
  }

  public void end()
  {
    localCollector.get().end();
  }

  // MBean implementation *********************************************************************************************

  public String[] getPackages() {
    return config.getPkgs().clone();
  }

  public String[] getKinds() {
    return shared.getKinds().toArray(EMPTY_ARRAY);
  }

  public void printReport() {
    System.out.println(report());
  }

  public long getCountValue(String kind, int index) {
    Statistic statistic = shared.getStatistic(kind, index, false);
    return statistic != null ? statistic.getCount() : -1;
  }

  public void clear()
  {
    shared.clear();
  }

  public String report() {
    return shared.report(new StringBuilder()).toString();
  }
}
