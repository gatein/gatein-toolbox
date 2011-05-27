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

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface SQLManMBean {

  /**
   * Returns the list of known packages.
   *
   * @return the package list
   */
  String[] getPackages();

  /**
   * Returns the list of known kinds.
   *
   * @return the kind list
   */
  String[] getKinds();

  /**
   * Returns the count value for a specified kind and index or -1 if it cannot be found.
   *
   * @param kind the kind
   * @param index the index
   * @return the related count value
   */
  long getCountValue(String kind, int index);

  /**
   * Reset the all the counters.
   */
  void clear();

  /**
   * Return a report.
   *
   * @return the report
   */
  String report();

  /**
   * Prints a report on the <code>System.out</code>
   */
  void printReport();
}
