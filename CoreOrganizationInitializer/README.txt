What is CoreOrganizationInitializer
--------------------------------------

CoreOrganizationInitializer is plugin, which can be deployed to GateIn (EPP). It's useful in situations, when you
are creating users and groups externally (without GateIn UI and without Organization API). It's the case especially when
you are directly adding users or groups to your LDAP server through ldif files or directly into your DB through SQL.

Thing is, that when you are adding users and groups through Organization API, it triggers some necessary listeners, which are creating
additional needed objects in JCR for each user and group. But when you are adding your users and groups externally, these listeners are
obviously not triggered. This may later cause problems as JCR objects are not created.

Purpose of CoreOrganizationInitializer is to trigger all necessary listeners for objects, which were created externally.


How to build and deploy CoreOrganizationInitializer
---------------------------------------------------

1) Download sources from GateIn toolbox https://github.com/gatein/gatein-toolbox/tree/master/CoreOrganizationInitializer

2) Configure it in file src/main/java/conf/portal/configuration.xml (More info about configuration options below)

3) Build with: mvn clean install

4) Deploy JAR file target/exo.toolbox.core.component.organization.initializer-2.0-SNAPSHOT.jar into GATEIN_HOME/server/default/deploy/gatein.ear/lib/


Operations
----------

There are 3 important operations, which can be used:
1) treatUser(String username, boolean checkFolders) - This will run trigger listeners for particular user. Parameter "checkFolders" is used
to check if some JCR folders are already in place for this particular user. If they are in place, listeners won't be triggered
as that means that listeners were previously already triggered for this user. When "checkFolders" is false, it won't check folders.
So you can use "checkFolders" with value false if you really want to enforce running all listeners again for particular user.
Usually it's not needed and recommended value is "true".

2) treatGroup(String groupName, boolean checkFolders) - This will trigger listeners for particular group. Parameter "checkFolders" has same purpose
as for users.

3) launchAll(boolean checkFolders) - This operation will find all users and it will trigger "treatUser" operation for every user.
It will also find all groups and trigger "treatGroup" for every group.
WARNING:This operation should be used with care, especially if you have really big number of users and groups as it may be time consuming and
can have significant impact on performance.
And especially it requires double-care when using with checkFolders=false, as this will really enforce running all listeners for all users
and groups, so performance impact can be even bigger.


How to trigger operations
-------------------------

There are few possibilities:

1) GateIn startup:
During each startup of GateIn, there is automatically triggered operation "launchAll" for each deployed portal container.

As suggested above, this operation can have significant performance impact if you have big number of users and groups and boot
of your portal can be time consuming. So you may want to disable triggering this operation during startup. You can do it by disable parameter
"executeAllListenersDuringBoot" in file src/main/java/conf/portal/configuration.xml this way:

          <value-param>
             <name>executeAllListenersDuringBoot</name>
             <value>false</value>
          </value-param>



2) Login of user

There is possibility to trigger operation "treatUser" for particular user after login of this user. This is really cool and recommended way,
as objects are created on demand for this user and it's not needed to sync in other way. If you want to disable it (not recommended) you can do it
by commenting whole part with ExtensibleFilter. There are some configuration parameters of ExtensibleFilter:
- checkFolders - already described above

- triggerListenersForGroups - If true, it will check groups of the user and it will call "treatGroup" for every group of user. If false, groups are not checked.
Default and recommended value of "triggerListenersForGroups" param is true.


3) Trigger operations on demand through JMX
You can trigger operations through JMX this way:
- Go to http://localhost:8080/jmx-console (supported only on JBoss)
- In domain "exo" look for MBean name=OrganizationInitializerService,portal="portal",service=OrganizationInitializerService
or for MBean name=plidmcache,portal="ecmdemo",service=PicketLinkIDMCacheService if you are using EPP-SP and portal container "ecmdemo"
(or other name if you have completely different portal container)

Now you should see all operations "launchAll", "treatUser", "treatGroup", which you can trigger.


4) Trigger operations on demand through REST interface:

Some examples how to trigger REST operations within your web browser:

http://localhost:8080/rest/initializer/launchAllListeners/true (This will trigger "launchAll" for portal container "portal" with checkFolders=true)
http://localhost:8080/rest-ecmdemo/initializer/launchAllListeners/true (Same as previous but for portal container "ecmdemo")
http://localhost:8080/rest-ecmdemo/initializer/launchAllListeners/false (Same as previous but checkFolders=false)

http://localhost:8080/rest/initializer/launchUserListeners/jduke/true (Trigger "treatUser" for user "jduke" on portal container "portal" with checkFolders=true)
http://localhost:8080/rest-ecmdemo/initializer/launchUserListeners/jduke/true (Same as previous for portal container "ecmdemo")

http://localhost:8080/rest/initializer/launchGroupListeners/@platform@users/true (Trigger "treatGroup" for group "/platform/users" for portal container portal.
You can notice that we need to use character '@' in URL instead of '/' for group names.

http://localhost:8080/rest-ecmdemo/initializer/launchGroupListeners/@acme@roles@employees/true (Group "/acme/roles/employee" in portal container "ecmdemo" )


5) Job scheduler

There is configured Job scheduler in src/main/java/conf/portal/configuration.xml, which will periodically trigger operation "launchAll".
You can comment this part if you want to disable this periodic job (might be useful for save some performance)
