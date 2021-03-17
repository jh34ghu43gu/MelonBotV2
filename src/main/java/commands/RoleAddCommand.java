package commands;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.entities.Role;
import util.EmoteHelper;

public class RoleAddCommand extends Command {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(UserAddCommand.class);
	private ServerManager sm;
	
	public RoleAddCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "addRole";
		this.help = "Add a role to the reaction list.";
		this.arguments = "<user>";
		this.requiredRole = "melon";
	}

	@Override
	protected void execute(CommandEvent event) {
		
		//See if they provided a name
		if(event.getArgs().isEmpty())
        {
            event.replyError("Please mention a role to add.");
            return;
        }
		
		//Split args in case there is an attached emote
		String[] args = event.getArgs().split(" ");
		for(String s : args) {
			log.debug("RoleAdd command arg: " + s);
		}
		
		//check for emote
		String emote = "default";
		if(args.length > 1) {
			//Check if emote is an emote
			if(EmoteHelper.isEmote(emote)) {
				emote = EmoteHelper.getEmoteString(args[1]);
			} else {
				event.replyError("Invalid emote. Please specify a default emoji or a custom emote from this server.");
				return;
			}
		}
		//Check if emote is in the server
		if(emote.startsWith("<")) {
			if(event.getGuild().getEmoteById(EmoteHelper.getCustomEmoteID(emote)) == null) {
				event.replyError("Custom emotes must be from the current server.");
				return;
			}
		}
		
		//Get role
		List<Role> roles;
		if(event.getMessage().getMentionedRoles().size() > 0) {
			roles = event.getMessage().getMentionedRoles();
		} else {
			roles = event.getGuild().getRolesByName(args[0], true);
		}
		
		//Check size of roles and send to server manager if ok
		if(roles.size() == 0) {
			event.replyError("Please mention a role to add.");
		} else if(roles.size() > 1) {
			event.replyError("Please only mention 1 role at a time.");
		} else {
			if(sm.addRoleToServer(roles.get(0).getId(), event.getGuild().getId(), emote)) {
				if(emote.startsWith("<")) {
					event.reply("Successfully added role " + roles.get(0).getName() + " with " + emote + " emote.");
				} else {
					event.reply("Successfully added role " + roles.get(0).getName() + " with " + EmoteHelper.getEmoteUnicode(emote) + " emote.");
				}
			} else {
				event.replyError("Failed to add role, is it already added?");
			}
		}
	}

}
