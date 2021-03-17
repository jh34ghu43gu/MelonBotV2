package commands;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.entities.Member;
import util.EmoteHelper;

public class UserAddCommand extends Command {

	private static final Logger log = (Logger) LoggerFactory.getLogger(UserAddCommand.class);
	private ServerManager sm;
	
	public UserAddCommand(ServerManager sm) {
		this.sm = sm;
		
		this.name = "addUser";
		this.help = "Add a user to the reaction list."
				+ "\n If a user never wants an emote reaction regardless of roles specify with 'none' or 'off' in place of an emote."
				+ "\n Note that this will not stop random reaction events.";
		this.arguments = "<user> [emote]";
		this.requiredRole = "melon";
	}
	
	@Override
    protected void execute(CommandEvent event)  {
		
		//See if they provided a name
		if(event.getArgs().isEmpty())
        {
            event.replyError("Please mention a user to add.");
            return;
        }
		
		//Split args in case there is an attached emote
		String[] args = event.getArgs().split(" ");
		for(String s : args) {
			log.debug("UserAdd command arg: " + s);
		}
		
		//clean a mention up
		if(args[0].startsWith("<@!")) {
			args[0] = args[0].substring(3, 21);
			log.debug("Modified arg0 = " + args[0]);
		} else {
			List<Member> mems = event.getGuild().getMembersByNickname(args[0], true);
			if(mems.size() > 0) {
				args[0] = mems.get(0).getId();
			} else {
				try {
					if(event.getGuild().getMemberByTag(args[0]) != null) {
						args[0] = event.getGuild().getMemberByTag(args[0]).getId();
					}
				} catch (IllegalArgumentException e) {
					event.replyError("Please mention a valid user to add.");
			        return;
				}
			}
			event.replyError("Please mention a valid user to add.");
            return;
		}
		Member m;
		//Make sure id is valid
		try { //try catch for invalid id's
			if(event.getGuild().getMemberById(args[0]) == null) {
				if(event.getMessage().getMentionedMembers().size() == 0) {
					event.replyError("Please mention a valid user to add.");
		            return;
				} else {
					m = event.getMessage().getMentionedMembers().get(0);
				}
			} else {
				m = event.getGuild().getMemberById(args[0]);
			}
			
			//Get the emote if there is one
			String emote = "default";
			if(args.length > 1) {
				//Check if emote is an emote
				if(EmoteHelper.isEmote(emote)) {
					emote = EmoteHelper.getEmoteString(args[1]);
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
			
			//Send it to the ServerManager
			if(sm.addUserToServer(m.getUser().getId(), event.getGuild().getId(), emote)) {
				if(emote.startsWith("<")) {
					event.reply("Added user " + m.getUser().getAsMention() + " with emote: " + emote);
				} else {
					event.reply("Added user " + m.getUser().getAsMention() + " with emote: " + EmoteHelper.getEmoteUnicode(emote));
				}
			} else {
				event.replyError("Something went wrong trying to add the user, do they already have an emote?");
			}
		} catch(NumberFormatException e) {
			log.warn("User tried to add someone without mentioning them.");
			event.replyError("Please mention a valid user to add.");
            return;
		}
		
	}

}
