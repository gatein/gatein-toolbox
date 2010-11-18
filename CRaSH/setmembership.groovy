import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.User;

@Description("Create a membership between group and users")
public class setmembership extends org.crsh.command.BaseCommand<User, Void> {

  private static final int BATCH_SIZE = 16;

  @Option(name="-u",required=false,aliases=["--username"],usage="The username")
  def String username;

  @Option(name="-a",required=false,aliases=["--add"],usage="The membership to add")
  def List<String> add;

  public void execute(CommandContext<User, Void> context) throws ScriptException {

    // Obtain portal container
    def rootContainer = org.exoplatform.container.RootContainer.instance;
    def portalContainer = rootContainer.getPortalContainer("portal");

    // Obtain user service
    def orgService = portalContainer.getComponentInstanceOfType(org.exoplatform.services.organization.OrganizationService.class);
    def userHandler = orgService.userHandler;
    def groupHandler = orgService.groupHandler;
    def membershipHandler = orgService.membershipHandler;
    def membershipTypeHandler = orgService.membershipTypeHandler;

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
      if (username == null) {
        throw new ScriptException("No username specified");
      }
      RequestLifeCycle.begin(portalContainer);
      try {
        def user = userHandler.findUserByName(username);
        if (user == null)
          throw new ScriptException("Could not find user $username");
        users = [user];
      } finally {
        RequestLifeCycle.end();
      }
    }
    
    def iterator = users.iterator();
    out:
    while (true) {
      RequestLifeCycle.begin(portalContainer);
      try {
        for (int i = 0;i < BATCH_SIZE;i++) {
          if (!iterator.hasNext())
            break out;
          def user = iterator.next();
          for (int j = 0;j < groups.size;j++) {
            membershipHandler.linkMembership(user, groups[j], membershipTypes[j], true);
          }
          println("assigned user $user");
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
  
  private String[] parseExpression(def expr) {
    def matcher = expr =~ /^([^:]+):(.+)$/;
    if (!matcher.matches()) {
      throw new ScriptException("Invalid permission express $expr");
    }
    return [matcher[0][1],matcher[0][2]];
  }
}

