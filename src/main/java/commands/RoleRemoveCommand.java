package commands;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.entities.Role;

public class RoleRemoveCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(RoleRemoveCommand.class);
	private ServerManager sm;
	
	public RoleRemoveCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "removeRole";
		this.help = "Remove a role from the reaction list.";
		this.arguments = "<role> [role] [role]...";
		this.requiredRole = "melon";
	}

	@Override
	protected void execute(CommandEvent event) {
		//See if they provided a name
		if(event.getArgs().isEmpty())
        {
            event.replyError("Please mention a role to remove.");
            return;
        }
		
		//Split args into array, unnecessary if they only use mentions 
		String[] args = event.getArgs().split(" ");
		for(String s : args) {
			log.debug("RoleRemove command arg: " + s);
		}
		
		//Make a list of roles
		List<Role> roles = new ArrayList<Role>();
		if(event.getMessage().getMentionedRoles().size() > 0) {
			roles = event.getMessage().getMentionedRoles();
		} else {
			//To avoid running getRolesByName a fuckton of times we will limit users to 5 per command unless they want to mention them all.
			if(args.length > 5 ) {
				event.replyError("Please only attempt to remove up to 5 roles at a time if you are not mentioning them.");
			}
			for(String s : args) {
				for(Role r : event.getGuild().getRolesByName(s, true)) {
					roles.add(r);
				}
			}
		}
		
		//Check if we got any valid roles
		if(roles.size() > 0) {
			//Send to ServerManager
			for(Role r : roles) {
				if(sm.removeRoleFromServer(r.getId(), event.getGuild().getId())) {
					event.reply("Succesfully removed role " + r.getName());
				} else {
					event.replyError("Unable to remove role " + r.getName() + ". Was role added in the first place?");
				}
			}
		} else {
			event.replyError("Please specify a valid role(s).");
			return;
		}
		
		
		
	}

}
