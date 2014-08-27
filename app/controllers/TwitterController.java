package controllers;

import global.Global;

import java.util.List;
import java.util.Set;

import models.twitter.TwitterTweet;
import models.twitter.TwitterUser;
import play.mvc.Controller;
import play.mvc.Result;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;
import views.html.ttTweets;
import akka.japi.Pair;
import crawler.TwitterCrawler;

public class TwitterController extends Controller {

	private static final TwitterCrawler crawler = Global.getTwitterCrawler();

	public static Result index(String username) {
		return redirect(routes.TwitterController.ttTweets(1, "uploadDate", "-", "", username));
	}

	public static Result ttTweets(int page, String sortBy, String order, String filter, String username) {
		if (username == null || username.isEmpty()) {
			return ok(ttTweets.render(TwitterTweet.list(page, 10, sortBy, order, filter), sortBy, order, filter, username, TwitterUser.getUsersWithTweets()));
		} else {
			return ok(ttTweets.render(TwitterTweet.list(page, 10, sortBy, order, filter, username), sortBy, order, filter, username,
					TwitterUser.getUsersWithTweets()));
		}
	}

	public static Result fetchTweets(String username) {
		Integer requestCount = 0;
		List<TwitterTweet> tweets = null;
		try {
			RateLimitStatus rateLimitStatus = crawler.getRateLimitStatus();

			System.out.println("  -------- " + username + " -------- ");
			System.out.println(" Limit: " + rateLimitStatus.getLimit());
			System.out.println(" Remaining: " + rateLimitStatus.getRemaining());
			System.out.println(" ResetTimeInSeconds: " + rateLimitStatus.getResetTimeInSeconds());
			System.out.println(" SecondsUntilReset: " + rateLimitStatus.getSecondsUntilReset());

			Pair<Integer, List<TwitterTweet>> pair = null;
			TwitterUser user = TwitterUser.get(username);
			if (user != null) {
				pair = crawler.getTweets(user, rateLimitStatus.getRemaining());
			} else {
				pair = crawler.getTweets(username, rateLimitStatus.getRemaining());
			}

			requestCount = pair.first();
			tweets = pair.second();
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		System.out.println(" RequestCount: " + requestCount);

		Set<Long> userIds = TwitterUser.getIds();
		Set<Long> tweetIds = TwitterTweet.getIds();
		for (TwitterTweet tweet : tweets) {
			if (!tweetIds.contains(tweet.id)) {
				if (tweet.user != null && !userIds.contains(tweet.user.id)) {
					tweet.user.save();
					userIds.add(tweet.user.id);
				}

				if (tweet.author != null && !userIds.contains(tweet.author.id)) {
					tweet.author.save();
					userIds.add(tweet.author.id);
				}

				tweet.save();
				tweetIds.add(tweet.id);
			}
		}

		return index(username);
	}
}
