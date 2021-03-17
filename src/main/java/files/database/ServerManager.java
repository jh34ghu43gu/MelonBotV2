package files.database;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.EmoteHelper;

/**
 * @author aaron
 * Holds the server array and interacts with it. 
 * Should only have 1 created in Bot.java and passed to any events that need it.
 */
public class ServerManager {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(ServerManager.class);
	
	private ArrayList<Server> servers;
	private String conn;
	private String connUser;
	private String connPassword;
	
	private String defaultEmote;
	private String defaultRandom;
	private String defaultRare;
	
	public ServerManager(String conn, String connUser, String connPassword, ArrayList<Server> servers, String emote, String random, String rare) {
		this.servers = servers;
		this.conn = conn;
		this.connUser = connUser;
		this.connPassword = connPassword;
		this.defaultEmote = emote;
		this.defaultRandom = random;
		this.defaultRare = rare;
	}
	
	/**
	 * Find a server's default emotes based on rand
	 * @param serverID
	 * @param rand - default, random, or rare
	 * @return server's default [rand] emote or blank if invalid rand
	 */
	public String getServerRandom(String serverID, String rand) {
		String out = "";
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				if(rand.equalsIgnoreCase("default")) {
					out = s.getDefaultEmote();
					if(out.equalsIgnoreCase("default")) {
						out = EmoteHelper.getDefaultEmote(defaultEmote);
					}
				} else if(rand.equalsIgnoreCase("random")) {
					out = s.getRandomEmote();
					if(out.equalsIgnoreCase("default")) {
						out = EmoteHelper.getDefaultEmote(defaultRandom);
					}
				} else if(rand.equalsIgnoreCase("rare")) {
					out = s.getRareEmote();
					if(out.equalsIgnoreCase("default")) {
						out = EmoteHelper.getDefaultEmote(defaultRare);
					}
				}
			}
		}
		return out;
	}
	
	/**
	 * Retrieve the user's assigned emote if it exists
	 * @param userID
	 * @param serverID
	 * @return format for discord messages aka unicode or <::#>
	 */
	public String getUserEmoteForMessages(String userID, String serverID) {
		String emote = "";
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				for(User u : s.getUsers()) {
					if(u.getUserID().equals(userID)) {
						emote = u.getEmote();
						
						if(emote.equalsIgnoreCase("default")) {
							emote = getServerRandom(serverID, "default");
						}
						
						if(!emote.startsWith("<")) { //Emote is already stored in text format if custom
							emote = EmoteHelper.getEmoteUnicode(emote); //Thus only retrieve unicode if emoji
						}
						
					}
				}
			}
		}
		return emote;
	}
	
	/**
	 * Retrieve the user's assigned emote if it exists
	 * @param userID
	 * @param serverID
	 * @return format for discord reactions aka unicode or id only
	 */
	public String getUserEmoteForReacts(String userID, String serverID) {
		String emote = "";
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) { //server found
				for(User u : s.getUsers()) {
					if(u.getUserID().equals(userID)) { //user found
						emote = u.getEmote(); //get their emote
						//log.debug("User has emote: " + emote);
						
						if(emote.equalsIgnoreCase("default")) { //if default then get server default
							emote = getServerRandom(serverID, "default");
							//log.debug("User had default emote, found default emote of: " + emote);
						}
						
						if(emote.startsWith("<")) {
							emote = EmoteHelper.cleanCustomEmote(emote);
						} else {
							emote = EmoteHelper.getEmoteUnicode(emote);
						}
					}
				}
			}
		}
		//log.debug("Returning getUserEmoteForReacts with: " + emote);
		return emote;
	}
	
	/**
	 * Retrieve the role's assigned emote if it exists
	 * @param roleID
	 * @param serverID
	 * @return format for discord message aka <::#>
	 */
	public String getRoleEmoteForMessage(String roleID, String serverID) {
		String emote = "";
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				for(Role r : s.getRoles()) {
					if(r.getRoleID().equals(roleID)) {
						if(r.getEmote().startsWith("<")) {
							emote = r.getEmote();
						} else {
							emote = EmoteHelper.getEmoteUnicode(r.getEmote());
						}
						if(emote.equalsIgnoreCase("default")) {
							emote = getServerRandom(serverID, "default");
						}
					}
				}
			}
		}
		return emote;
	}
	
	/**
	 * Retrieve the role's assigned emote if it exists
	 * @param roleID
	 * @param serverID
	 * @return format for discord react aka unicode or id
	 */
	public String getRoleEmoteForReact(String roleID, String serverID) {
		String emote = "";
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				for(Role r : s.getRoles()) {
					if(r.getRoleID().equals(roleID)) {
						if(r.getEmote().startsWith("<")) {
							emote = EmoteHelper.cleanCustomEmote(r.getEmote());
						} else {
							emote = EmoteHelper.getEmoteUnicode(r.getEmote());
						}
						if(emote.equalsIgnoreCase("default")) {
							emote = getServerRandom(serverID, "default");
						}
					}
				}
			}
		}
		return emote;
	}
	
	/*
	public ArrayList<Role> getServerRoles(String serverID) {
		ArrayList<Role> roles = new ArrayList<Role>();
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				roles = s.getRoles();
			}
		}
		return roles;
	} */
	
	/**
	 * Add a user-emote to server
	 * @param userID
	 * @param serverID
	 * @param emote
	 * @return false if failure or server doesn't exist
	 */
	public Boolean addUserToServer(String userID, String serverID, String emote) {
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				return s.addUserWithEmote(userID, emote);
			}
		}
		return false;
	}
	
	/**
	 * Add a role-emote to server
	 * @param roleID
	 * @param serverID
	 * @param emote
	 * @return false if failure or server doesn't exist
	 */
	public Boolean addRoleToServer(String roleID, String serverID, String emote) {
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				return s.addRoleWithEmote(roleID, emote);
			}
		}
		return false;
	}
	
	/**
	 * Removes the server and all roles/users associated with said server from the database and local list.
	 * @param id
	 * @return false if server wasn't on the list or failed.
	 */
	public Boolean deleteServerByID(String id) {
		for(Server s : servers) {
			if(s.getServer_id().equals(id)) {
				if(s.deleteServerFull()) {
					servers.remove(s);
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Adds the server to database and ServerManager's list.
	 * @param id
	 * @return false if server already exists or failed to add.
	 */
	public Boolean addServerByID(String id) {
		if(serverExists(id)) {
			return false;
		}
		
		Server s = new Server(conn, connUser, connPassword);
		if(s.addServer(id)) {
			servers.add(s);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if the server exists in the ServerManager's list.
	 * @param id
	 * @return true if server exists in the list, false if not.
	 */
	public Boolean serverExists(String id) {
		for(Server s : servers) {
			if(s.getServer_id().equals(id)) { return true; }
		}
		return false;
	}
	
	/**
	 * Remove a user from the server
	 * @param userID
	 * @param serverID
	 * @return false if error or user doesn't exist
	 */
	public Boolean removeUserFromServer(String userID, String serverID) {
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				return s.deleteUser(userID);
			}
		}
		return false;
	}
	
	/**
	 * Remove a role from the server
	 * @param roleID
	 * @param serverID
	 * @return false if error or role doesn't exist
	 */
	public Boolean removeRoleFromServer(String roleID, String serverID) {
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				return s.deleteRole(roleID);
			}
		}
		return false;
	}
	
	/**
	 * Edit a server default 
	 * @param serverID
	 * @param edit - 'default' || 'random' || 'rare'
	 * @param emote
	 * @return false if error 
	 */
	public Boolean editServerDefault(String serverID, String edit, String emote) {
		for(Server s : servers) {
			if(s.getServer_id().equals(serverID)) {
				return s.editServerEmote(edit, emote);
			}
		}
		return false;
	}

}
