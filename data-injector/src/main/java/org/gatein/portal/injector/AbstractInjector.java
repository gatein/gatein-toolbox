/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.gatein.portal.injector;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.gatein.common.logging.Logger;
import org.picocontainer.Startable;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/11/13
 */
public abstract class AbstractInjector implements Startable
{
   protected PortalContainer portalContainer;

   @Override
   public final void start()
   {
      getLogger().info("Set portal container on actual service instance");
      portalContainer = (PortalContainer)ExoContainerContext.getCurrentContainer();
      _start();
   }

   public void _start()
   {
   }

   @Override
   public void stop()
   {
   }

   public void startTransaction()
   {
      RequestLifeCycle.begin(portalContainer);
   }

   public void endTransaction()
   {
      RequestLifeCycle.end();
   }

   public abstract Logger getLogger();
}
