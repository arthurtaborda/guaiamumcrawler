package crawler;

import java.util.ArrayList;
import java.util.List;

import models.twitter.TwitterTweet;
import models.twitter.TwitterUser;
import play.Logger;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import akka.japi.Pair;

import com.google.common.collect.Lists;

public class TwitterCrawler {

	private Twitter twitter;

	public TwitterCrawler(Twitter twitter) {
		this.twitter = twitter;
	}

	public Pair<Integer, List<TwitterTweet>> getTweets(TwitterUser user, final Integer rateLimit) throws TwitterException {
		return getTweets(user, user.feed.lastTweetScannedId, rateLimit);
	}

	public Pair<Integer, List<TwitterTweet>> getTweets(String username, final Integer rateLimit) throws TwitterException {
		TwitterUser user = getUser(twitter.showUser(username));
		long lastId = Long.MAX_VALUE;

		return getTweets(user, lastId, rateLimit);
	}

	public RateLimitStatus getRateLimitStatus() throws TwitterException {
		return twitter.getRateLimitStatus().get("/statuses/user_timeline");
	}

	private Pair<Integer, List<TwitterTweet>> getTweets(TwitterUser user, Long lastId, final Integer rateLimit) throws TwitterException {
		//int maxTweets = 3300; //twiter won't let me get more than this in one request
		int count = 200;
		List<Status> list = null;
		List<Status> statuses = Lists.newArrayList();

		if (lastId == null || lastId == 0)
			lastId = 999999999999999999l;

		int requestCount = 0;
		do {
			try {
				list = twitter.getUserTimeline(user.username, new Paging(1, count, 1, lastId - 1));
				requestCount++;
				statuses.addAll(list);
				Logger.debug("Gathered " + list.size() + " tweets - Total: " + statuses.size());
				for (Status t : list)
					if (t.getId() < lastId)
						lastId = t.getId();
				//				if (maxTweets - statuses.size() < count) {
				//					count = maxTweets - statuses.size();
				//					user.maxTweetId = lastId;
				//				}
			} catch (TwitterException te) {
				Logger.debug("Couldn't connect: " + te);

				if (te.exceededRateLimitation()) {
					user.feed.lastTweetScannedId = lastId;
					break;
				}
			}
		} while (list.size() > 0 && requestCount < rateLimit);

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

		Pair<Integer, List<TwitterTweet>> pair = new Pair<>(requestCount, tweets);

		return pair;
	}

	private TwitterUser getUser(User u) {
		TwitterUser user = new TwitterUser(u.getId(), u.getName(), u.getScreenName(), u.getLocation(), u.getDescription(), u.getFollowersCount(),
				u.getFriendsCount());

		return user;
	}

	public List<TwitterUser> getFollowers(Long userId) throws TwitterException {
		List<TwitterUser> followers = new ArrayList<TwitterUser>();

		ArrayList<User> users = new ArrayList<User>();
		long nextCursor = -1;
		do {
			PagableResponseList<User> usersResponse = twitter.getFollowersList(userId, nextCursor);
			Logger.debug("size() of first iteration:" + usersResponse.size());
			nextCursor = usersResponse.getNextCursor();
			users.addAll(usersResponse);
		} while (nextCursor > 0);

		return followers;
	}

}
