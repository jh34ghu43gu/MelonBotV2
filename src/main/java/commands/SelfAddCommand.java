package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import files.database.ServerManager;
import net.dv8tion.jda.api.entities.Member;
import util.EmoteHelper;

public class SelfAddCommand extends Command {
	
	private ServerManager sm;
	
	public SelfAddCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "addMe";
		this.help = "Add yourself to the reaction list.";
		this.arguments = "[emote]";
	}

	@Override
	protected void execute(CommandEvent event) {
		//Get the emote if there is one
		String emote = "default";
		String args = event.getArgs();
		if( args.length() > 0) {
			//Check if emote is an emote
			if(EmoteHelper.isEmote(emote)) {
				emote = EmoteHelper.getEmoteString(args);
			} else {
				event.replyError("Invalid emote. Please specify a default emoji, custom emote from this server, or 'none'/'off'.");
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
		
		//Get the user
		Member m = event.getMember();
		
		//Send it to the ServerManager
		if(sm.addUserToServer(m.getUser().getId(), event.getGuild().getId(), emote)) {
			if(emote.startsWith("<")) {
				event.reply("Added you with emote: " + emote);
			} else {
				event.reply("Added you with emote: " + EmoteHelper.getEmoteUnicode(emote));
			}
		} else {
			event.replyError("Something went wrong trying to add you, do you already have an emote assigned?");
		}
		
	}

}
