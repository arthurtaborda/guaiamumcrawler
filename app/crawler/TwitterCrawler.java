package crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.twitter.TwitterRequest;
import models.twitter.TwitterTweet;
import models.twitter.TwitterUser;
import play.Logger;
import twitter4j.HttpResponseCode;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.google.common.collect.Lists;

public class TwitterCrawler {

	private Twitter twitter;

	public TwitterCrawler(Twitter twitter) {
		this.twitter = twitter;
	}

	public TwitterRequest getTweets(TwitterUser user, final int numberOfRequests) throws TwitterException {
		return getTweets(user, Long.MAX_VALUE, numberOfRequests);
	}

	public TwitterRequest getTweets(String username, final int numberOfRequests) throws TwitterException {
		TwitterUser user = getUser(username);

		return getTweets(user, Long.MAX_VALUE, numberOfRequests);
	}

	public Map<String, RateLimitStatus> getRateLimitStatus() throws TwitterException {
		return twitter.getRateLimitStatus();
	}

	public TwitterRequest getTweets(TwitterUser user, long lastId, final Integer numberOfRequests) throws TwitterException {
		return getTweets(user, 1, lastId, numberOfRequests);
	}

	public TwitterRequest getTweets(TwitterUser user, long sinceId, long lastId, final int numberOfRequests) throws TwitterException {
		int count = 200;
		List<Status> list = null;
		List<Status> statuses = Lists.newArrayList();

		if (lastId == 0)
			lastId = Long.MAX_VALUE;
		if (sinceId == 0)
			sinceId = 1;

		int requestCount = 0;
		do {
			try {
				list = twitter.getUserTimeline(user.username, new Paging(1, count, sinceId, lastId - 1));
				requestCount++;
				statuses.addAll(list);
				Logger.debug("Gathered " + list.size() + " tweets - Total: " + statuses.size() + " (" + user.username + ")");
				for (Status t : list)
					if (t.getId() < lastId)
						lastId = t.getId();
			} catch (TwitterException te) {
				if (te.exceededRateLimitation() || te.getErrorCode() == HttpResponseCode.UNAUTHORIZED) {
					break;
				}

				continue;
			}
		} while (list != null && list.size() > 0 && requestCount < numberOfRequests);

		List<TwitterTweet> tweets = new ArrayList<TwitterTweet>();
		for (Status status : statuses) {
			TwitterTweet t = new TwitterTweet();
			t.id = status.getId();
			t.retweetCount = status.getRetweetCount();
			t.favoriteCount = status.getFavoriteCount();
			t.createdTime = status.getCreatedAt();
			t.message = status.getText();
			t.userId = status.getUser().getId();
			t.isRetweet = status.isRetweet();

			try {
				Detector detector = DetectorFactory.create();
				detector.append(t.message);
				t.language = detector.detect();
			} catch (LangDetectException e) {
				t.language = null;
			}

			t.user = user;

			if (t.isRetweet) {
				if (status.getRetweetedStatus().getUser().getId() != t.userId) {
					User author = status.getRetweetedStatus().getUser();
					t.author = getUser(author);
				} else {
					t.author = user;
				}
			}

			tweets.add(t);
		}

		boolean gotAllTweets = list != null && (list.size() == 0 || user.tweetCount == list.size());
		boolean requestLimitReached = requestCount == numberOfRequests;
		TwitterRequest request = new TwitterRequest(tweets, requestCount, requestLimitReached);
		request.requestRemaining = this.getRateLimitStatus().get("/statuses/user_timeline").getRemaining();
		request.gotAllTweets = gotAllTweets;

		return request;
	}

	public TwitterUser getUser(String username) throws TwitterException {
		User u = twitter.showUser(username);

		return getUser(u);
	}

	private TwitterUser getUser(User u) {
		TwitterUser user = new TwitterUser(u.getId(), u.getName(), u.getScreenName(), u.getLocation(), u.getDescription(), u.getFollowersCount(),
				u.getFriendsCount());

		user.tweetCount = u.getStatusesCount();

		return user;
	}

	private TwitterRequest getFollowersOrFriends(String type, String username, long nextCursor, int requests) throws TwitterException {
		List<TwitterUser> followers = new ArrayList<TwitterUser>();

		List<User> users = Lists.newArrayList();
		int remaining = requests;
		do {
			PagableResponseList<User> usersResponse = null;

			if (type.equals("followers")) {
				usersResponse = twitter.getFollowersList(username, nextCursor, 200);
			} else {
				usersResponse = twitter.getFriendsList(username, nextCursor, 200);
			}
			Logger.debug("Fetching " + usersResponse.size() + " " + type);
			nextCursor = usersResponse.getNextCursor();
			users.addAll(usersResponse);
			remaining--;
		} while (nextCursor > 0 && remaining > 0);

		for (User user : users) {
			followers.add(getUser(user));
		}

		TwitterRequest request = new TwitterRequest(followers, nextCursor, requests - remaining, remaining == 0);

		return request;
	}

	public TwitterRequest getFollowers(String username) throws TwitterException {
		return getFollowers(username, -1);
	}

	public TwitterRequest getFriends(String username) throws TwitterException {
		return getFriends(username, -1);
	}

	public TwitterRequest getFollowers(String username, long cursor) throws TwitterException {
		Map<String, RateLimitStatus> map = this.getRateLimitStatus();
		RateLimitStatus rls = map.get("/followers/list");
		int requests = rls.getRemaining();
		return getFollowersOrFriends("followers", username, cursor, requests);
	}

	public TwitterRequest getFriends(String username, long cursor) throws TwitterException {
		Map<String, RateLimitStatus> map = this.getRateLimitStatus();
		RateLimitStatus rls = map.get("/friends/list");
		int requests = rls.getRemaining();
		return getFollowersOrFriends("friends", username, cursor, requests);
	}

	public TwitterRequest getFollowers(String username, long cursor, int requests) throws TwitterException {
		return getFollowersOrFriends("followers", username, cursor, requests);
	}

	public TwitterRequest getFriends(String username, long cursor, int requests) throws TwitterException {
		return getFollowersOrFriends("friends", username, cursor, requests);
	}

}
