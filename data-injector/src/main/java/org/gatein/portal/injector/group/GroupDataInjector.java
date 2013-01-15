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

package org.gatein.portal.injector.group;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.injector.AbstractInjector;

/**
 * @author <a href="mailto:trongson.tran1228@gmail.com">Son Tran Trong</a>
 * @version $Id$
 * 
 * The <code>GroupDataInjector</code> class represents service generating and injecting groups to GateIn.
 * The service can be accessed via JMX (MBean: exo/groupDataInject/portal/groupInjector) or REST
 *
 */

@Managed
@ManagedDescription("Group data injector")
@NameTemplate({@Property(key = "view", value = "portal")
            ,@Property(key = "service", value = "groupInjector")
            ,@Property(key = "type", value = "groupDataInject")})
@RESTEndpoint(path = "groupInjector")
public class GroupDataInjector extends AbstractInjector
{
   private static Logger LOG = LoggerFactory.getLogger(GroupDataInjector.class);

   private OrganizationService orgService;

   private GroupHandler groupHandler;

   public GroupDataInjector(OrganizationService orgService)
   {
      this.orgService = orgService;
      this.groupHandler = orgService.getGroupHandler();
   }

   @Override
   public Logger getLogger()
   {
      return LOG;
   }

   public void createGrop(String parentName, String groupName, String label, String description) throws Exception
   {
      Group group = groupHandler.createGroupInstance();
      group.setGroupName(groupName);
      group.setLabel(label);
      group.setDescription(description);

      if (parentName == null)
      {
         groupHandler.addChild(null, group, true);
      }
      else
      {
         Group parentGroup = groupHandler.findGroupById("/" + parentName);
         groupHandler.addChild(parentGroup, group, true);
      }
   }

