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

import junit.framework.TestCase;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(BMUnitRunner.class)
public class SQLTestCase extends TestCase {

  @Test
  @BMScript(dir = "src/main/resources", value = "sqlman")
  public void testSimple() throws Exception {
    System.setProperty("sqlman.pkgs", "org.sqlman");
    SQLMan sqlman = SQLMan.getInstance();
    assertEquals(-1, sqlman.getCountValue("jdbc", 0));
    assertEquals(-1, sqlman.getCountValue("jdbcquery", 0));
    assertEquals(-1, sqlman.getCountValue("jdbcupdate", 0));

    //
    Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "");

    //
    PreparedStatement ps = conn.prepareStatement("CREATE TABLE FOO ( POUET INTEGER )");
    ps.executeUpdate();
    assertEquals(-1, sqlman.getCountValue("jdbc", 0));
    assertEquals(-1, sqlman.getCountValue("jdbcquery", 0));
    assertEquals(1, sqlman.getCountValue("jdbcupdate", 0));

    //
    PreparedStatement ps2 = conn.prepareStatement("INSERT INTO FOO (POUET) VALUES (?)");
    ps2.setInt(1, 50);
    ps2.execute();
    assertEquals(1, sqlman.getCountValue("jdbc", 0));
    assertEquals(-1, sqlman.getCountValue("jdbcquery", 0));
    assertEquals(1, sqlman.getCountValue("jdbcupdate", 0));

    //
    PreparedStatement ps3 = conn.prepareStatement("SELECT POUET FROM FOO");
    ResultSet rs = ps3.executeQuery();
    assertTrue(rs.next());
    assertEquals(50, rs.getInt(1));
    assertEquals(1, sqlman.getCountValue("jdbc", 0));
    assertEquals(1, sqlman.getCountValue("jdbcquery", 0));
    assertEquals(1, sqlman.getCountValue("jdbcupdate", 0));

    //
    ps.close();

    //
    long v1 = sqlman.getCountValue("loadbundle", 0);
    ResourceBundle bundle = ResourceBundle.getBundle("bundle", Locale.ENGLISH);
    assertNotNull(bundle);
    long v2 = sqlman.getCountValue("loadbundle", 0);
    assertEquals(2, v2 - v1);
  }
}
