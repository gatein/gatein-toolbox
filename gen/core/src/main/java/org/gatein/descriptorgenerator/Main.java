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

package org.gatein.descriptorgenerator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Main
{
   public static void main(String[] args) throws Exception
   {
      Config config = new Config();

      //
      File outDirectory = new File(config.getOutputPath());
      outDirectory.mkdir();

      //
      StringWriter navigationWriter = new StringWriter();
      StringWriter pagesWriter = new StringWriter();

      //
      Gen gen = new Gen(config, navigationWriter, pagesWriter);
      gen.generate();

      //
      FileWriter navigationFileWriter = new FileWriter(outDirectory.getAbsolutePath() + "/navigation.xml");
      FileWriter pagesFileWriter = new FileWriter(outDirectory.getAbsolutePath() + "/pages.xml");

      //
      reformatXML(new StringReader(navigationWriter.toString()), navigationFileWriter);
      reformatXML(new StringReader(pagesWriter.toString()), pagesFileWriter);

      //
      navigationWriter.close();
      pagesWriter.close();
   }

   private static void reformatXML(Reader src, Writer dst) throws Exception
   {
      String s = "" +
         "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
         "<xsl:output method=\"xml\" omit-xml-declaration=\"yes\"/>\n" +
         "<xsl:strip-space elements=\"*\"/>\n" +
         "<xsl:template match=\"@*|node()\">\n" +
         "<xsl:copy>\n" +
         "<xsl:apply-templates select=\"@*|node()\"/>\n" +
         "</xsl:copy>\n" +
         "</xsl:template>\n" +
         "</xsl:stylesheet>";

      //
      TransformerFactory factory = TransformerFactory.newInstance();
      factory.setAttribute("indent-number", 2);
      Transformer transformer = factory.newTransformer(new StreamSource(new StringReader(s)));
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StreamSource source = new StreamSource(src);
      StreamResult result = new StreamResult(dst);
      transformer.transform(source, result);
   }

}