   /**
    * Generate groups for Gatein
    * 
    * @param groupName
    * The default groupName
    * 
    * @param parentName
    * The parent group name 
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of group, such as groupName_0, groupName_1, groupName_n
    * 
    * @param endIndex
    * The end of index
    * 
    * @param createParent
    * Boolean parameter that allow to create parent if doesn't exist
    */
   @Managed
   @ManagedDescription("Create amount of new groups")
   @Impact(ImpactType.READ)
   public void createGroups(@ManagedDescription("Group name") @ManagedName("groupName") String groupName
      ,@ManagedDescription("Parent group name") @ManagedName("parentName") String parentName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex") String endIndex
      ,@ManagedDescription("Creating parent if it doesn't exist") @ManagedName("createParent") String createParent)
   {
      try
      {
         boolean allowCreateParent = Boolean.parseBoolean(createParent);
         groupName = groupName.trim();
         if (groupName.length() == 0)
         {
            LOG.error("groupName cannot be null or empty");
         }
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         startTransaction();
         if (parentName == null || parentName.trim().length() == 0)
         {
            parentName = null;
         }
         else
         {
            parentName = parentName.trim();
            Group parentGrop = orgService.getGroupHandler().findGroupById("/" + parentName);
            if (parentGrop == null)
            {
               if (allowCreateParent)
               {
                  createGrop(null, parentName, parentName, parentName);
               }
               else
               {
                  LOG.error("Parent Group doesn't exist");
                  throw new Exception();
               }
            }
         }
         for (int i = sIndex; i <= eIndex; i++)
         {
            createGrop(parentName, groupName + "_" + i, groupName + "_" + i, groupName + "_" + i);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Create groups fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }
   
   /**
    * Remove a list of groups
    * 
    * @param groupName
    * The default group name
    * 
    * @param parentName
    * The parent group name
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of group that need to remove, 
    * such as groupName_0, groupName_1, groupName_n
    * 
    * @param endIndex
    * The end of index
    * 
    * @param deleteParent
    * Boolean parameter that allow to remove parent.
    */
   @Managed
   @ManagedDescription("Remove amount of groups")
   @Impact(ImpactType.READ)
   public void removeGroups(@ManagedDescription("Group name") @ManagedName("groupName") String groupName
      ,@ManagedDescription("Parent group name") @ManagedName("parentName") String parentName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex") String endIndex
      ,@ManagedDescription("Alow to delete parent") @ManagedName("deleteParent") String deleteParent) 
   {
      try
      {
         boolean allowDeleteParent = Boolean.parseBoolean(deleteParent);
         groupName = groupName.trim();
         if (groupName.length() == 0)
         {
            LOG.error("groupName cannot be null or empty");
         }

         if (parentName == null || parentName.trim().length() == 0)
         {
            parentName = null;
         }
         else
         {
            groupName = "/" + parentName + "/" + groupName;
         }
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            Group group = groupHandler.findGroupById(groupName + "_" + i);
            if (group != null)
               groupHandler.removeGroup(group, true);
         }

         if (parentName != null && allowDeleteParent)
         {
            Group group = groupHandler.findGroupById("/" + parentName);
            if (group != null)
               groupHandler.removeGroup(group, true);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Remove groups fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }
   
   /**
    * Add a list of user to specific group with membership type
    * 
    * @param userName
    * The default user name
    * 
    * @param groupName
    * The group that user in. Format of groupName: [/parentName]/groupName such as: /platform/guest
    * 
    * @param membershipName
    * The membership type, such as: member, validator, ...
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of user that need to added to membership, 
    * such as groupName_0, groupName_1, groupName_n
    * 
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("Add amount of memberships")
   @Impact(ImpactType.READ)
   public void addMemberships(@ManagedDescription("User name") @ManagedName("userName") String userName
      ,@ManagedDescription("Group name") @ManagedName("groupName") String groupName
      ,@ManagedDescription("Membership name") @ManagedName("membershipName") String membershipName
      ,@ManagedDescription("The starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("The end of index") @ManagedName("endIndex") String endIndex) 
   {
      try
      {
         Group group = groupHandler.findGroupById(groupName);
         if (group == null)
         {
            LOG.error("groupName doesn't exist. Notice that groupName's format must be [/groupParent]/groupName");
         }
         MembershipType mt = orgService.getMembershipTypeHandler().findMembershipType(membershipName);
         if (mt == null)
         {
            LOG.error("membershipName doesn't exist.");
         }

         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            User user = orgService.getUserHandler().findUserByName(userName + "_" + i);
            if (user != null)
            {
               orgService.getMembershipHandler().linkMembership(user, group, mt, true);
            }
            else
            {
               LOG.error("User " + userName + "_" + i + " doesn't exist.");
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Remove groups fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }
   
   /**
    * Remove a list of memberships
    * 
    * @param userName
    * The default user name
    * 
    * @param groupName
    * The group that user in. Format of groupName: [/parentName]/groupName such as: /platform/guest
    * 
    * @param membershipName
    * The membership type, such as: member, validator, ...
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of user that need to removed from membership, 
    * such as groupName_0, groupName_1, groupName_n
    * 
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("Remove amount of memberships")
   @Impact(ImpactType.READ)
   public void removeMemberships(@ManagedDescription("User name") @ManagedName("userName") String userName
      ,@ManagedDescription("Group name") @ManagedName("groupName") String groupName
      ,@ManagedDescription("Membership name") @ManagedName("membershipName") String membershipName
      ,@ManagedDescription("The starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("The end of index") @ManagedName("endIndex") String endIndex)
   {
      try
      {
         groupName = groupName.trim();
         membershipName = membershipName.trim();
         userName = userName.trim();

         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            StringBuffer sb = new StringBuffer();
            sb.append(membershipName)
            .append(":").append(userName).append("_").append(i)
            .append(":").append(groupName);
            Membership ms = orgService.getMembershipHandler().removeMembership(sb.toString(), true);
            if (ms == null)
            {
               LOG.error("Remove membership with id: " + sb.toString() + " fail. Check your inputs");
            }
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
