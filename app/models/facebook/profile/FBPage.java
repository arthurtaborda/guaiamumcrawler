package models.facebook.profile;

public class FBPage extends FBProfile {

	public FBPage() {
	}

	public FBPage(String id) {
		super(id);
	}

	public FBPage(String id, String name) {
		super(id, name);
	}

	public FBPage(String id, String name, String username) {
		super(id, name, username);
	}

	@Override
	public String getType() {
		return "page";
	}

	@Override
	public String getLink() {
		return "http://facebook.com/" + id;
	}

}
