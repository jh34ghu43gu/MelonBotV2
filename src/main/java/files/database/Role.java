package files.database;

/**
 * @author aaron
 * Role object to represent a role that has an emote attached to it.
 */
public class Role {
	
	private String roleID;
	private String emote;
	
	public Role(String roleID, String emote) {
		super();
		this.roleID = roleID;
		this.emote = emote;
	}

	public String getRoleID() {
		return roleID;
	}

	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}

	public String getEmote() {
		return emote;
	}

	public void setEmote(String emote) {
		this.emote = emote;
	}
	
	

}
