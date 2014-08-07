package models.facebook;

public class FBGroup extends FBProfile {

	public FBGroup(String id) {
		super(id);
	}

	public FBGroup(String id, String name) {
		super(id, name);
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
