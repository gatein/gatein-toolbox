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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Statistic {

  /** . */
  private final LongStatistic size = new LongStatistic();

  /** . */
  private final LongStatistic timeMillis = new LongStatistic();

  /** . */
  private final LongStatistic minMillis = new LongStatistic(Long.MAX_VALUE);

  /** . */
  private final LongStatistic maxMillis = new LongStatistic();

  public boolean isEmpty() {
    return size.get() > 0;
  }

  public long getSize() {
    return size.get();
  }

  public long getTimeMillis() {
    return timeMillis.get();
  }

  void log(long millis) {
    size.incrementAndGet();
    if (millis > 0) {
      timeMillis.addAndGet(millis);
      minMillis.setIfLesser(millis);
      maxMillis.setIfGreater(millis);
    }
  }

  void merge(Statistic that) {
    long v1 = that.size.get();
    if (v1 > 0) {
      size.addAndGet(v1);
      timeMillis.addAndGet(that.timeMillis.get());
      minMillis.setIfLesser(that.minMillis.get());
      maxMillis.setIfGreater(that.maxMillis.get());
    }
  }

  void report(StringBuilder sb) {
    sb.append("count=").append(size.get()).
       append(",time=").append(timeMillis.get()).
       append(",min=").append(minMillis.get()).
       append(",max=").append(maxMillis.get());
  }
}
