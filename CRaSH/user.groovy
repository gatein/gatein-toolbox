import org.crsh.cmdline.IntrospectionException
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Required
import org.crsh.cmdline.annotations.Usage
import org.crsh.command.CRaSHCommand
import org.crsh.command.InvocationContext
import org.exoplatform.container.component.RequestLifeCycle
import org.exoplatform.container.ExoContainerContext
import org.exoplatform.container.ExoContainer

/**
 * @author <a href="henri.gomez@exoplatform.com">Henri Gomez</a>
 * @author <a href="julien.viet@exoplatform.com">Julien Viet</a>
 * @author <a href="thomas.delhomenie@exoplatform.com">Thomas Delhomenie</a>
 * @author <a href="thomas.delhomenie@exoplatform.com">Romain Dénarié</a>
 */
@Usage("user management")
class user extends CRaSHCommand {

    //
    private static final int DEFAULT_BATCH_SIZE = 100;

    /** . */
    @Usage("don't broadcast event")
    @Option(names=["n","nobroadcast"])
    def Boolean nobroadcast

    /** . */
    @Usage("the patch size (default 100)")
    @Option(names=["b","batchsize"])
    def Integer batchSize

    private rootContainer
    private portalContainer
    private orgService
    private userHandler
    private groupHandler
    private membershipHandler
    private membershipTypeHandler



    user() {
        // Obtain portal container
        rootContainer = org.exoplatform.container.RootContainer.instance
        portalContainer = rootContainer.getPortalContainer("portal")

        // Obtain user service
        orgService = portalContainer.getComponentInstanceOfType(org.exoplatform.services.organization.OrganizationService.class)

        userHandler = orgService.userHandler
        groupHandler = orgService.groupHandler
        membershipHandler = orgService.membershipHandler
        membershipTypeHandler = orgService.membershipTypeHandler
    }

    @Usage("add users in GateIn")
    @Command
    public void add(
    InvocationContext<Void, Void> context,
    @Usage("the number of users to generate")
    @Required
    @Argument
    int size,
    @Usage("the user base name")
    @Option(names=["u","username"])
    String baseName,
    @Usage("the user domain email")
    @Option(names=["d","domain"])
    String domainEmail,
    @Usage("the user password")
    @Option(names=["p","password"])
    String password,
    @Usage("membership")
    @Option(names=["m","membership"])
    List<String> initialMembership) throws ScriptException {

        //
        if (size < 1)
            throw new ScriptException("User size must be greater than zero: $size");
        if (batchSize == null)
           batchSize = DEFAULT_BATCH_SIZE;
        if (batchSize < 1)
           throw new ScriptException("Batch size must be greater than zero: $batchSize");
        if (password == null)
            password = "gtn";
		if (domainEmail == null)
			domainEmail = "example.com";
        //
        println("starting creation of $size users");
        boolean transacted = false;
        int count = 0;
        boolean done = false;
        while (!done) {
            try {
                for (int i = 0;i < batchSize;i++) {
                    if (!transacted) {
                        transacted = true;
                        RequestLifeCycle.begin(portalContainer);
                    }
                    def name = baseName + count;
                    def user = userHandler.createUserInstance(name);
                    user.password = password;
					user.setFirstName(name);
					user.setLastName(name);
					user.setEmail(name + "@" + domainEmail);
                    userHandler.createUser(user, !nobroadcast);
                    context.produce(user);
                    println("created user $name");

                    List<String> usernames = new ArrayList<String>()
                    usernames.add (name)
                    addMembership(context,usernames,initialMembership)

                    if (++count >= size) {
                        done = true;
                        break;
                    }
                }
            } finally {
                if (transacted) {
                    transacted = false;
                    RequestLifeCycle.end();
                }
            }
        }

        //
        println("$count users created");
        context.writer << "$count users created";

    }

    @Usage("add memberships to user")
    @Command
    public void addMembership(
    InvocationContext<Void, Void> context,
    @Usage("user names")
    @Argument
    List<String> usernames,
    @Usage("membership")
    @Option(names=["m","membership"])
    List<String> add) throws ScriptException {
	
	private oldContainer = ExoContainerContext.getCurrentContainer()
	ExoContainerContext.setCurrentContainer(portalContainer)

        //
        if (batchSize == null) {
           batchSize = DEFAULT_BATCH_SIZE;
        }
        if (batchSize < 1) {
           throw new ScriptException("Batch size must be greater than zero: $batchSize");
        }

        // Get group / membershiptype to add
        def groups = [];
        def membershipTypes = [];
        RequestLifeCycle.begin(portalContainer);
        try {
            add.each {
                def (membershipName,groupId) = parseExpression(it);
                def membershipType = membershipTypeHandler.findMembershipType(membershipName);
                if (membershipType == null)
                    throw new ScriptException("Membership type $membershipName not found");
                def group = groupHandler.findGroupById(groupId);
                if (group == null)
                    throw new ScriptException("Group $groupId not found");
                groups.add(group);
                membershipTypes.add(membershipType);
            }
        } finally {
            RequestLifeCycle.end();
        }

        def users;
        if (context.piped) {
            users = context.consume();
        } else {
            RequestLifeCycle.begin(portalContainer);
            try {
                users = [];
                usernames.each { username ->
                def user = userHandler.findUserByName(username);
                  if (user == null)
                      throw new ScriptException("Could not find user $username");
                  users.add(user);
                }
            } finally {
                RequestLifeCycle.end();
            }
        }

        def iterator = users.iterator();
        boolean transacted = false;
        boolean done = false;
        while (!done) {
            try {
                if (!transacted) {
                    transacted = true;
                    RequestLifeCycle.begin(portalContainer);
                }
                for (int i = 0;i < batchSize;i++) {
                    if (!iterator.hasNext()) {
                        done = true;
                        break;
                    }
                    def user = iterator.next();
                    for (int j = 0;j < groups.size;j++) {
                        membershipHandler.linkMembership(user, groups[j], membershipTypes[j], !nobroadcast);
                    }
                    println("assigned user $user");
                }
            } finally {
                if (transacted) {
                    transacted = false;
                    RequestLifeCycle.end();
                }
            }
        }
	ExoContainerContext.setCurrentContainer(oldContainer)
    }

    private String[] parseExpression(def expr) {
        def matcher = expr =~ /^([^:]+):(.+)$/;
        if (!matcher.matches()) {
            throw new ScriptException("Invalid permission express $expr");
        }
        return [matcher[0][1], matcher[0][2]];
    }
}
