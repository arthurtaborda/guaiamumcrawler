package models.facebook;

public class FBUser extends FBProfile {

	public FBUser() {
	}

	public FBUser(String id) {
		super(id);
	}

	public FBUser(String id, String name) {
		super(id, name);
	}

	@Override
	public String getType() {
		return "user";
	}

	@Override
	public String getLink() {
		return "http://facebook.com/" + id;
	}
}
