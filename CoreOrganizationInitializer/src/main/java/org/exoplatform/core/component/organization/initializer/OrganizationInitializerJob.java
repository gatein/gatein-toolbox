/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.core.component.organization.initializer;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;

public class OrganizationInitializerJob extends BaseJob {

  private static final Log LOG = ExoLogger.getLogger("job.OrganizationInitializerJob");

  private OrganizationListenersInitializerService organizationListenersInitializerService_;

  /**
   * {@inheritDoc}
   */
  public void execute(JobContext context) throws Exception {
    LOG.info("File plan job started");
    if (organizationListenersInitializerService_ == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      organizationListenersInitializerService_ = (OrganizationListenersInitializerService) container.getComponentInstanceOfType(OrganizationListenersInitializerService.class);
    }

    RequestLifeCycle.begin(PortalContainer.getInstance());

    try
    {
       // TODO: Make checkFolders configurable through startup parameters? Or try existence of JCR workspace?
       boolean checkFolders = true;

       organizationListenersInitializerService_.launchAll(checkFolders);
    }
    finally
    {
       RequestLifeCycle.end();
    }

    LOG.info("Organization Listeners Initializer job done");
  }
}
