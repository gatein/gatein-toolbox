
import org.crsh.jcr.JCR;
import org.crsh.jcr.command.ContainerOpt;
import org.crsh.jcr.command.UserNameOpt;
import org.crsh.jcr.command.PasswordOpt;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Required;
import org.crsh.command.InvocationContext;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Usage("ExoCache management")
public class cache extends org.crsh.jcr.command.JCRCommand
{

   @Usage("Clean the specified cache.")
   @Command
   public Object clear(@Argument List<String> cacheNames) throws ScriptException
   {
      cacheNames.each
      {
         getCache(it).clearCache();
      }
      return "Done";
   }

   @Usage("Get the cache size.")
   @Command
   public Object size(@Argument List<String> cacheNames) throws ScriptException
   {
      String print = "";
      cacheNames.each
      {
         print += "$it : ${getCache(it).cacheSize}\n";
      }
      return print;
   }

   @Usage("Get the cache capacity.")
   @Command
   public Object capacity(@Argument List<String> cacheNames) throws ScriptException
   {
      String print = "";
      cacheNames.each
      {
         print += "$it : ${getCache(it).maxSize}\n";
      }
      return print;
   }

   @Usage("Get the cache live time.")
   @Command
   public Object time(@Argument List<String> cacheNames) throws ScriptException
   {
      String print = "";
      cacheNames.each
      {
         print += "$it : ${getCache(it).liveTime}\n";
      }
      return print;
   }

   private CacheService getCacheService()
   {
      PortalContainer container = PortalContainer.getInstance();
      return (CacheService) container.getComponentInstanceOfType(CacheService.class);
   }

   private ExoCache getCache(String cacheName)
   {
      return getCacheService().getCacheInstance(cacheName);
   }

}