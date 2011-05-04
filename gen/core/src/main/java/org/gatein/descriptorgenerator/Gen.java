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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Gen
{

   /** . */
   private final Writer navigationWriter;

   /** . */
   private final Writer pagesWriter;

   /** . */
   private final VelocityEngine velocityEngine;

   /** . */
   private final VelocityContext context;

   /** . */
   private final Config config;

   /** . */
   private final int childrenSize;

   /** . */
   private final int treeHeight;

   /** . */
   private final FQN fqn;

   public Gen(
      Config config,
      Writer navigationWriter,
      Writer pagesWriter)
   {
      VelocityEngine ve = new VelocityEngine();
      ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, config.getTemplatePath());

      //
      this.velocityEngine = ve;
      this.context = new VelocityContext();
      this.context.put("nodes", this);
      this.context.put("pages", this);

      this.navigationWriter = navigationWriter;
      this.pagesWriter = pagesWriter;
      
      this.fqn = new FQN("n");
      this.childrenSize = config.getChildrenSize();
      this.treeHeight = config.getHeight();
      this.config= config;
   }

   public void generate() throws Exception
   {
      this.context.put("height", config.getHeight());
      Template templatePages = velocityEngine.getTemplate("pages-root.xml");
      templatePages.merge(context, pagesWriter);

      this.context.put("height", config.getHeight());
      Template templateNavigation = velocityEngine.getTemplate("navigation-root.xml");
      templateNavigation.merge(context, navigationWriter);
   }

   public String includePage(String templateName, int localTreeHeight)
   {
      if (localTreeHeight > 0)
      {
         StringWriter sw = new StringWriter();
         Template template = velocityEngine.getTemplate(templateName);

         for (int i = 0; i < childrenSize; ++i)
         {
            fqn.push("" + i);

            //
            String pageId = fqn.toId();
            String path = fqn.toPath();
            String name = fqn.getName();
            context.put("id", pageId);
            context.put("path", path);
            context.put("name", name);
            context.put("pageRef", "portal::classic::" + pageId);
            context.put("height", localTreeHeight);

            //
            template.merge(context, sw);
            
            sw.append(includePage(templateName, localTreeHeight - 1));
            fqn.pop();
         }
         return sw.toString();
      }
      else
      {
         return "";
      }
   }

   public String includeNode(String templateName, int localTreeHeight)
   {
      if (localTreeHeight > 0)
      {
         StringWriter sw = new StringWriter();
         Template template = velocityEngine.getTemplate(templateName);

         for (int i = 0; i < childrenSize; ++i)
         {
            fqn.push("" + i);

            //
            String pageId = fqn.toId();
            String path = fqn.toPath();
            String name = fqn.getName();
            context.put("id", pageId);
            context.put("path", path);
            context.put("name", name);
            context.put("pageRef", "portal::classic::" + pageId);
            context.put("height", localTreeHeight);

            //
            template.merge(context, sw);

            sw.append(includeNode(templateName, localTreeHeight - 1));
            sw.append("</node>");
            fqn.pop();
         }
         return sw.toString();
      }
      else
      {
         return "";
      }
   }
}
