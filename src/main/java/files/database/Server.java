package files.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author aaron
 * Server object that represents a guild, the guild's options, roles, and users.
 * Contains all methods for interacting with the database.
 */
public class Server {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(Server.class);
	
	private String server_id;
	private String defaultEmote;
	private String randomEmote; // 1:10000
	private String rareEmote; // 1:1000000
	private ArrayList<User> users;
	private ArrayList<Role> roles;	
	
	private String conn;
	private String connUser;
	private String connPassword;
	
	public Server(String conn, String connUser, String connPassword) {
		this.conn = conn;
		this.connUser = connUser;
		this.connPassword = connPassword;
		users = new ArrayList<User>();
		roles = new ArrayList<Role>();
	}
	
	/*
	 * =========================================================================================
	 * Getters/setters
	 * =========================================================================================
	 */
	
	public String getServer_id() {
		return server_id;
	}

	public void setServer_id(String server_id) {
		this.server_id = server_id;
	}

	public String getDefaultEmote() {
		return defaultEmote;
	}

	public String getRandomEmote() {
		return randomEmote;
	}

	public String getRareEmote() {
		return rareEmote;
	}

	public void setDefaultEmote(String defaultEmote) {
		this.defaultEmote = defaultEmote;
	}

	public void setRandomEmote(String randomEmote) {
		this.randomEmote = randomEmote;
	}

