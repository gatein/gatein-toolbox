/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.portal.injector.page;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.injector.AbstractInjector;

import java.util.Collections;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 * The <code>PageDataInjector</code> class represents service generating and injecting page to GateIn.
 * The service can be accessed via JMX (MBean: exo/pageInject/pageInjector) or REST
 *
 */

@Managed
@ManagedDescription("Page data injector")
@NameTemplate({@Property(key = "view", value = "portal"), @Property(key = "service", value = "pageInjector"),
   @Property(key = "type", value = "pageInject")})
@RESTEndpoint(path = "pageInjector")
public class PageDataInjector extends AbstractInjector
{
   private static Logger LOG = LoggerFactory.getLogger(PageDataInjector.class);

   private PageService pageService;

   public PageDataInjector(PageService _pageService)
   {
      pageService = _pageService;
   }

   @Override
   public Logger getLogger()
   {
      return LOG;
   }

   /**
    * Generate and inject pages to specified site
    *
    * @param type
    * The type of site (ex: portal, group, user)
    *
    * @param name
    * The name of site
    *
    * @param pageNamePrefix
    * The prefix name for page
    *
    * @param pageTitlePrefix
    * The prefix title for page
    *
    * @param startIndex
    * The start of index to identify page
    *
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("Create new pages")
   @Impact(ImpactType.READ)
   public void createPages(
      @ManagedDescription("Site type of page") @ManagedName("siteType") String type,
      @ManagedDescription("Site name of page") @ManagedName("siteName") String name,
      @ManagedDescription("Page name prefix") @ManagedName("pageNamePrefix") String pageNamePrefix,
      @ManagedDescription("Page title prefix") @ManagedName("pageTitlePrefix") String pageTitlePrefix,
      @ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex,
      @ManagedDescription("Ending index") @ManagedName("endIndex") String endIndex)
   {
      try
      {
         startTransaction();
         SiteType siteType = SiteType.valueOf(type.toUpperCase());
         SiteKey siteKey = siteType.key(name);
         int start = Integer.parseInt(startIndex);
         int end = Integer.parseInt(endIndex);

         for (int i = start; i < end; i++)
         {
            PageContext page = new PageContext(siteKey.page(pageNamePrefix + "_" + i), new PageState(pageTitlePrefix + "_"
               + i, null, true, null, Collections.<String>emptyList(), null));
            pageService.savePage(page);
         }
      }
      catch (Exception e)
      {
         LOG.error("Failed to create new page", e);
      }
      finally
      {
         endTransaction();
      }
   }

   @Managed
   @ManagedDescription("Remove pages")
   @Impact(ImpactType.READ)
   public void removePages(@ManagedDescription("Site type of page") @ManagedName("siteType") String siteType,
                           @ManagedDescription("Site name of page") @ManagedName("siteName") String siteName,
                           @ManagedDescription("Page name prefix") @ManagedName("pageNamePrefix") String pageNamePrefix,
                           @ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex,
                           @ManagedDescription("Ending index") @ManagedName("endIndex") String endIndex)
   {
      try
      {
         int _startIndex = Integer.parseInt(startIndex);
         int _endIndex = Integer.parseInt(endIndex);
         SiteType type = SiteType.valueOf(siteType.toUpperCase());
         SiteKey key = type.key(siteName);

         startTransaction();
         for (int i = _startIndex; i < _endIndex; i++)
         {
            pageService.destroyPage(new PageKey(key, pageNamePrefix + "_" + i));
         }
      }
      catch (Exception ex)
      {
         LOG.error("Failed to destroy pages", ex);
      }
      finally
      {
         endTransaction();
      }
   }
}
