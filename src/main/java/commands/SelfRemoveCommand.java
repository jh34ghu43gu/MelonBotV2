package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import files.database.ServerManager;

public class SelfRemoveCommand extends Command {

	private ServerManager sm;
	
	public SelfRemoveCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "removeMe";
		this.help = "Remove yourself from the reaction list.";
	}
	
	
	@Override
	protected void execute(CommandEvent event) {		
		if(sm.removeUserFromServer(event.getMember().getId(), event.getGuild().getId())) {
			event.reply("Removed you from the reaction list.");
		} else {
			event.replyError("Failed to remove you from the reaction list, were you added in the first place?");
		}
	}
}
