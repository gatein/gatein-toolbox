package org.exoplatform.core.component.organization.initializer;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.filter.Filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;

/**
 * Filter will trigger listeners of {@link OrganizationListenersInitializerService} after login of user.
 * It can be configured to trigger listeners also for all groups of this user.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TriggerInitializerFilter implements Filter
{
   // Whether to check JCR folders when triggering listeners
   private boolean checkFolders;
   
   // If true, we will trigger listener not only for user, but also for all his groups.
   private boolean triggerListenersForGroups;
   
   private Log log = ExoLogger.getLogger(this.getClass());
   
   // Flag to ensure that we will trigger listeners only once per logged user.
   private static final String ATTR_FLAG_NAME = "TriggerInitializerFilter.flag"; 

   public Boolean isCheckFolders()
   {
      return checkFolders;
   }

   public void setCheckFolders(Boolean checkFolders)
   {
      this.checkFolders = checkFolders;
   }

   public Boolean isTriggerListenersForGroups()
   {
      return triggerListenersForGroups;
   }

   public void setTriggerListenersForGroups(Boolean triggerListenersForGroups)
   {
      this.triggerListenersForGroups = triggerListenersForGroups;
   }

   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
   {
      if (log.isTraceEnabled())
      {
         log.trace("filter in progress. checkFolders=" + checkFolders + ", TriggerListenersForGroups=" + triggerListenersForGroups);
      }

      HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

      //
      if (httpServletRequest.getRemoteUser() != null && httpServletRequest.getSession().getAttribute(ATTR_FLAG_NAME) == null)
      {
         // Find services with ExoContainer
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         OrganizationListenersInitializerService initializer = (OrganizationListenersInitializerService)container.getComponentInstanceOfType(OrganizationListenersInitializerService.class);
         OrganizationService orgService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);

         // Encapsulate with RequestLifeCycle
         RequestLifeCycle.begin(container);
         try
         {
            // Find user and trigger listeners for him
            String username = httpServletRequest.getRemoteUser();
            User user = orgService.getUserHandler().findUserByName(username);
            initializer.treatUser(user, checkFolders);
            
            // This means that we will trigger listeners for all groups of particular user as well
            if (triggerListenersForGroups)
            {
               Collection<Group> groups = orgService.getGroupHandler().findGroupsOfUser(username);
               for (Group group : groups)
               {
                  initializer.treatGroup(group, checkFolders);
               }               
            }
            
            // Put flag to indicate, that this session is "processed"
            httpServletRequest.getSession().setAttribute(ATTR_FLAG_NAME, "true");
         }
         catch (Exception e)
         {
            log.error("Error occured in TriggerInitializerFilter", e);
         }
         finally
         {
            RequestLifeCycle.end();
         }
      }
      
      // Continue with filter chain
      filterChain.doFilter(servletRequest, servletResponse);
   }
}
