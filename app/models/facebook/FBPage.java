package models.facebook;

public class FBPage extends FBProfile {

	public FBPage() {
	}

	public FBPage(String id) {
		super(id);
	}

	public FBPage(String id, String name) {
		super(id, name);
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
