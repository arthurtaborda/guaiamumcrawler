package models.facebook.profile;

public class FBGroup extends FBProfile {

	public FBGroup() {
	}

	public FBGroup(String id) {
		super(id);
	}

	public FBGroup(String id, String name) {
		super(id, name);
	}

	public FBGroup(String id, String name, String username) {
		super(id, name, username);
	}

	@Override
	public String getType() {
		return "group";
	}

	@Override
	public String getLink() {
		return "http://facebook.com/groups" + id;
	}
}
