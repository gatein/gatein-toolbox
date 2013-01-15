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
package org.gatein.portal.injector.navigation;

import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationServiceWrapper;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.injector.AbstractInjector;
import java.util.List;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/8/13
 */
@Managed
@ManagedDescription("Navigation data injector")
@NameTemplate({@Property(key = "view", value = "portal"), @Property(key = "service", value = "navigationInjector"), @Property(key = "type", value = "navdataInject")})
@RESTEndpoint(path = "navInjector")
public class NavigationDataInjector extends AbstractInjector
{
   private static Logger LOG = LoggerFactory.getLogger(NavigationDataInjector.class);

   private NavigationServiceWrapper navService;

   private DataStorage dataStorage;

   public NavigationDataInjector(NavigationServiceWrapper _navService, DataStorage _dataStorage)
   {
      navService = _navService;
      dataStorage = _dataStorage;
   }

   @Override
   public Logger getLogger()
   {
      return LOG;
   }

   public void createNavigation(String type, String owner, List<String> children)
   {
      SiteKey key = new SiteKey(type, owner);
      NavigationContext navCtx = navService.loadNavigation(key);
      if (navCtx != null)
      {
         LOG.error("Navigation type= " + type + " , owner= " + owner + " already exists");
      }
      else
      {
         try
         {
            if (dataStorage.getPortalConfig(type, owner) == null)
            {
               dataStorage.create(new PortalConfig(type, owner));
            }
         }
         catch (Exception ex)
         {
            LOG.error("Error while load/create site ( type= " + type + " , owner = " + owner, ex);
         }

         navCtx = new NavigationContext(key, new NavigationState(0));
         NavNode rootNode = navService.loadNode(new NavNodeModel(), navCtx, Scope.CHILDREN, null).getNode();
         int index = 0;
         for (String child : children)
         {
            rootNode.addChild(index, child);
            index++;
         }
         navService.saveNode(rootNode.context, null);
      }
   }

   public void createNavigation(String type, String owner, String prefix, int startIndex, int endIndex)
   {
      if (startIndex < endIndex)
      {
         SiteKey key = new SiteKey(type, owner);
         NavigationContext navCtx = navService.loadNavigation(key);
         if (navCtx != null)
         {
            LOG.error("Navigation type= " + type + " , owner= " + owner + " already exists");
         }
         else
         {
            try
            {
               if (dataStorage.getPortalConfig(type, owner) == null)
               {
                  dataStorage.create(new PortalConfig(type, owner));
               }
            }
            catch (Exception ex)
            {
               LOG.error("Error while load/create site ( type= " + type + " , owner = " + owner, ex);
            }

            navService.saveNavigation(new NavigationContext(key, new NavigationState(0)));
            navCtx = navService.loadNavigation(key);
            NavNode rootNode = navService.loadNode(new NavNodeModel(), navCtx, Scope.CHILDREN, null).getNode();
            for (int i = startIndex; i < endIndex; i++)
            {
               rootNode.addChild(i, prefix + "_" + i);
            }
            navService.saveNode(rootNode.context, null);
         }
      }
   }

   public void addNodes(String navType, String navOwner, List<String> nodeNames)
   {
      SiteKey key = new SiteKey(navType, navOwner);
      NavigationContext navCtx = navService.loadNavigation(key);
      if (navCtx == null)
      {
         LOG.error("Navigation type= " + navType + " , owner= " + navOwner + " does not exist");
      }
      else
      {
         NavNode rootNode = navService.loadNode(new NavNodeModel(), navCtx, Scope.CHILDREN, null).getNode();
         int index = rootNode.getSize();
         for (String nodeName : nodeNames)
         {
            if (rootNode.getChild(nodeName) != null)
            {
               LOG.debug("Navigation node named " + nodeName + " already exist, will not add it anymore");
            }
            else
            {
               rootNode.addChild(index, nodeName);
               index++;
            }
         }
         navService.saveNode(rootNode.context, null);
      }
   }

   public void addNodes(String navType, String navOwner, String nodePrefix, int startIndex, int endIndex)
   {
      if (startIndex < endIndex)
      {
         SiteKey key = new SiteKey(navType, navOwner);
         NavigationContext navCtx = navService.loadNavigation(key);
         if (navCtx == null)
         {
            LOG.error("Navigation type= " + navType + " , owner= " + navOwner + " does not exist");
         }
         else
         {
            NavNode rootNode = navService.loadNode(new NavNodeModel(), navCtx, Scope.CHILDREN, null).getNode();
            int index = rootNode.getSize();
            for (int i = startIndex; i < endIndex; i++)
            {
               String nodeName = nodePrefix + "_" + i;
               if (rootNode.getChild(nodeName) != null)
               {
                  LOG.debug("Navigation node named " + nodeName + " already exist, will not add it anymore");
               }
               else
               {
                  rootNode.addChild(index, nodeName);
                  index++;
               }
            }
            navService.saveNode(rootNode.context, null);
         }
      }
   }

