package org.exoplatform.core.component.organization.initializer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
import org.exoplatform.services.rest.resource.ResourceContainer;

@Managed
@ManagedDescription("Organization Listeners Initializer Service Endpoint")
@NameTemplate({
      @Property(key = "name", value = "OrganizationInitializerService"),
      @Property(key = "service", value = "OrganizationInitializerService")
})
@Path("/initializer/")
public class RestOrganizationInitializer implements ResourceContainer {

  private OrganizationListenersInitializerService initializerService;
  private Log log = ExoLogger.getLogger(this.getClass());
  private OrganizationService orgService_;


  public RestOrganizationInitializer(
			OrganizationListenersInitializerService organizationListenersInitializerService, OrganizationService orgService) {
		this.initializerService = organizationListenersInitializerService;
		this.orgService_=orgService;
  }


   /**
    * Use with care!!! It's calling listeners for all users and all groups in whole organization.
    * @return rest response
    */
   @Managed
   @ManagedDescription("Launch all listeners")
   @Impact(ImpactType.WRITE)
   @GET
	@Path("launchAllListeners/{checkFolders}")
	public Response launchAllListeners(@ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") @PathParam("checkFolders")Boolean checkFolders) {
	    log.info("Launch All listeners with checkFolders=" + checkFolders);
	    initializerService.launchAll(checkFolders);
		 return Response.ok().build();
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
   @GET
	@Path("launchUserListeners/{userName}/{checkFolders}")
	public String launchUserListeners(@ManagedDescription("userName") @ManagedName("userName") @PathParam("userName") String userName,
                                       @ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") @PathParam("checkFolders")Boolean checkFolders) {
	    log.info("Launch listeners for user "+userName + ", checkFolders=" + checkFolders);
	    UserHandler userHandler = orgService_.getUserHandler();
	    User user;
		try {
			user = userHandler.findUserByName(userName);
			boolean ok = initializerService.treatUser(user, checkFolders);
         String responseString = ok ? "User listeners executed successfuly." : "Error occured during execution of user listeners.";
         return responseString;
		} catch (Exception e) {
			log.warn("Error with user "+userName, e);
         return "Error occured during execution of user listeners: " + e.getMessage();
		}
	}

   @Managed
   @ManagedDescription("Launch group listeners for single group")
   @Impact(ImpactType.WRITE)
   @GET
   @Path("launchGroupListeners/{groupName}/{checkFolders}")
   public String launchGroupListeners(@ManagedDescription("Group Name") @ManagedName("groupName") @PathParam("groupName") String groupName,
                                        @ManagedDescription("Check JCR Folders") @ManagedName("checkFolders") @PathParam("checkFolders")Boolean checkFolders)
   {
      // We simply can't use slash in rest url
      groupName = groupName.replaceAll("@", "/");

      log.info("Launch listeners for group " + groupName + ", checkFolders=" + checkFolders);
      GroupHandler groupHandler = orgService_.getGroupHandler();
      Group group;
      try
      {
         group = groupHandler.findGroupById(groupName);
         boolean ok = initializerService.treatGroup(group, checkFolders);
         String responseString = ok ? "Group listeners executed successfuly." : "Error occured during execution of group listeners.";
         return responseString;
      } catch (Exception e)
      {
         log.warn("Error with group " + groupName, e);
         return "Error occured during execution of group listeners: " + e.getMessage();
      }
   }

}



