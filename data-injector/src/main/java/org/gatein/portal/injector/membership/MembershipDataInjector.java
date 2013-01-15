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

package org.gatein.portal.injector.membership;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.injector.AbstractInjector;
import java.util.Date;

/**
 * @author <a href="mailto:trongson.tran1228@gmail.com">Son Tran Trong</a>
 * @version $Id$
 * 
 * The <code>UserDataInjector</code> class represents service generating and injecting users to GateIn.
 * The service can be accessed via JMX (MBean: exo/userDataInject/portal/userInjector) or REST
 *
 */

@Managed
@ManagedDescription("Membership data injector")
@NameTemplate({@Property(key = "view", value = "portal")
               ,@Property(key = "service", value = "membershipInjector")
               ,@Property(key = "type", value = "membershipDataInject")})
@RESTEndpoint(path = "membershipInjector")
public class MembershipDataInjector extends AbstractInjector
{
   private static Logger LOG = LoggerFactory.getLogger(MembershipDataInjector.class);

   private MembershipTypeHandler mtHandler;

   public MembershipDataInjector(OrganizationService orgService)
   {
      this.mtHandler = orgService.getMembershipTypeHandler();
   }

   @Override
   public Logger getLogger()
   {
      return LOG;
   }

   public void createMembership(String membershipName, String description) throws Exception
   {
      MembershipType mt = mtHandler.findMembershipType(membershipName);
      mt = mtHandler.createMembershipTypeInstance();
      mt.setName(membershipName);
      mt.setDescription(description);
      Date now = new Date();
      mt.setCreatedDate(now);
      mt.setModifiedDate(now);
      mtHandler.createMembershipType(mt, true);
   }
   
   /**
    * Generate memberships data for Gatein
    * 
    * @param membershipName
    * The default membership name
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of memberships, 
    * such as membershipName_0, membershipName_1, membershipName_n
    * 
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("Create amount of new memberships")
   @Impact(ImpactType.READ)
   public void createMembershipsType(@ManagedDescription("Membership name") @ManagedName("membershipName") String membershipName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex")  String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex")  String endIndex)
   {
      try
      {
         if (membershipName == null || membershipName.trim().length() == 0)
         {
            LOG.error("Membership name cannot be null or empty");
            throw new Exception();
         }
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         membershipName = membershipName.trim();
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            String membershipNameTemp = membershipName + "_" + i;
            createMembership(membershipNameTemp, membershipNameTemp);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Create memberships fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }

   /**
    * Remove a list of memberships
    * 
    * @param membershipName
    * The default membership name
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of memberships that need to be removed, 
    * such as membershipName_0, membershipName_1, membershipName_2
    * 
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("Remove amount of memberships")
   @Impact(ImpactType.READ)
   public void removeMembershipsType(@ManagedDescription("Membership name") @ManagedName("membershipName") String membershipName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex") String endIndex)
   {
      try
      {
         if (membershipName == null || membershipName.trim().length() == 0)
         {
            LOG.error("Membership name cannot be null or empty");
            throw new Exception();
         }
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         membershipName = membershipName.trim();
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            String membershipNameTemp = membershipName + "_" + i;
            mtHandler.removeMembershipType(membershipNameTemp, true);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Remove memberships fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }
}
