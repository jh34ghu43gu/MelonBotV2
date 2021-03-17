package events;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import files.database.ServerManager;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerJoinEvent extends ListenerAdapter{

	private static final Logger log = (Logger) LoggerFactory.getLogger(ServerJoinEvent.class);
	
	private ServerManager sm;
	
	public ServerJoinEvent(ServerManager sm) {
		this.sm = sm;
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		String serverID = event.getGuild().getId();
		log.debug("Joined a new server, attempting to add...");
		sm.addServerByID(serverID);
	}
	
}
