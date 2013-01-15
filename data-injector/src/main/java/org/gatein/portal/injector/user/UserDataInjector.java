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

package org.gatein.portal.injector.user;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.injector.AbstractInjector;

/**
 * @author <a href="mailto:trongson.tran1228@gmail.com">Son Tran Trong</a>
 * @version $Id$
 * 
 * The <code>UserDataInjector</code> class represents service generating and injecting users to GateIn.
 * The service can be accessed via JMX (MBean: exo/userDataInject/portal/userInjector) or REST
 *
 */

@Managed
@ManagedDescription("User data injector")
@NameTemplate({@Property(key = "view", value = "portal")
               ,@Property(key = "service", value = "userInjector")
               ,@Property(key = "type", value = "userDataInject")})
@RESTEndpoint(path = "userInjector")
public class UserDataInjector extends AbstractInjector
{
   private static Logger LOG = LoggerFactory.getLogger(UserDataInjector.class);

   private OrganizationService orgService;

   public UserDataInjector(OrganizationService orgService)
   {
      this.orgService = orgService;
   }
   
   public Logger getLogger()
   {
      return LOG;
   }

   public void createUser(String userName, String password, String email, String firstName, String lastName) throws Exception
   {
      boolean newUser = false;
      User user = orgService.getUserHandler().findUserByName(userName);
      if (user == null)
      {
         user = orgService.getUserHandler().createUserInstance(userName);
         newUser = true;
      }
      user.setPassword(password);
      user.setEmail(email);
      user.setFirstName(firstName);
      user.setLastName(lastName);

      if (newUser)
      {
         orgService.getUserHandler().createUser(user, true);
         return;

      }
      orgService.getUserHandler().saveUser(user, true);
   }

   /**
    * Generate users data for Gatein
    * 
    * @param userName
    * The default userName
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of users, such as userName_0, userName_1, userName_n
    * 
    * @param endIndex
    * The end of index
    * 
    * @param password
    * The password of user. If not set, it will be the default: 123456
    */
   @Managed
   @ManagedDescription("Create amount of new users")
   @Impact(ImpactType.READ)
   public void createListUsers(@ManagedDescription("User name") @ManagedName("userName") String userName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex") String endIndex
      ,@ManagedDescription("if not specific it will be default 123456") @ManagedName("password") String password)
   {
      try
      {
         if (password == null || password.trim().length() == 0)
         {
            password = new String("123456");
         }
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         userName = userName.trim();
         password = password.trim();
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            String userNameTemp = userName + "_" + i;
            createUser(userNameTemp, password, userNameTemp + "@localhost", userNameTemp, userNameTemp);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Create users fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }

   }

   /**
    * Remove a list of users
    * 
    * @param userName
    * The default user name
    * 
    * @param startIndex
    * The startIndex and the endIndex are used to generate a number of users that need to be removed, 
    * such as userName_0, userName_1, userName_n.
    * 
    * @param endIndex
    * The end of index
    */
   @Managed
   @ManagedDescription("remove amount of users")
   @Impact(ImpactType.READ)
   public void removeListUsers(@ManagedDescription("user name") @ManagedName("userName") String userName
      ,@ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex
      ,@ManagedDescription("End index") @ManagedName("endIndex") String endIndex)
   {
      try
      {
         int sIndex = Integer.parseInt(startIndex);
         int eIndex = Integer.parseInt(endIndex);
         userName = userName.trim();
         startTransaction();
         for (int i = sIndex; i <= eIndex; i++)
         {
            orgService.getUserHandler().removeUser(userName + "_" + i, true);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         LOG.error("Remove users fail. Please check your inputs");
      }
      finally
      {
         endTransaction();
      }
   }
}