   /**
    *
    * @param navType
    * @param navOwner
    * @param absolutePath path from root to targeted node
    * @param nodePrefix
    * @param startIndex
    * @param endIndex
    */
   public void addNodes(String navType, String navOwner, String absolutePath, String nodePrefix, int startIndex, int endIndex)
   {
      SiteKey key = new SiteKey(navType, navOwner);
      NavigationContext navCtx = navService.loadNavigation(key);
      if (navCtx == null)
      {
         LOG.error("Navigation type= " + navType + " , owner= " + navOwner + " does not exist");
      }
      else
      {
         String[] path = absolutePath.split("/");
         NavNode rootNode = navService.loadNode(new NavNodeModel(), navCtx, GenericScope.branchShape(path), null).getNode();
         NavNode targetNode = rootNode.getDescendant(path);
         if (targetNode == null)
         {
            LOG.error("Could not find node specified by path " + absolutePath + " under navigation type= " + navType + " , owner= " + navOwner);
         }
         else
         {
            int index = targetNode.getSize();
            for (int i = startIndex; i < endIndex; i++)
            {
               String nodeName = nodePrefix + "_" + i;
               if (targetNode.getChild(nodeName) == null)
               {
                  targetNode.addChild(index, nodeName);
                  index++;
               }
            }
            navService.saveNode(targetNode.context, null);
         }
      }
   }

   @Managed
   @ManagedDescription("Create new navigation")
   @Impact(ImpactType.READ)
   public void createNavs(@ManagedDescription("Type of new navigation") @ManagedName("navType") String type,
                          @ManagedDescription("Owner of new navigation") @ManagedName("navOwner") String owner,
                          @ManagedDescription("Prefix of new node names") @ManagedName("prefix") String prefix,
                          @ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex,
                          @ManagedDescription("Ending index") @ManagedName("endIndex") String endIndex)
   {
      int _startIndex = Integer.parseInt(startIndex);
      int _endIndex = Integer.parseInt(endIndex);
      //Invoke from JMX bean will not go through GateIn's servlet filter. Therefore, we need to open/close transaction
      try
      {
         startTransaction();
         createNavigation(type, owner, prefix, _startIndex, _endIndex);
      }
      catch (Exception ex)
      {
         LOG.error("Failed to create new navigation", ex);
      }
      finally
      {
         endTransaction();
      }
   }

   @Managed
   @ManagedDescription("Add nodes into root node of existing navigation")
   @Impact(ImpactType.READ)
   public void insertNodes(@ManagedDescription("Type of target navigation") @ManagedName("navType") String navType,
                           @ManagedDescription("Owner of target navigation") @ManagedName("navOwner") String navOwner,
                           @ManagedDescription("Prefix of new node names") @ManagedName("prefix") String nodePrefix,
                           @ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex,
                           @ManagedDescription("Ending index") @ManagedName("endIndex") String endIndex)
   {
      int _startIndex = Integer.parseInt(startIndex);
      int _endIndex = Integer.parseInt(endIndex);
      //Invoke from JMX bean will not go through GateIn's servlet filter. Therefore, we need to open/close transaction
      try
      {
         startTransaction();
         addNodes(navType, navOwner, nodePrefix, _startIndex, _endIndex);
      }
      catch (Exception ex)
      {
         LOG.error("Failed to insert new nodes", ex);
      }
      finally
      {
         endTransaction();
      }
   }

   @Managed
   @ManagedDescription("Add nodes into node specified by path in existing navigation")
   @Impact(ImpactType.READ)
   public void insertNodes(@ManagedDescription("Type of target navigation") @ManagedName("navType") String navType,
                           @ManagedDescription("Owner of target navigation") @ManagedName("navOwner") String navOwner,
                           @ManagedDescription("Path from root to target node") @ManagedName("absolutePath") String absolutePath,
                           @ManagedDescription("Prefix of new node names") @ManagedName("prefix") String nodePrefix,
                           @ManagedDescription("Starting index") @ManagedName("startIndex") String startIndex,
                           @ManagedDescription("Ending index") @ManagedName("endIndex") String endIndex)
   {
      int _startIndex = Integer.parseInt(startIndex);
      int _endIndex = Integer.parseInt(endIndex);
      //Invoke from JMX bean will not go through GateIn's servlet filter. Therefore, we need to open/close transaction
      try
      {
         startTransaction();
         addNodes(navType, navOwner, absolutePath, nodePrefix, _startIndex, _endIndex);
      }
      catch (Exception ex)
      {
         LOG.error("Failed to insert new nodes", ex);
      }
      finally
      {
         endTransaction();
      }
   }

   @Managed
   @ManagedDescription("Delete node specified by path in existing navigation")
   @Impact(ImpactType.READ)
   public void deleteNode(@ManagedDescription("Type of target navigation") @ManagedName("navType") String navType,
                          @ManagedDescription("Owner of target navigation") @ManagedName("navOwner") String navOwner,
                          @ManagedDescription("Path from root to target node") @ManagedName("pathFromRoot") String pathFromRoot)
   {
      try
      {
         startTransaction();
         NavigationContext ctx = navService.loadNavigation(new SiteKey(navType, navOwner));
         if (ctx == null)
         {
            LOG.error("Navigation type= " + navType + " , owner= " + navOwner + " does not exist");
         }
         else
         {
            String[] path = pathFromRoot.split("/");
            NavNode rootNode = navService.loadNode(new NavNodeModel(), ctx, GenericScope.branchShape(path), null).getNode();
            NavNode targetNode = rootNode.getDescendant(path);
            if (targetNode != null && targetNode != rootNode)
            {
               targetNode.remove();
            }
         }
      }
      finally
      {
         endTransaction();
      }
   }
}
