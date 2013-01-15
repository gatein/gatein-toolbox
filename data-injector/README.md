# GateIn Data Injector

GateIn Data Injector defines a set of service components used for generating dataset. Those services are exposed as JMX beans
and REST endpoints.

The tool has been tested only on GateIn 3.5.Final. There is no warranty that it works correctly on other releases of GateIn

# How to deploy

- Run _mvn clean install_ from current directory

- Copy _datainject-core_ artifact under target to _lib_ directory of GateIn's Tomcat packaging

# How to access data injector service components

Service components are exoposed as JMX beans and REST endpoints, which are accessible via JConsole, GateIn Management Gadget and HTTP GET requests

### JConsole

 Open the MBeans tab of JConsole being connected to JVM running GateIn. Injector services are available in _exo_ category under the names

  * _navdataInject_
  * _groupDataInject_
  * _pageInject_
  * _userDataInject_

### GateIn Management Gadget

 Log in GateIn and check the link _http://localhost:8080/portal/g/:platform:administrators/administration/servicesManagement_ to see GateIn management gadget. Open the GUI to interact with injector services by selecting one of following values in _Services_ select-box

 * _navInjector_
 * _userInjector_
 * _groupInjector_
 * _pageInjector_

### HTTP GET requests

 Data injector REST endpoints could be invoked by simple HTTP GET requests. URL for each usecase is given in succeeding sections

# Build dataset with data injector

### 1. Navigation data

#### _1.1. Create new navigations with page nodes_

* Method: createNavs
* Params: navType, navOwner, prefix, startIndex, endIndex

To create a new navigation named _testClassic_ of type _portal_ and inject 100 children named (testNode_0, testNode_1,..., testNode_99) to navigation's root node

* Invoking createNavs with navType=portal, navOwner=testClassic, prefix=testNode, startIndex=0, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/navInjector/createNavs?navType=portal&navOwner=testClassic&prefix=testNode&startIndex=0&endIndex=100_

#### _1.2. Insert nodes into node specified by path from root node of existing navigation_

* Method: insertNodes
* Params: navType, navOwner, absolutePath, prefix, startIndex, endIndex

To inject 100 children named (testNode_0, testNode_1,..., testNode_99) to node specified by path /level_0/level_1 under navigation (type=portal, owner=classic)

* Invoking insertNodes with navType=portal, navOwner=classic, absolutePath=/level_0/level_1, prefix=testNode, startIndex=0, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/navInjector/insertNodes?navType=portal&navOwner=testClassic&absolutePath=/level_0/level_1&prefix=testNode&startIndex=0&endIndex=100_

#### _1.3. Delete node specified by path from root node of existing navigation_

* Method: deleteNode
* Params: navType, navOwner, absolutePath

To delete node specified by path level_0/level_1 under navigation (type=portal, owner=classic)

* Invoking deleteNode with navType=portal, navOwner=classic, pathFromRoot=level_0/level_1
* Send HTTP GET request _http://localhost:8080/portal/rest/management/navInjector/deleteNode?navType=portal&navOwner=testClassic&pathFromRoot=level_0/level_1

### 2. Page data

#### _2.1. Create pages_

* Method: createPages
* Params: siteType, siteName, pageNamePrefix, pageTitlePrefix, startIndex, endIndex

To generate 100 pages(foo_0, foo_1,...,foo_99) whose ownerType is _portal_ and ownerId is _classic_

* Invoking createPages with siteType=portal, siteName=classic, pageNamePrefix=foo, pageTitlePrefix=foo_title, startIndex=0, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/pageInjector/createPages?siteType=portal&siteName=classic&pageNamePrefix=foo&pageTitlePrefix=foo_title&startIndex=0&endIndex=100_

#### _2.2. Remove pages_

* Method: createPages
* Params: siteType, siteName, pageNamePrefix, startIndex, endIndex

To remove 100 pages(foo_0, foo_1,...,foo_99) whose ownerType is _portal_ and ownerId is _classic_

* Invoking removePages with siteType=portal, siteName=classic, pageNamePrefix=foo, startIndex=0, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/pageInjector/removePages?siteType=portal&siteName=classic&pageNamePrefix=foo&startIndex=0&endIndex=100_

### 3. Organization data