	public void setRareEmote(String rareEmote) {
		this.rareEmote = rareEmote;
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public ArrayList<Role> getRoles() {
		return roles;
	}
	
	/**
	 * Check if user is added and return what emote they are added to
	 * @param id - user id
	 * @return Emote, 'default', or '' if they aren't added
	 */
	public String userHasEmote(String id) { //Note: don't need to check server id since there will be a server object per server
		String emote = "";
		for(User u : users) {
			if(u.getUserID() == id) {
				emote = u.getEmote();
				break;
			}
		}
		return emote;
	}
	
	/**
	 * Check if role is added and return what emote it is attached to
	 * @param id - role id
	 * @return Emote, 'default', or '' if they aren't added
	 */
	public String roleHasEmote(String id) {
		String emote = "";
		for(Role r : roles) {
			if(r.getRoleID() == id) {
				emote = r.getEmote();
				break;
			}
		}
		return emote;
	}
	
	/**
	 * Checks if role exists in the arraylist
	 * @param id
	 * @return - role object if it exists, otherwise a role object with -1 roleID
	 */
	private Role roleExists(String id) {
		for(Role r : roles) {
			if(r.getRoleID().equals(id)) {
				return r;
			}
		}
		Role r = new Role("-1", "");
		return r;
	}
	
	/**
	 * Checks if user exists in the arraylist
	 * @param id
	 * @return - user object if it exists, otherwise a user object with -1 id
	 */
	private User userExists(String id) {
		for(User u : users) {
			if(u.getUserID().equals(id)) {
				return u;
			}
		}
		User u = new User(-1, "", "");
		return u;
	}
	
	/* =========================================================================================
	 * Database modify methods
	 * =========================================================================================
	 */
	
	/**
	 * Edit a server default/random/rare emote in the database and in the server object
	 * @param type - default, random, rare
	 * @param emote
	 * @return false if bad type or error
	 */
	public Boolean editServerEmote(String type, String emote) {
		log.debug("Attempting to edit server emote type: " + type + " to " + emote);
		if(type.equalsIgnoreCase("default") || type.equalsIgnoreCase("random") || type.equalsIgnoreCase("rare")) {
			//	¯\_(._.)_/¯
		} else {
			log.debug("Bad server emote type.");
			return false;
		}
		
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement updatePStatement = null;
		    String myQuery = "";
		    if(type.equalsIgnoreCase("default")) {
		    	myQuery = "update servers "
			    		+ "set default_emote = ? "
			    		+ "where server_id = ?";
		    } else if(type.equalsIgnoreCase("random")) {
		    	myQuery = "update servers "
			    		+ "set random_emote = ? "
			    		+ "where server_id = ?";
		    } else if(type.equalsIgnoreCase("rare")) {
		    	myQuery = "update servers "
			    		+ "set rare_emote = ? "
			    		+ "where server_id = ?";
		    }
		    updatePStatement = con.prepareStatement(myQuery);
		    updatePStatement.setString(1, emote);
		    updatePStatement.setString(2, server_id);
		    updatePStatement.executeUpdate();
		    
		    con.close();
		    //Do these outside the connection in case error stops the DB edit
		    if(type.equalsIgnoreCase("default")) {
		    	this.defaultEmote = emote;
		    } else if(type.equalsIgnoreCase("random")) {
		    	this.randomEmote = emote;
		    } else if(type.equalsIgnoreCase("rare")) {
		    	this.rareEmote = emote;
		    }
			log.debug("Succesfully edited server emote type: " + type + " to " + emote);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to edit server emote type: " + type + " to " + emote);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Edit user's emote in the database and in user object
	 * @param userID
	 * @param emote
	 * @return false if user doesn't exist or error
	 */
	public Boolean editUserEmote(String userID, String emote) {
		log.debug("Attempting to edit user " + userID + " emote to " + emote);
		User u = this.userExists(userID);
		if(u.getId() == -1) { //Don't try to edit a user that doesn't exist
			log.error("Failed to edit user " + userID + " emote to " + emote + ". User does not exist. Server id: " + server_id);
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement updatePStatement = null;
		    String myQuery = "update users "
			    		+ "set emote = ? "
			    		+ "where server_id = ?"
			    		+ "and user_id = ?";
		    updatePStatement = con.prepareStatement(myQuery);
		    updatePStatement.setString(1, emote);
		    updatePStatement.setString(2, server_id);
		    updatePStatement.setString(3, userID);
		    updatePStatement.executeUpdate();
		    
		    con.close();
		    
		    u.setEmote(emote);
		    
			log.debug("Succesfully edited user " + userID + " emote to " + emote);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to edit user " + userID + " emote to " + emote);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Edit role's emote in the database and role object
	 * @param roleID
	 * @param emote
	 * @return false if role doesn't exist or error
	 */
	public Boolean editRoleEmote(String roleID, String emote) {
		log.debug("Attempting to edit role " + roleID + " emote to " + emote);
		Role r = this.roleExists(roleID);
		if(r.getRoleID().equals("-1")) { //Don't try to edit a role that doesn't exist
			log.error("Failed to edit role " + roleID + " emote to " + emote + ". Role does not exist. Server id: " + server_id);
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement updatePStatement = null;
		    String myQuery = "update roles "
			    		+ "set emote = ? "
			    		+ "where role_id = ?";
		    updatePStatement = con.prepareStatement(myQuery);
		    updatePStatement.setString(1, emote);
		    updatePStatement.setString(2, roleID);
		    updatePStatement.executeUpdate();
		    
		    con.close();
		    
		    r.setEmote(emote);
		    
			log.debug("Succesfully edited role " + roleID + " emote to " + emote);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to edit role " + roleID + " emote to " + emote);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	
	/* =========================================================================================
	 * Database add/delete methods
	 * =========================================================================================
	 */
	
	/**
	 * Adds server to servers table with default emotes
	 * @param id
	 * @return - false if any errors
	 */
	public Boolean addServer(String id) {
		log.debug("Attempting to add server to database. Id: " + id);
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement insertPStatement = null;
		    String myQuery = "insert into servers(server_id, default_emote, random_emote, rare_emote) "
		    		+ "values(?, ?, ?, ?)";
		    insertPStatement = con.prepareStatement(myQuery);
		    insertPStatement.setString(1, id);
		    insertPStatement.setString(2, "default");
		    insertPStatement.setString(3, "default");
		    insertPStatement.setString(4, "default");
		    insertPStatement.execute();
		    
		    this.server_id = id;
		    this.defaultEmote = "default";
		    this.randomEmote = "default";
		    this.rareEmote = "default";
		    
		    con.close();
			log.debug("Succesfully added server object into database. Id: " + id);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to add server object, id: " + id);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Adds user to users table from a user id and server id (will set emote to 'default')
	 * Also adds user to users arraylist
	 * @param userID
	 * @return false if any errors.
	 */
	public Boolean addUser(String userID) {
		return addUserWithEmote(userID, "default");
	}
	
	/**
	 * Adds user to users table from a user id and server id with an emote string
	 * Also adds user to users arraylist
	 * @param userID
	 * @param emote
	 * @return false if any errors or user already exists with the server
	 */
	public Boolean addUserWithEmote(String userID, String emote) {
		log.debug("Attempting to add user to database. User id: " + userID + " Server id: " + server_id);
		if(userExists(userID).getId() != -1) {
			log.warn("Failed to add user " + userID + ", user already has an emote in this server.");
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement insertPStatement = null;
		    String myQuery = "insert into users(server_id, user_id, emote) "
		    		+ "values(?, ?, ?)";
		    insertPStatement = con.prepareStatement(myQuery, Statement.RETURN_GENERATED_KEYS);
		    insertPStatement.setString(1, server_id);
		    insertPStatement.setString(2, userID);
		    insertPStatement.setString(3, emote);
		    insertPStatement.execute();
		    ResultSet rs = insertPStatement.getGeneratedKeys();
		    long id = -1;
			if (rs.next()) {
				 id = rs.getLong(1);
			}
		    con.close();
		    
		    User u = new User(id, userID, emote);
			users.add(u);
			log.debug("Succesfully added user object into database. User id: " + userID + " Server id: " + server_id);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to add user object, User id: " + userID + " Server id: " + server_id);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Adds role to roles table from a role id with default emote
	 * Also adds to roles arraylist
	 * @param roleID
	 * @return false if errors
	 */
	public Boolean addRole(String roleID) {
		return addRoleWithEmote(roleID, "default");
	}
	
	/**
	 * Adds role to roles table from a role id with an emote
	 * Also adds to roles arraylist
	 * @param roleID
	 * @param emote
	 * @return false if errors or role exists
	 */
	public Boolean addRoleWithEmote(String roleID, String emote) {
		log.debug("Attempting to add role to database. Role id: " + roleID + " Server id: " + server_id);
		if(!roleExists(roleID).getRoleID().equals("-1")) {
			log.warn("Could not add role " + roleID + " to database, role already exists.");
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn,
					connUser,
					connPassword);
			
			PreparedStatement insertPStatement = null;
		    String myQuery = "insert into roles(role_id, server_id, emote) "
		    		+ "values(?, ?, ?)";
		    insertPStatement = con.prepareStatement(myQuery);
		    insertPStatement.setString(1, roleID);
		    insertPStatement.setString(2, server_id);
		    insertPStatement.setString(3, emote);
		    insertPStatement.execute();
		    con.close();
		    
		    Role r = new Role(roleID, emote);
			roles.add(r);
			log.debug("Succesfully added role object into database. Role id: " + roleID + " Server id: " + server_id);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to add role object, Role id: " + roleID + " Server id: " + server_id);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Deletes the role from the database
	 * @param roleID
	 * @return false if role doesn't exist or error
	 */
	public Boolean deleteRole(String roleID) {
		log.debug("Attempting to remove role " + roleID + " from database. Server id: " + server_id);
		Role r = this.roleExists(roleID);
		if(r.getRoleID().equals("-1")) { //Don't try to delete a role that doesn't exist
			log.error("Failed to removed role " + roleID + " from database. Role does not exist. Server id: " + server_id);
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn,
					connUser,
					connPassword);
			
			PreparedStatement deletePStatement = null;
		    String myQuery = "delete from roles "
		    		+ "where role_id = ? "
		    		+ "and server_id = ? ";
		    deletePStatement = con.prepareStatement(myQuery);
		    deletePStatement.setString(1, roleID);
		    deletePStatement.setString(2, server_id);
		    deletePStatement.execute();
		    con.close();
		    
		    roles.remove(r);
		    
			log.debug("Succesfully removed role " + roleID + " from database. Server id: " + server_id);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to removed role " + roleID + " from database. Server id: " + server_id);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Deletes user from database
	 * @param userID
	 * @return false if user doesn't exist or error
	 */
	public Boolean deleteUser(String userID) {
		log.debug("Attempting to remove user " + userID + " from database. Server id: " + server_id);
		User u = this.userExists(userID);
		if(u.getId() == -1) { //Don't try to delete a user that doesn't exist
			log.error("Failed to removed user " + userID + " from database. User does not exist. Server id: " + server_id);
			return false;
		}
		try {
			Connection con = DriverManager.getConnection(
					conn,
					connUser,
					connPassword);
			
			PreparedStatement deletePStatement = null;
		    String myQuery = "delete from users "
		    		+ "where user_id = ? "
		    		+ "and server_id = ? ";
		    deletePStatement = con.prepareStatement(myQuery);
		    deletePStatement.setString(1, userID);
		    deletePStatement.setString(2, server_id);
		    deletePStatement.execute();
		    con.close();
		    
		    users.remove(u);
		    
			log.debug("Succesfully removed user " + userID + " from database. Server id: " + server_id);
		    return true;
		} catch (SQLException e) {
			log.error("Failed to removed user " + userID + " from database. Server id: " + server_id);
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Delete server from database, server object should be deleted from wherever this method was called from.
	 * @return false if user or role arrays aren't empty, or if error
	 */
	public Boolean deleteServer() {
		log.debug("Attempting to remove server " + server_id);
		if(!users.isEmpty() || !roles.isEmpty()) { //Need to not have any roles/users attached or it will fail.
			log.error("Failed to removed server " + server_id + " roles/users are not empty.");
			return false;
		}
		
		try {
			Connection con = DriverManager.getConnection(
					conn,
					connUser,
					connPassword);
			
			PreparedStatement deletePStatement = null;
		    String myQuery = "delete from servers "
		    		+ "where server_id = ? ";
		    deletePStatement = con.prepareStatement(myQuery);
		    deletePStatement.setString(1, server_id);
		    deletePStatement.execute();
		    con.close();
		    
		    //Server can be cleared from where ever this was called.
		    
			log.debug("Succesfully removed server " + server_id + " from database.");
		    return true;
		} catch (SQLException e) {
			log.error("Failed to removed server " + server_id + " from database.");
			log.error(e.getMessage());
			return false; 
		}
	}
	
	/**
	 * Delete server from database, unlike deleteServer() this method will attempt to remove all associated roles/users.
	 * @return false if error or failure to remove a role/user.
	 */
	public Boolean deleteServerFull() {
		log.debug("Attempting to remove server " + server_id + " and all associated roles/users.");
		
		ArrayList<String> userIDS = new ArrayList<String>();
		ArrayList<String> roleIDS = new ArrayList<String>();
		for(User u : users) {
			userIDS.add(u.getUserID());
		}
		for(Role r : roles) {
			roleIDS.add(r.getRoleID());
		}
		
		for(String s : userIDS) {
			if(!deleteUser(s)) {
				log.error("Could not remove a user(" + s + "), aborting server delete.");
				return false;
			}
		}
		for(String s : roleIDS) {
			if(!deleteRole(s)) {
				log.error("Could not remove a role(" + s + "), aborting server delete.");
				return false;
			}
		}
		log.debug("Finished removing all roles and users associated with server " + server_id);
		try {
			Connection con = DriverManager.getConnection(
					conn,
					connUser,
					connPassword);
			
			PreparedStatement deletePStatement = null;
		    String myQuery = "delete from servers "
		    		+ "where server_id = ? ";
		    deletePStatement = con.prepareStatement(myQuery);
		    deletePStatement.setString(1, server_id);
		    deletePStatement.execute();
		    con.close();
		    
		    //Server can be cleared from where ever this was called.
		    
			log.debug("Succesfully removed server " + server_id + " from database.");
		    return true;
		} catch (SQLException e) {
			log.error("Failed to removed server " + server_id + " from database.");
			log.error(e.getMessage());
			return false; 
		}
	}
	
	
	/* =========================================================================================
	 * Query methods
	 * =========================================================================================
	 */
	
	
	/**
	 * Fetch all server objects and children from the database.
	 * @param conn
	 * @param connUser
	 * @param connPassword
	 * @return - ArrayList of servers
	 * @throws Exception if any server/role/user object could not be fetched.
	 */
	public static ArrayList<Server> fetchServers(String conn, String connUser, String connPassword) throws Exception {
		log.debug("Attempting to retrieve all server objects from database.");
		ArrayList<Server> servers = new ArrayList<Server>();
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select server_id, default_emote, random_emote, rare_emote "
		    		+ "from servers ";
		    queryPStatement = con.prepareStatement(myQuery);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //No servers exist
		    	return servers;
		    } else {
		    	do {
		    		Server s = new Server(conn, connUser, connPassword);
		    		String serverID = rs.getString(1);
			    	s.setServer_id(serverID);
			    	s.setDefaultEmote(rs.getString(2));
			    	s.setRandomEmote(rs.getString(3));
			    	s.setRareEmote(rs.getString(4));
			    	if(!s.fetchUsersFromServerIDWithConn(serverID, con)) { //Error getting users
				    	throw new Exception("Failed to fetch users for server " + serverID);
				    }
				    if(!s.fetchRolesFromServerIDWithConn(serverID, con)) { //Error getting roles
				    	throw new Exception("Failed to fetch roles for server " + serverID);
				    }
				    servers.add(s);
		    	} while(rs.next());
		    }
		    
		    con.close();
		    log.debug("Succesfully retrieved all server objects from database.");
		    return servers; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get a server object when trying to fetch all servers.");
			log.error(e.getMessage());
			throw new Exception("Problem fetching a server object.");
		} catch (Exception e) {
			log.error("Failed to fetch a user or role object while fetching all servers.");
			log.error(e.getMessage());
			throw new Exception("Problem fetching a server object's user/role objects.");
		}
	}
	
	/**
	 * Query server table for matching server id and pull all needed info (users/roles)
	 * @param id - Guild id
	 * @return - false if errors or server doesn't exist
	 */
	public Boolean fetchServerFromServerID(String id) {
		log.debug("Attempting to retrieve server object from database. Id: " + id);
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select server_id, default_emote, random_emote, rare_emote "
		    		+ "from servers "
		    		+ "where server_id = ?";
		    queryPStatement = con.prepareStatement(myQuery);
		    queryPStatement.setString(1, id);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //Server doesn't exist
		    	return false;
		    } else {
		    	this.server_id = rs.getString(1);
		    	this.defaultEmote = rs.getString(2);
		    	this.randomEmote = rs.getString(3);
		    	this.rareEmote = rs.getString(4);
		    }
		    if(!this.fetchUsersFromServerIDWithConn(id, con)) { //Error getting users
		    	return false;
		    }
		    if(!this.fetchRolesFromServerIDWithConn(id, con)) { //Error getting roles
		    	return false;
		    }
		    con.close();
		    log.debug("Succesfully retrieved server object from database. Id: " + id);
		    return true; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get server object for server id: " + id);
			log.error(e.getMessage());
			return false; //We couldn't finish fetching the items
		}
	}
	
	/**
	 * Query users table for all users under guild id and adds them to users arraylist
	 * @param id - guild id
	 * @return - returns false for errors
	 */
	public Boolean fetchUsersFromServerID(String id) {
		log.debug("Attempting to retrieve user objects from database. Server Id: " + id);
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select id, server_id, user_id, emote "
		    		+ "from users "
		    		+ "where server_id = ?";
		    queryPStatement = con.prepareStatement(myQuery);
		    queryPStatement.setString(1, id);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //No users for the server
		    	log.debug("No user objects found for server id: " + id);
		    	return true;
		    } else {
		    	do {
		    		User u = new User(rs.getLong(1), rs.getString(3), rs.getString(4));
		    		users.add(u);
		    		log.debug("Added user id: " + u.getUserID() + " to server " + id);
		    	} while(rs.next());
		    }
		    con.close();
		    log.debug("Succesfully retrieved all user objects for server id: " + id);
		    return true; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get user objects for server id: " + id);
			log.error(e.getMessage());
			return false; //We couldn't finish fetching the items
		}
	}
	
	/**
	 * Query roles table for all roles under guild id and adds them to roles arraylist
	 * @param id - guild id
	 * @return - false if errors
	 */
	public Boolean fetchRolesFromServerID(String id) {
		log.debug("Attempting to retrieve role objects from database. Server Id: " + id);
		try {
			Connection con = DriverManager.getConnection(
					conn, 
					connUser, 
					connPassword);
			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select server_id, role_id, emote "
		    		+ "from roles "
		    		+ "where server_id = ?";
		    queryPStatement = con.prepareStatement(myQuery);
		    queryPStatement.setString(1, id);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //No roles for the server
		    	log.debug("No role objects found for server id: " + id);
		    	return true;
		    } else {
		    	do {
		    		Role r = new Role(rs.getString(2), rs.getString(3));
		    		roles.add(r);
		    		log.debug("Added role id: " + r.getRoleID() + " to server " + id);
		    	} while(rs.next());
		    }
		    con.close();
		    log.debug("Succesfully retrieved all role objects for server id: " + id);
		    return true; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get role objects for server id: " + id);
			log.error(e.getMessage());
			return false; //We couldn't finish fetching the items
		}
	}
	
	
	/**
	 * Method for server class to query with an existing connection
	 * Query users table for all users under guild id and adds them to users arraylist
	 * @param id - guild id
	 * @return - returns false for errors
	 */
	private Boolean fetchUsersFromServerIDWithConn(String id, Connection con) {
		log.debug("Attempting to retrieve user objects from database. Server Id: " + id);
		try {
			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select server_id, user_id, emote "
		    		+ "from users "
		    		+ "where server_id = ?";
		    queryPStatement = con.prepareStatement(myQuery);
		    queryPStatement.setString(1, id);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //No users for the server
		    	log.debug("No user objects found for server id: " + id);
		    	return true;
		    } else {
		    	do {
		    		User u = new User(rs.getString(2), rs.getString(3));
		    		users.add(u);
		    		log.debug("Added user id: " + u.getUserID() + " to server " + id);
		    	} while(rs.next());
		    }
		    log.debug("Succesfully retrieved all user objects for server id: " + id);
		    return true; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get user objects for server id: " + id);
			log.error(e.getMessage());
			return false; //We couldn't finish fetching the items
		}
	}
	
	/**
	 * Method for server class to query with an existing connection
	 * Query roles table for all roles under guild id and adds them to roles arraylist
	 * @param id - guild id
	 * @return - false if errors
	 */
	private Boolean fetchRolesFromServerIDWithConn(String id, Connection con) {
		log.debug("Attempting to retrieve role objects from database. Server Id: " + id);
		try {			
			PreparedStatement queryPStatement = null;
		    String myQuery = "select server_id, role_id, emote "
		    		+ "from roles "
		    		+ "where server_id = ?";
		    queryPStatement = con.prepareStatement(myQuery);
		    queryPStatement.setString(1, id);
		    ResultSet rs = queryPStatement.executeQuery();
		    if(rs.next() == false) { //No roles for the server
		    	log.debug("No role objects found for server id: " + id);
		    	return true;
		    } else {
		    	do {
		    		Role r = new Role(rs.getString(2), rs.getString(3));
		    		roles.add(r);
		    		log.debug("Added role id: " + r.getRoleID() + " to server " + id);
		    	} while(rs.next());
		    }
		    log.debug("Succesfully retrieved all role objects for server id: " + id);
		    return true; //No errors mean we fetched all the items
		} catch (SQLException e) {
			log.error("Failed to get role objects for server id: " + id);
			log.error(e.getMessage());
			return false; //We couldn't finish fetching the items
		}
	}
	
	
}
