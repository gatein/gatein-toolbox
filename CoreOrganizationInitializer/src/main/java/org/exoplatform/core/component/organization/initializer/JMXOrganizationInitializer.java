package org.exoplatform.core.component.organization.initializer;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.picocontainer.Startable;

/**
 * JMX Endpoint for accessing OrganizationListenersInitializerService stuff.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Managed
@ManagedDescription("Organization Listeners Initializer Service Endpoint")
@NameTemplate({
      @Property(key = "name", value = "OrganizationInitializerService"),
      @Property(key = "service", value = "OrganizationInitializerService")
})
public class JMXOrganizationInitializer implements Startable
{

   private OrganizationListenersInitializerService initializerService;
   private Log log = ExoLogger.getLogger(this.getClass());
   private OrganizationService orgService_;

   // TODO: We need this as workaround to handle bug https://issues.jboss.org/browse/EXOJCR-1791 as during client calls, 
   // we need to add this container to context. It should be removed when EXOJCR-1791 will be fixed.
   private ExoContainer exoContainer;


   public JMXOrganizationInitializer(ExoContainerContext exoContainerContext,
         OrganizationListenersInitializerService organizationListenersInitializerService, OrganizationService orgService)
   {
      this.exoContainer = exoContainerContext.getContainer();
      this.initializerService = organizationListenersInitializerService;
      this.orgService_=orgService;
   }

   @Managed
   @ManagedDescription("Launch all listeners for all users and groups")
   @Impact(ImpactType.WRITE)
   public String launchAllListeners(@ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") Boolean checkFolders)
   {
      // TODO: We need to encapsulate call within correct portal container by ourselves because JMX kernel layer is not doing it (Workaround for EXOJCR-1791)
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();      

      try            
      {
         ExoContainerContext.setCurrentContainer(this.exoContainer);
         boolean ok = initializerService.launchAll(checkFolders);
         String responseString = ok ? "All listeners executed successfuly." : "Error occured during execution of listeners.";
         return responseString;
      }
      finally 
      {
         ExoContainerContext.setCurrentContainer(oldContainer);
      }
   }

   /**
    *
    * @param userName username
    * @param checkFolders whether to check folders in OrganizationInitializerUtils or not. If folders are not checked, we simply trigger all listeners for user, userProfile and all his memberships.
    * @return rest response
    */
   @Managed
   @ManagedDescription("Launch user listeners for single user")
   @Impact(ImpactType.WRITE)
   public String launchUserListeners(@ManagedDescription("userName") @ManagedName("userName") String userName,
                                     @ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") Boolean checkFolders)
   {
      UserHandler userHandler = orgService_.getUserHandler();
      User user;
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();

      try
      {
         ExoContainerContext.setCurrentContainer(this.exoContainer);
         user = userHandler.findUserByName(userName);
         boolean ok = initializerService.treatUser(user, checkFolders);
         String responseString = ok ? "User listeners executed successfuly." : "Error occured during execution of user listeners.";
         return responseString;
      }
      catch (Exception e)
      {
         log.warn("Error with user "+userName, e);
         return "Error occured during execution of user listeners: " + e.getMessage();
      }
      finally
      {
         ExoContainerContext.setCurrentContainer(oldContainer);
      }
   }

   @Managed
   @ManagedDescription("Launch group listeners for single group")
   @Impact(ImpactType.WRITE)
   public String launchGroupListeners(@ManagedDescription("Group Name") @ManagedName("groupName") String groupName,
                                      @ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") Boolean checkFolders)
   {
      GroupHandler groupHandler = orgService_.getGroupHandler();
      Group group;
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();

      try
      {
         ExoContainerContext.setCurrentContainer(this.exoContainer);
         group = groupHandler.findGroupById(groupName);
         boolean ok = initializerService.treatGroup(group, checkFolders);
         String responseString = ok ? "Group listeners executed successfuly." : "Error occured during execution of group listeners.";
         return responseString;
      }
      catch (Exception e)
      {
         log.warn("Error with group " + groupName, e);
         return "Error occured during execution of group listeners: " + e.getMessage();
      }
      finally
      {
         ExoContainerContext.setCurrentContainer(oldContainer);
      }
   }

   public void start()
   {
   }

   public void stop()
   {
   }

}
