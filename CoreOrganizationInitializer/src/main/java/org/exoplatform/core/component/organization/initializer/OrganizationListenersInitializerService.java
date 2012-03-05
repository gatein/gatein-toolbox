package org.exoplatform.core.component.organization.initializer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.picocontainer.Startable;

public class OrganizationListenersInitializerService implements Startable {

    private Map<String, UserEventListener> userDAOListeners_;
    private Map<String, GroupEventListener> groupDAOListeners_;
    private Map<String, MembershipEventListener> membershipDAOListeners_;
    private Map<String, UserProfileEventListener> userProfileListeners_;
    private OrganizationService organizationService;
    private RepositoryService repositoryService;
    private boolean executeAllListenersDuringBoot;
    private Log log = ExoLogger.getLogger(this.getClass());

    public OrganizationListenersInitializerService(OrganizationService organizationService, RepositoryService repositoryService, ConfigurationManager manager, InitParams initParams) {
        this.organizationService = organizationService;
        this.repositoryService = repositoryService;
        userDAOListeners_ = new HashMap<String, UserEventListener>();
        groupDAOListeners_ = new HashMap<String, GroupEventListener>();
        membershipDAOListeners_ = new HashMap<String, MembershipEventListener>();
        userProfileListeners_ = new HashMap<String, UserProfileEventListener>();
        boolean hasExternalComponentPlugins = false;
        int nbExternalComponentPlugins = 0;
        try {
            ExternalComponentPlugins organizationServiceExternalComponentPlugins = manager.getConfiguration().getExternalComponentPlugins(OrganizationListenersInitializerService.class.getName());

            if (organizationServiceExternalComponentPlugins != null && organizationServiceExternalComponentPlugins.getComponentPlugins() != null) {
                nbExternalComponentPlugins = organizationServiceExternalComponentPlugins.getComponentPlugins().size();
            }

            Component organizationServiceComponent = manager.getComponent(OrganizationListenersInitializerService.class);

            if (organizationServiceComponent != null && organizationServiceComponent.getComponentPlugins() != null) {
                nbExternalComponentPlugins += organizationServiceComponent.getComponentPlugins().size();
            }
            hasExternalComponentPlugins = (nbExternalComponentPlugins > 0);
        } catch (Exception e) {
            log.error("Test if this component has ExternalComponentPlugins generated an exception, cause:" + e.getMessage());
        }

        if (!hasExternalComponentPlugins) {
            try {
                ExternalComponentPlugins organizationServiceExternalComponentPlugins = manager.getConfiguration().getExternalComponentPlugins(OrganizationService.class.getName());
                addComponentPlugin(this, organizationServiceExternalComponentPlugins.getComponentPlugins(), ExoContainerContext.getCurrentContainer());

                Component organizationServiceComponent = manager.getComponent(OrganizationService.class);
                List<org.exoplatform.container.xml.ComponentPlugin> organizationServicePlugins = organizationServiceComponent.getComponentPlugins();
                if (organizationServicePlugins != null) {
                    addComponentPlugin(this, organizationServicePlugins, ExoContainerContext.getCurrentContainer());
                }
            } catch (Exception e) {
                log.error("Failed to add OrganizationService plugins to the OrgSrvInitializer ");
            }
        } else {
            log.info("This component has already " + nbExternalComponentPlugins + " ExternalComponentPlugins");
        }
        OrganizationInitializerUtils.REPOSITORY = initParams.getValueParam("repository").getValue();
        OrganizationInitializerUtils.WORKSPACE = initParams.getValueParam("workspace").getValue();
        OrganizationInitializerUtils.HOME_PATH = initParams.getValueParam("homePath").getValue();
        this.executeAllListenersDuringBoot =  Boolean.parseBoolean(initParams.getValueParam("executeAllListenersDuringBoot").getValue());
    }

    private static final Comparator<org.exoplatform.container.xml.ComponentPlugin> COMPARATOR = new Comparator<org.exoplatform.container.xml.ComponentPlugin>() {
        public int compare(org.exoplatform.container.xml.ComponentPlugin o1, org.exoplatform.container.xml.ComponentPlugin o2) {
            return o1.getPriority() - o2.getPriority();
        }
    };

