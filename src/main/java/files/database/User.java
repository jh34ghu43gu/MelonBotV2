package files.database;

/**
 * @author aaron
 * User object to represent a user that has an emote attached to them.
 */
public class User {
	
	private long id;
	private String userID;
	private String emote;
	
	public User(String userID, String emote) {
		super();
		this.userID = userID;
		this.emote = emote;
	}
	
	public User(long id, String userID, String emote) {
		super();
		this.id = id;
		this.userID = userID;
		this.emote = emote;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getEmote() {
		return emote;
	}

	public void setEmote(String emote) {
		this.emote = emote;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	

}
