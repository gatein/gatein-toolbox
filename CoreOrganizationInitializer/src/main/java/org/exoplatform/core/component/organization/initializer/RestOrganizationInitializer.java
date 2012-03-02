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

/**
 * REST Endpoint for accessing OrganizationListenersInitializerService stuff.
 */
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


   @GET
	@Path("launchAllListeners/{checkFolders}")
	public String launchAllListeners(@PathParam("checkFolders") Boolean checkFolders)
   {
	    boolean ok = initializerService.launchAll(checkFolders);
       String responseString = ok ? "All listeners executed successfuly." : "Error occured during execution of listeners.";
		 return responseString;
	}


   @GET
	@Path("launchUserListeners/{userName}/{checkFolders}")
	public String launchUserListeners(@PathParam("userName") String userName,
                                     @PathParam("checkFolders") Boolean checkFolders) {
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


   @GET
   @Path("launchGroupListeners/{groupName}/{checkFolders}")
   public String launchGroupListeners(@PathParam("groupName") String groupName,
                                      @PathParam("checkFolders") Boolean checkFolders)
   {
      // We simply can't use slash in rest url
      groupName = groupName.replaceAll("@", "/");

      GroupHandler groupHandler = orgService_.getGroupHandler();
      Group group;
      try
      {
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
   }

}
