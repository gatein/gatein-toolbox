/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Config
{
   private final String height;
   private final String childrenSize;
   private final String templatePath;
   private final String outputPath;

   public Config()
   {
      height =  System.getProperty("gateingen.height");
      childrenSize =  System.getProperty("gateingen.childrenSize");
      templatePath =  System.getProperty("gateingen.template.path");
      outputPath =  System.getProperty("gateingen.output.path");

      if (height == null)
      {
         throw new IllegalStateException("-Dgateingen.height must be set");
      }

      if (childrenSize == null)
      {
         throw new IllegalStateException("-Dgateingen.childrenSize must be set");
      }

      if (templatePath == null)
      {
         throw new IllegalStateException("-Dgateingen.template.path must be set");
      }

      if (outputPath == null)
      {
         throw new IllegalStateException("-Dgateingen.output.path must be set");
      }
   }

   public int getHeight()
   {
      return Integer.parseInt(height);
   }

   public int getChildrenSize()
   {
      return Integer.parseInt(childrenSize);
   }

   public String getTemplatePath()
   {
      return templatePath;
   }

   public String getOutputPath()
   {
      return outputPath;
   }
}
