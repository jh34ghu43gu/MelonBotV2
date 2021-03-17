package events;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerLeaveEvent extends ListenerAdapter{
	private static final Logger log = (Logger) LoggerFactory.getLogger(ServerLeaveEvent.class);
	
	private ServerManager sm;
	
	public ServerLeaveEvent(ServerManager sm) {
		this.sm = sm;
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		String serverID = event.getGuild().getId();
		log.debug("We have left a server, attempting to remove...");
		sm.deleteServerByID(serverID);
	}
}
