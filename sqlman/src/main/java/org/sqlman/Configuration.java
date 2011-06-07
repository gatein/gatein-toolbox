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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

  ConcurrentMap<Integer, Statistic> buildMap()
  {
    ConcurrentMap<Integer, Statistic> map = new ConcurrentHashMap<Integer, Statistic>();
    for (int i = 0;i < pkgs.length;i++)
    {
      map.put(i, new Statistic());
    }
    map.put(-1, new Statistic());
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