    @SuppressWarnings("unchecked")
    private void addComponentPlugin(Object component, List<org.exoplatform.container.xml.ComponentPlugin> plugins, ExoContainer container) throws Exception {
        if (plugins == null)
            return;
        Collections.sort(plugins, COMPARATOR);
        for (org.exoplatform.container.xml.ComponentPlugin plugin : plugins) {

            try {
                Class clazz = Class.forName(plugin.getType());
                ComponentPlugin cplugin = (ComponentPlugin) container.createComponent(clazz, plugin.getInitParams());
                cplugin.setName(plugin.getName());
                cplugin.setDescription(plugin.getDescription());
                clazz = component.getClass();

                Method m = getSetMethod(clazz, plugin.getSetMethod());
                Object[] params = {cplugin};
                m.invoke(component, params);

                cplugin.setName(plugin.getName());
                cplugin.setDescription(plugin.getDescription());
            } catch (Exception ex) {
                log.error("Failed to instanciate plugin " + plugin.getName() + "for component " + component + ": " + ex.getMessage(), ex);
            }
        }
    }

    private Method getSetMethod(Class clazz, String name) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (name.equals(m.getName())) {
                Class[] types = m.getParameterTypes();
                if (types != null && types.length == 1 && ComponentPlugin.class.isAssignableFrom(types[0])) {
                    return m;
                }
            }
        }
        return null;
    }

    public void start() 
    {
      if (executeAllListenersDuringBoot)
      {
         log.info("Going to execute listeners for all users and groups");
         RequestLifeCycle.begin(PortalContainer.getInstance());

         try
         {
            // TODO: Make checkFolders configurable through startup parameters? Or try existence of JCR workspace?
            boolean checkFolders = true;

            this.launchAll(checkFolders);
         }
         finally
         {
            RequestLifeCycle.end();
         }
      }
      else
      {
         log.info("Skipped executing of listeners");
      }
    }


   /**
    * @param checkFolders whether to check folders in OrganizationInitializerUtils or not. If folders are not checked, we simply trigger all listeners for users and groups.
    * @return true if no error occured and everything has been created correctly.
    */
    public boolean launchAll(boolean checkFolders) {

      boolean ok = true;
    	try {
            log.info("Launch OrganizationListenersInitializerService with checkFolders=" + checkFolders);
          
            if (checkFolders)
            {
              Session session = repositoryService.getRepository(OrganizationInitializerUtils.REPOSITORY).getSystemSession(OrganizationInitializerUtils.WORKSPACE);
              OrganizationInitializerUtils.init(session);
            }
            
            PageList pageList = organizationService.getUserHandler().findUsers(new Query());
            int pagesNumber = pageList.getAvailablePage();
            for (int i = 1; i <= pagesNumber; i++) {
                List users = pageList.getPage(i);
                if (log.isDebugEnabled()) {
                    log.debug("Number of users = " + users.size());
                }

                for (Object objectUser : users) {
                    User user = (User) objectUser;
                    boolean userOk = treatUser(user, checkFolders);

                    if (!userOk)
                    {
                       ok = false;
                    }
                }
            }

            Collection groups = organizationService.getGroupHandler().getAllGroups();
            if (log.isDebugEnabled()) {
                log.debug("Groups = " + groups.size());
            }
            for (Object objectGroup : groups) {
                Group group = (Group) objectGroup;
                boolean groupOk = treatGroup(group, checkFolders);

                if (!groupOk)
                {
                   ok = false;
                }
            }
            log.info("OrganizationListenersInitializerService launched successfully!");
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            log.error(writer.toString());
            ok = false;
        }

        return ok;
    }


    public void stop() {
    }


   /**
    * 
    * @param group
    * @param checkFolders whether to check folders in OrganizationInitializerUtils or not. If folders are not checked, we simply trigger all listeners for group.
    * @return true if no error occured and everything has been created correctly.
    */
    public boolean treatGroup(Group group, boolean checkFolders) {

       if (group == null)
       {
          log.error("Group is null. This group likely does not exist.");
          return false;
       }

       boolean ok = true;
       if (log.isDebugEnabled())
       {
         log.debug("Initialize Group listener : " + group.getId() + ", checkFolders=" + checkFolders);
       }
       
    	 try {
             if (!checkFolders || !OrganizationInitializerUtils.hasGroupFolder(repositoryService, group)) {
                 log.info("Group = " + group.getId());
                 Collection<GroupEventListener> groupDAOListeners = groupDAOListeners_.values();
                 for (GroupEventListener groupEventListener : groupDAOListeners) {
                     try {
                         groupEventListener.preSave(group, true);
                         groupEventListener.postSave(group, true);
                     } catch (Exception e) {e.printStackTrace();
                         log.warn("Failed to initialize " + group.getId() + " Group, listener = " + groupEventListener.getClass());
                         log.debug(e);
                         ok = false;
                     }
                 }
             }
         } catch (Exception e) {
             log.warn("Failed to initialize " + group.getId() + " Group listeners ", e);
             ok = false;
         }       
    	
         return ok;
    }

   /**
    *
    * @param user
    * @param checkFolders whether to check folders in OrganizationInitializerUtils or not. If folders are not checked, we simply trigger all listeners for user, userProfile and all his memberships.
    * @return true if no error occured and everything has been created correctly.
    */    
    public boolean treatUser (User user, boolean checkFolders) {
      if (user == null)
      {
         log.error("User is null. This user likely does not exist.");
         return false;
      }

      boolean ok = true;
       
    	if (user.getCreatedDate() == null) {
            user.setCreatedDate(new Date());
        }
        if (log.isDebugEnabled()) {
            log.debug("Initialize User listener : " + user.getUserName() + ", checkFolders=" + checkFolders);
        }

        try {
            if (!checkFolders || !OrganizationInitializerUtils.hasUserFolder(repositoryService, user)) {
                log.info("User loaded ======> " + user.getUserName());
                Collection<UserEventListener> userDAOListeners = userDAOListeners_.values();
                for (UserEventListener userEventListener : userDAOListeners) {
                    try {                        
                        userEventListener.preSave(user, true);
                        userEventListener.postSave(user, true);
                    } catch (Exception e) {
                        log.warn("Failed to initialize " + user.getUserName() + " User with listener : " + userEventListener.getClass());
                        log.debug(e);
                        ok = false;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize " + user.getUserName() + " User", e);
            ok = false;
        }

        UserProfile userProfile = null;
        try
        {
            userProfile = organizationService.getUserProfileHandler().findUserProfileByName(user.getUserName());

            // TODO: Is this really needed? NewUserEventListener called for this user in previous step, should take care of creating UserProfile if it does not exists.
            if (userProfile == null) {
                  userProfile = organizationService.getUserProfileHandler().createUserProfileInstance(user.getUserName());
                  organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);
                  userProfile = organizationService.getUserProfileHandler().findUserProfileByName(user.getUserName());
            }
        }
        catch (Exception e)
        {
           log.warn("Failed to load user profile for " + user.getUserName() + " User", e);
           ok = false;
        }

        try {
            if (!checkFolders || !OrganizationInitializerUtils.hasProfileFolder(repositoryService, userProfile)) {
                log.info("User profile loaded ======> " + user.getUserName());
                Collection<UserProfileEventListener> userProfileListeners = userProfileListeners_.values();
                for (UserProfileEventListener userProfileEventListener : userProfileListeners) {
                    try {
                        if(userProfile.getUserInfoMap() == null) {
                            userProfile.setUserInfoMap(new HashMap<String, String>());
                        }
                        userProfileEventListener.preSave(userProfile, true);
                        userProfileEventListener.postSave(userProfile, true);
                    } catch (Exception e) {
                        log.warn("Failed to initialize " + user.getUserName() + " User profile with listener : " + userProfileEventListener.getClass());
                        log.debug(e);
                        ok = false;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize " + user.getUserName() + " User profile", e);
            ok = false;
        }

        try {
            Collection memberships = organizationService.getMembershipHandler().findMembershipsByUser(user.getUserName());
            if (log.isDebugEnabled()) {
                log.debug("Memberships = " + memberships.size());
            }
            for (Object objectMembership : memberships) {
                Membership membership = (Membership) objectMembership;
                try {
                    if (!checkFolders || !OrganizationInitializerUtils.hasMembershipFolder(repositoryService, membership)) {
                        log.info("Membership loaded = " + membership.getId());
                        Collection<MembershipEventListener> membershipDAOListeners = membershipDAOListeners_.values();
                        for (MembershipEventListener membershipEventListener : membershipDAOListeners) {
                            try {
                                membershipEventListener.preSave(membership, true);
                                membershipEventListener.postSave(membership, true);
                            } catch (Exception e) {
                                log.warn("Failed to initialize " + user.getUserName() + " Membership (" + membership.getId() + ") listener = " + membershipEventListener.getClass());
                                log.debug(e);
                                ok = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to initialize " + user.getUserName() + " Membership (" + membership.getId() + ")");
                    log.debug(e);
                    ok = false;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to initialize " + user.getUserName() + " Memberships listeners", e);
            ok = false;
        }
       
        return ok;
    }
    
    public synchronized void addListenerPlugin(ComponentPlugin listener) throws Exception {
        if (listener instanceof UserEventListener)
            userDAOListeners_.put(listener.getName(), (UserEventListener) listener);
        else if (listener instanceof GroupEventListener)
            groupDAOListeners_.put(listener.getName(), (GroupEventListener) listener);
        else if (listener instanceof MembershipEventListener)
            membershipDAOListeners_.put(listener.getName(), (MembershipEventListener) listener);
        else if (listener instanceof UserProfileEventListener)
            userProfileListeners_.put(listener.getName(), (UserProfileEventListener) listener);
    }
}