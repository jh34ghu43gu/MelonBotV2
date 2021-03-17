package test;

import java.util.ArrayList;

import com.vdurmont.emoji.EmojiManager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import files.ConfigHelper;
import files.database.Server;
import files.database.ServerManager;
import util.EmoteHelper;

public class Tests {
	
	//SETUP (adding all the main bot stuff so configs don't get replaced)
	String conn = "";
	String connUser = "";
	String connPassword = "";
	
	/*
	public String setup() {
		ConfigHelper CH = new ConfigHelper();
		CH.addOption("LOGIN_TOKEN", "Bot login token goes here");
		CH.addOption("prefix", "Bot command prefix goes here");
		CH.addOption("ownerID", "Discord bot maker ID goes here");
		CH.addOption("databaseConnection", "");
		CH.addOption("databaseUserName", "");
		CH.addOption("databasePassword", "");
		CH.addOption("defaultEmote", "watermelon");
		CH.addOption("randomEmote", "pineapple");
		CH.addOption("rareEmote", "ok_hand");
		if(!CH.exists()) {
			CH.build();
		} else if (CH.size() != CH.fileSize()) {
			CH.copyFile();
			CH.build();
		} else {
			CH.clear();
		}
		
		conn = CH.getOptionFromFile("databaseConnection");
		connUser = CH.getOptionFromFile("databaseUserName");
		return CH.getOptionFromFile("databasePassword");
	} */
	//END SETUP

	@Test
    public void testConfigHelperNullValue() {
        ConfigHelper CH = new ConfigHelper();
        
        assertDoesNotThrow(() -> CH.addOption("TestValue", null));
        assertDoesNotThrow(() -> CH.addOption("TestValue2", "yes"));
    }
	
	@Test
	public void testServerAddEditDeleteDatabaseItems() {
		Server s = new Server(conn, connUser, connPassword);
		
		//Add stuff
		assertTrue(s.addServer("testServer"));
		assertTrue(s.addRole("testRole"));
		assertTrue(s.addUser("testUser"));
		
		
		//Edit server
		assertTrue(s.editServerEmote("default", "melon"));
		assertTrue(s.editServerEmote("random", "melon"));
		assertTrue(s.editServerEmote("rare", "melon"));
		assertFalse(s.editServerEmote("not real", "melon"));
		
		assertTrue(s.editUserEmote("testUser", "melon"));
		assertFalse(s.editUserEmote("testUserFake", "melon"));
		
		assertTrue(s.editRoleEmote("testRole", "melon"));
		assertFalse(s.editRoleEmote("testRoleFake", "melon"));
		
		
		//Delete stuff
		assertTrue(s.deleteRole("testRole"));
		assertTrue(s.deleteUser("testUser"));
		assertTrue(s.deleteServer());
	}
	
	@Test
	public void testAllServerFetch() {
		
		//No servers in DB
		assertDoesNotThrow(() -> Server.fetchServers(conn, connUser, connPassword));

		//Add a server
		Server s = new Server(conn, connUser, connPassword);
		s.addServer("testServer");
		s.addRole("testRole");
		s.addUser("testUser");
		Server s2 = new Server(conn, connUser, connPassword);
		s2.addServer("testServer2");
		s2.addRoleWithEmote("testRole2", "melon");
		s2.addUserWithEmote("testUser", "melon2"); //We can have multiple users but role ids are unique
		assertDoesNotThrow(() -> Server.fetchServers(conn, connUser, connPassword));
		
		//Make sure data was saved and retrieved properly.
		try {
			ArrayList<Server> servers = Server.fetchServers(conn, connUser, connPassword);
			assertTrue(servers.size() == 2);
			//s
			assertTrue(servers.get(0).getServer_id().equals("testServer"));
			assertTrue(servers.get(0).getRoles().size() == 1);
			assertTrue(servers.get(0).getUsers().size() == 1);
			assertTrue(servers.get(0).getRoles().get(0).getRoleID().equals("testRole"));
			assertTrue(servers.get(0).getRoles().get(0).getEmote().equals("default"));
			assertTrue(servers.get(0).getUsers().get(0).getUserID().equals("testUser"));
			assertTrue(servers.get(0).getUsers().get(0).getEmote().equals("default"));
			//s2
			assertTrue(servers.get(1).getServer_id().equals("testServer2"));
			assertTrue(servers.get(1).getRoles().size() == 1);
			assertTrue(servers.get(1).getUsers().size() == 1);
			assertTrue(servers.get(1).getRoles().get(0).getRoleID().equals("testRole2"));
			assertTrue(servers.get(1).getRoles().get(0).getEmote().equals("melon"));
			assertTrue(servers.get(1).getUsers().get(0).getUserID().equals("testUser"));
			assertTrue(servers.get(1).getUsers().get(0).getEmote().equals("melon2"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Cleanup DB
		s.deleteRole("testRole");
		s.deleteUser("testUser");
		s.deleteServer();
		s2.deleteRole("testRole2");
		s2.deleteUser("testUser");
		s2.deleteServer();
	}
	
	@Test
	public void testEmoteHelper() {
		String melonEmoji = EmojiManager.getForAlias("watermelon").getUnicode(); //Simulate unicode since eclipse won't let me
		String melonHex = EmojiManager.getForAlias("watermelon").getHtmlHexadecimal();
		assertEquals(EmoteHelper.getEmoteString(melonEmoji), melonHex);
		
		String testEmote = "<:c_medigun_gold:818125282871214100>";
		assertEquals(testEmote, EmoteHelper.getEmoteString(testEmote));
	}
	
	@Test
	public void testServerManager() {
		Server s1 = new Server(conn, connUser, connPassword);
		s1.addServer("testServer");
		s1.addRole("testRole");
		s1.addUser("testUser");
		Server s2 = new Server(conn, connUser, connPassword);
		s2.addServer("testServer2");
		s2.addRole("testRole2");
		s2.addUser("testUser");
		
		ArrayList<Server> servers = new ArrayList<Server>();
		servers.add(s1);
		servers.add(s2);
		
		ServerManager sm = new ServerManager(conn, connUser, connPassword, servers, "", "", "");
		assertTrue(sm.addServerByID("testServer3"));
		assertTrue(sm.serverExists("testServer3"));
		assertTrue(sm.addRoleToServer("testRole3", "testServer3", "default"));
		assertTrue(sm.addUserToServer("testUSer", "testServer3", "default"));
		assertTrue(sm.deleteServerByID("testServer"));
		assertTrue(sm.deleteServerByID("testServer2"));
		assertTrue(sm.deleteServerByID("testServer3"));
	}
	
}
