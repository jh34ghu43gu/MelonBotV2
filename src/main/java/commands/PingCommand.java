package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends Command{
	
	public PingCommand() {
		this.name = "ping";
		this.help = "Responds with pong.";
		this.guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Pong");
	}

}
