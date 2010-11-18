import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.User;

@Description("Add users in GateIn")
public class addusers extends org.crsh.command.BaseCommand<Void, User> {

  private final int BATCH_SIZE = 16;

  @Option(name="-s",required=true,aliases=["--size"],usage="The number of users to generate")
  def int size;

  @Option(name="-n",required=true,aliases=["--name"],usage="The user base name")
  def String baseName;

  @Option(name="-p",aliases=["--password"],usage="The user password")
  def String password = "gtn";

  public void execute(CommandContext<Void, User> context) throws ScriptException {

    //
    if (size < 0) {
      throw new ScriptException("Size cannot be negative");
    }

    // Obtain portal container
    def rootContainer = org.exoplatform.container.RootContainer.instance;
    def portalContainer = rootContainer.getPortalContainer("portal");


    // Obtain user service
    def orgService = portalContainer.getComponentInstanceOfType(org.exoplatform.services.organization.OrganizationService.class);
    def userHandler = orgService.userHandler;

    //
    int count = 0;
    out:
    while (true) {
      RequestLifeCycle.begin(portalContainer);
      try {
        for (int i = 0;i < BATCH_SIZE;i++) {
          def name = baseName + count;
          def user = userHandler.createUserInstance(name);
          user.password = password;
          userHandler.createUser(user, true);
          context.produce(user);
          count++;
          println("created user $name");
          if (count >= size) {
            break out;
          }
         }
      } finally {
        RequestLifeCycle.end();
      }
    }

    //
    context.writer << "$count users created";
  }
}