#### _3.1. Create users_

* Method: createListUsers
* Params: userName, startIndex, endIndex, password

To created 100 users named (testUserName_1, testUserName_2, testUserName_3, ..., testUserName_100) whose password equals to test123 (or default: 123456 if not set)

* Invoking createListUsers with userName=testUserName, startIndex=1, endIndex=100, password=test123
* Send HTTP GET request _http://localhost:8080/portal/rest/management/userInjector/createListUsers?userName=testUserName&startIndex=1&endIndex=100&password=test123_

#### _3.2. Remove a list of users_

* Method: removeListUsers
* Params: userName, startIndex, endIndex

To remove 100 users named (testUserName_1, testUserName_2, testUserName_3, ..., testUserName_100) from Gatein

* Invoking removeListUsers with userName=testUserName, startIndex=1, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/userInjector/removeListUsers?userName=testUserName&startIndex=1&endIndex=100_

#### _3.3. Create membership types_

* Method: createMembershipsType
* Params: membershipName, startIndex, endIndex

To inject 100 membership types named (testMembershipName_1, testMembershipName_2, ..., testMembershipName_100) to Gatein

* Invoking createMembershipsType with membershipName=testMembershipName, startIndex=1, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/userInjector/createMembershipsType?membershipName=testMembershipName&startIndex=1&endIndex=100_

#### _3.4. Remove a list of membership types_

* Method: removeMembershipsType
* Params: membershipName, startIndex, endIndex

To remove 100 membership type named (testMembershipName_1, testMembershipName_2, ..., testMembershipName_100) from Gatein

* Invoking removeMembershipsType with membershipName=testMembershipName, startIndex=1, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/userInjector/removeMembershipsType?membershipName=testMembershipName&startIndex=1&endIndex=100_

#### _3.5. Create groups_

* Method: createGroups
* Params: groupName, parentName, startIndex, endIndex, createParent

To create 100 sub-groups named (testGroupName_1, testGroupName_2, ..., testGroupName_100) under group named _platform_. In case _platform_ group does not exist then create it

* Invoking createGroups with groupName=testGroupName, parentName=platform, startIndex=1, endIndex=100, createParent=true
* Send HTTP GET request _http://localhost:8080/portal/rest/management/groupInjector/createGroups?groupName=testGroupName&parentName=platform&startIndex=1&endIndex=100&createParent=true_

#### _3.6. Remove groups_

* Method: removeGroups
* Params: groupName, parentName, startIndex, endIndex, deleteParent

To remove 100 subgroups named (testGroupName_1, testGroupName_2, ..., testGroupName_100) from group named _platform_ without deleting _platform_ group

* Invoking removeGroups with groupName=testGroupName, parentName=platform, startIndex=1, endIndex=100, deleteParent=false
* Send HTTP GET request _http://localhost:8080/portal/rest/management/groupInjector/removeGroups?groupName=testGroupName&parentName=platform&startIndex=1&endIndex=100&deleteParent=false_

#### _3.7. Add memberships_

* Method: addMemberships
* Params: userName, groupName, membershipName, startIndex, endIndex

Notice: value of groupName must be in format: [/parentName]/groupName, such as /platform/users

To grant _member_ membership to 100 users named (testUserName_1, testUserName_2, ..., testUserName_100) of _platform_ group

* Invoking addMemberships with userName=testUserName, groupName=/platform, membershipName=member startIndex=1, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/groupInjector/addMemberships?groupName=/platform&userName=testUserName&membershipName=member&startIndex=1&endIndex=100_

#### _3.8. Remove memberships_

* Method: removeMemberships
* Params: userName, groupName, membershipName, startIndex, endIndex

Notice: value of groupName must be in format: [/parentName]/groupName, such as /platform/users

To revoke _member_ membership of 100 users named (testUserName_1, testUserName_2, ..., testUserName_100) of _/platform/administrators_ group.

* Invoking removeMemberships with userName=testUserName, groupName=/platform/administrators, membershipName=member startIndex=1, endIndex=100
* Send HTTP GET request _http://localhost:8080/portal/rest/management/groupInjector/removeMemberships?groupName=/platform/administrators&userName=testUserName&membershipName=member&startIndex=1&endIndex=100_
