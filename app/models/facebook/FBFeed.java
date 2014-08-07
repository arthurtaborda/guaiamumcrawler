package models.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FBFeed {

	@JsonIgnore
	public FBProfile profile;

	public Long lastTimeScanned;

	public Long averageTimePost;

	public Boolean totallyScanned;

	@JsonIgnore
	public List<FBPost> posts;

	public FBFeed() {
	}

	public FBFeed(List<FBPost> posts) {
		this.posts = posts;
	}
}
