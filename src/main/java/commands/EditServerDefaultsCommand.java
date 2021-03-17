package commands;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import util.EmoteHelper;

public class EditServerDefaultsCommand extends Command {

	private static final Logger log = (Logger) LoggerFactory.getLogger(EditServerDefaultsCommand.class);
	
	private ServerManager sm;
	
	public EditServerDefaultsCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "editServer";
		this.help = "Edit a server default emote. \n"
				+ "Default emotes have a 1 in 100 chance of appearing on any message. \n"
				+ "Random emotes have a 1 in 10,000 chance of appearing. \n"
				+ "Rare emotes have a 1 in 1 million chance of appearing.";
		this.arguments = "<default | random | rare> <emote>";
		this.requiredRole = "melon";
	}
	
	@Override
	protected void execute(CommandEvent event) {
		if(event.getArgs().isEmpty())
        {
            event.replyError("Please say which default you would like to change (default | random | rare) and the new emote.");
            return;
        }
		
		//Split args into array
		String[] args = event.getArgs().split(" ");
		for(String s : args) {
			log.debug("Edit server defaults command arg: " + s);
		}
		
		//Which one are they editing?
		String edit = "";
		if(args[0].equalsIgnoreCase("default")) {
			edit = "default";
		} else if(args[0].equalsIgnoreCase("random")) {
			edit = "random";
		} else if(args[0].equalsIgnoreCase("rare")) {
			edit = "rare";
		} else {
			event.replyError("Invalid default type: valid options are 'default' 'random' or 'rare'.");
			return;
		}
		
		//Check emote validity
		String emote = "";
		if(args.length > 1) {
			//Check if emote is an emote
			if(EmoteHelper.isEmote(args[1])) {
				emote = EmoteHelper.getEmoteString(args[1]);
				//One case where off and none are not valid so throw those out here.
				if(emote.equalsIgnoreCase("none") || emote.equalsIgnoreCase("off") || emote.equalsIgnoreCase("default") || emote.length() == 0) {
					event.replyError("Invalid emote. Please specify a default emoji, custom emote from this server.");
					log.debug("Tried to edit server default to none/off.");
					return;
				}
			} else {
				event.replyError("Invalid emote(" + args[1] + "). Please specify a default emoji, custom emote from this server.");
				log.debug("Tried to edit server default to invalid emote.");
				return;
			}
		} else {
			event.replyError("Please specify an emote.");
			return;
		}
		
		//Emote is good, edit is good, send it away.
		if(sm.editServerDefault(event.getGuild().getId(), edit, emote)) {
			event.reply("Edited server '" + edit + "' to " + EmoteHelper.getEmoteUnicode(emote) + ".");
		} else {
			event.replyError("Failed to edit server '"+ edit + "' to " + EmoteHelper.getEmoteUnicode(emote) + ", please try again later.");
		}
		
		
	}

}
