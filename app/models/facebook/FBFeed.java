package models.facebook;

import java.util.List;

import models.facebook.profile.FBProfile;

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
		this.totallyScanned = false;
	}

	public FBFeed(List<FBPost> posts) {
		this.posts = posts;
	}
}
