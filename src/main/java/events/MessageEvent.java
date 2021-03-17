package events;

import java.util.List;
import java.util.Random;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import util.EmoteHelper;

public class MessageEvent extends ListenerAdapter {

	private static final Logger log = (Logger) LoggerFactory.getLogger(MessageEvent.class);
	private ServerManager sm;
	
	public MessageEvent(ServerManager sm) {
		this.sm = sm;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.TEXT))
        {
			User u = event.getAuthor();
			
			String userID = u.getId();
			String serverID = event.getGuild().getId();
			List<Role> roles = event.getMember().getRoles();
			
			//Can happen if bot is added while offline, just add server now.
			if(!sm.serverExists(serverID)) {
				sm.addServerByID(serverID);
			}
			
			//Check for emote
			String emote = sm.getUserEmoteForReacts(userID, serverID);
			boolean noEmote = false;
			//If they have an emote apply the emote
			if(emote.length() != 0) {
				if(emote.equalsIgnoreCase("none") || emote.equalsIgnoreCase("off")) {
					noEmote = true; //Set this for role check.
				} else {
					log.debug("User message has forced emote of: " + EmoteHelper.getEmoteString(emote) + ", attempting to add.");
					event.getMessage().addReaction(emote).queue();
				}
			}
			
			if(!noEmote) { //Make sure user isn't overriding roles.
				for(Role r : roles) {
					emote = sm.getRoleEmoteForReact(r.getId(), serverID);
					if(emote.length() != 0) {
						log.debug("User message has role forced emote of: " + EmoteHelper.getEmoteString(emote));
						event.getMessage().addReaction(emote).queue();
					}
				}
			}
			
			//Random emote logic
			Random rand = new Random();
			if(rand.nextInt(100) == 1) {
				event.getMessage().addReaction(sm.getServerRandom(serverID, "default")).queue();
				log.debug("Default random emote reaction fired.");
			}
			if(rand.nextInt(10000) == 1) {
				event.getMessage().addReaction(sm.getServerRandom(serverID, "default")).queue();
				event.getMessage().addReaction(sm.getServerRandom(serverID, "random")).queue();
				log.debug("Random random emote reaction fired.");
			}
			if(rand.nextInt(1000000) == 1) {
				event.getMessage().addReaction(sm.getServerRandom(serverID, "default")).queue();
				event.getMessage().addReaction(sm.getServerRandom(serverID, "random")).queue();
				event.getMessage().addReaction(sm.getServerRandom(serverID, "rare")).queue();
				log.debug("Rare random emote reaction fired.");
			}
			
        }
	}
}
