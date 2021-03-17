package bot;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

import ch.qos.logback.classic.Logger;
import commands.EditServerDefaultsCommand;
import commands.PingCommand;
import commands.RoleAddCommand;
import commands.RoleRemoveCommand;
import commands.SelfAddCommand;
import commands.SelfRemoveCommand;
import commands.UserAddCommand;
import commands.UserRemoveCommand;
import events.MessageEvent;
import events.ServerJoinEvent;
import events.ServerLeaveEvent;
import files.ConfigHelper;
import files.database.Server;
import files.database.ServerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Bot.class);
	
	private static String LOGIN_TOKEN;
	private static JDA jda;
	
	public static void main(String[] args) throws Exception {
		log.info("Starting bot...");
		
		//Config options setup
		ConfigHelper CH = new ConfigHelper();
		//All config values go here
		CH.addOption("LOGIN_TOKEN", "Bot login token goes here");
		CH.addOption("prefix", "Bot command prefix goes here");
		CH.addOption("ownerID", "Discord bot maker ID goes here");
		CH.addOption("databaseConnection", "");
		CH.addOption("databaseUserName", "");
		CH.addOption("databasePassword", "");
		CH.addOption("defaultEmote", "watermelon");
		CH.addOption("randomEmote", "pineapple");
		CH.addOption("rareEmote", "ok_hand");
		if(!CH.exists()) { //file doesn't exist or the amount of options has changed
			CH.build();
		} else if (CH.size() != CH.fileSize()) {
			CH.copyFile();
			CH.build();
		} else {
			//This might trigger someone to write all those options then clear it but it was
			//the first way I thought to track our config changes so unless I can think of something better
			//it's going to have to work like this.
			CH.clear();
		}

		//We don't want to keep CH's hashmap loaded so we will pull directly from file
		LOGIN_TOKEN = CH.getOptionFromFile("LOGIN_TOKEN");
		String prefix = CH.getOptionFromFile("prefix");
		String ownerID = CH.getOptionFromFile("ownerID");
		
		String conn = CH.getOptionFromFile("databaseConnection");
		String connUser = CH.getOptionFromFile("databaseUserName");
		String connPassword = CH.getOptionFromFile("databasePassword");
		
		String defaultEmote = CH.getOptionFromFile("defaultEmote");
		String defaultRandom = CH.getOptionFromFile("defaultRandom");
		String defaultRare = CH.getOptionFromFile("defaultRare");
		
		//Grab servers
		ArrayList<Server> servers;
		try {
			log.debug("Bot startup attempting to fetch all servers.");
			servers = Server.fetchServers(conn, connUser, connPassword);
		} catch (Exception e1) {
			log.error("Failed to access DB on startup, terminating startup.");
			log.error(e1.getMessage());
			throw new Exception("Cannot reach DB");
		}
		
		ServerManager sm = new ServerManager(conn, connUser, connPassword, servers, defaultEmote, defaultRandom, defaultRare);
		ServerJoinEvent joinEvent = new ServerJoinEvent(sm);
		ServerLeaveEvent leaveEvent = new ServerLeaveEvent(sm);
		MessageEvent messageEvent = new MessageEvent(sm);
		
		CommandClientBuilder commandBuilder = new CommandClientBuilder();
        commandBuilder.setPrefix(prefix);
        commandBuilder.setOwnerId(ownerID);
        //Add commands here
        commandBuilder.addCommands(new PingCommand());
        commandBuilder.addCommands(new UserAddCommand(sm));
        commandBuilder.addCommands(new UserRemoveCommand(sm));
        commandBuilder.addCommands(new RoleAddCommand(sm));
        commandBuilder.addCommands(new RoleRemoveCommand(sm));
        commandBuilder.addCommands(new SelfAddCommand(sm));
        commandBuilder.addCommands(new SelfRemoveCommand(sm));
        commandBuilder.addCommands(new EditServerDefaultsCommand(sm));
        CommandClient commandClient = commandBuilder.build();
		
		JDABuilder builder = JDABuilder.createDefault(LOGIN_TOKEN);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
		//Register events here
		builder.addEventListeners(commandClient);
		builder.addEventListeners(joinEvent);
		builder.addEventListeners(leaveEvent);
		builder.addEventListeners(messageEvent);
		
		
		
		try {
			jda = builder.build();	
		} catch (Exception e) {
			log.error("Error logging in.");
			e.printStackTrace();
		} 
		
	}

}
