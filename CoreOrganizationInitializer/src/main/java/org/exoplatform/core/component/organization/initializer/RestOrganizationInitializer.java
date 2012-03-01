package org.exoplatform.core.component.organization.initializer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.rest.resource.ResourceContainer;

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
	@Path("launchAllListeners/")
	public Response launchAllListeners() {
	    log.info("Launch All listeners");
	    initializerService.launchAll();
		return Response.ok().build();
	}
  
  @GET
	@Path("launchUserListeners/{userName}")
	public Response launchUserListeners(@PathParam("userName") String userName) {
	    log.info("Launch listeners for user "+userName);
	    UserHandler userHandler = orgService_.getUserHandler();
	    User user;
		try {
			user = userHandler.findUserByName(userName);
			initializerService.treatUser(user);
		} catch (Exception e) {
			log.warn("Error with user "+userName);
		}
		return Response.ok().build();
	}

}



