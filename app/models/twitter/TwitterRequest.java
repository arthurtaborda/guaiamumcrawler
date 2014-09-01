package models.twitter;

import java.util.List;

public class TwitterRequest {

	public List<TwitterUser> users;

	public List<TwitterTweet> tweets;

	public long cursor;

	public int requestCount;

	public boolean gotAllTweets;

	public int requestRemaining;

	public boolean requestLimitReached;

	public TwitterRequest(List<TwitterUser> users, long cursor, int requestCount, boolean requestLimitReached) {
		this.users = users;
		this.cursor = cursor;
		this.requestCount = requestCount;
		this.requestLimitReached = requestLimitReached;
	}

	public TwitterRequest(List<TwitterTweet> tweets, int requestCount, boolean requestLimitReached) {
		this.tweets = tweets;
		this.requestCount = requestCount;
		this.requestLimitReached = requestLimitReached;
	}

}
