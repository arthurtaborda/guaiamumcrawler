package models.twitter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TwitterFeed {

	@JsonIgnore
	public TwitterUser user;

	public Long lastTimeScanned;

	public Long averageTimePost;

	public Boolean totallyScanned;

	@JsonIgnore
	public List<TwitterTweet> tweets;

	public TwitterFeed() {
		this.totallyScanned = false;
	}

	public TwitterFeed(List<TwitterTweet> tweets) {
		this.tweets = tweets;
	}
}
