package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import files.database.ServerManager;
import net.dv8tion.jda.api.entities.Member;

public class UserRemoveCommand extends Command {
	//private static final Logger log = (Logger) LoggerFactory.getLogger(UserAddCommand.class);
	private ServerManager sm;
	
	public UserRemoveCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "removeUser";
		this.help = "Remove a user from the reaction list.";
		this.arguments = "<user> [user] [user]...";
		this.requiredRole = "melon";
	}

	@Override
	protected void execute(CommandEvent event) {
		//See if they provided a name
		if(event.getArgs().isEmpty())
        {
            event.replyError("Please mention a user to remove.");
            return;
        }
		
		//See if it's a mention
		
		if(event.getMessage().getMentionedMembers().size() == 0) {
			event.replyError("Please mention a user to remove.");
            return;
		} else {
			for(Member m : event.getMessage().getMentionedMembers()) {
				if(sm.removeUserFromServer(m.getId(), event.getGuild().getId())) {
					event.reply(m.getAsMention() + " was removed.");
				} else {
					if(m.getNickname() != null) {
						event.replyError("Unable to remove " + m.getNickname() + ", were they added in the first place?");
					} else {
						event.replyError("Unable to remove " + m.getUser().getName() + ", were they added in the first place?");
					}
				}
			}
		}
	}
}
