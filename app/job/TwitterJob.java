package job;

import global.Global;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.twitter.TwitterFeed;
import models.twitter.TwitterRequest;
import models.twitter.TwitterTweet;
import models.twitter.TwitterUser;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import play.Logger;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

import com.google.common.collect.Sets;

import crawler.TwitterCrawler;

public class TwitterJob implements Job {

	private TwitterCrawler crawler;
	private Map<String, RateLimitStatus> rateLimitStatus;
	private String language;
	private int languagePercentage;

	private int timelineRequests;

	Set<Long> userIDs;
	Set<String> usernames;
	Set<TwitterUser> usersToRescan;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Logger.info("FACEBOOK JOB STARTED");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		languagePercentage = Integer.valueOf(dataMap.getString("languagePercentage"));
		language = dataMap.getString("language");
		usernames = getSources(dataMap.getString("sourceFile"));

		userIDs = TwitterUser.getIds();
		usersToRescan = TwitterUser.getUsersToRescan();

		Set<String> u2 = Sets.newHashSet();
		for (String username : usernames) {
			TwitterUser u = TwitterUser.get(username);
			if (u != null) {
				usersToRescan.add(u);
			} else {
				u2.add(username);
			}
		}
		usernames = u2;

		crawler = Global.getTwitterCrawler();

		try {
			crawl();
		} catch (Exception e) {
			e.printStackTrace();

			JobExecutionException e2 = new JobExecutionException(e);
			e2.setRefireImmediately(true);
			throw e2;
		}
	}

	private void crawl() throws TwitterException {
		RateLimitStatus timelineLS = null;

		do {
			try {
				rateLimitStatus = crawler.getRateLimitStatus();
				timelineLS = rateLimitStatus.get("/statuses/user_timeline");
				timelineRequests = timelineLS.getRemaining();
			} catch (TwitterException e) {
				waitFor(899);
			}
		} while (timelineLS == null);

		long averageTimePost = 0;
		TwitterRequest request = null;

		Logger.debug(" Limit: " + timelineLS.getLimit());
		Logger.debug(" Remaining: " + timelineLS.getRemaining());
		Logger.debug(" ResetTimeInSeconds: " + timelineLS.getResetTimeInSeconds());
		Logger.debug(" SecondsUntilReset: " + timelineLS.getSecondsUntilReset());
		for (String username : usernames) {
			System.out.println("  -------- " + username + " -------- ");
			Logger.debug("New user: " + username);

			if (timelineRequests <= 0) {
				waitFor(timelineLS.getSecondsUntilReset());
				timelineRequests = timelineLS.getLimit();
			}

			TwitterUser ttUser = crawler.getUser(username);
			request = crawler.getTweets(ttUser, timelineLS.getRemaining());
			timelineRequests = request.requestRemaining;
			averageTimePost = getAveragePostTime(request.tweets);

			Set<TwitterUser> newUsers = persistData(ttUser, request, averageTimePost);
			usersToRescan.addAll(newUsers);
		}

		for (TwitterUser ttUser : usersToRescan) {
			if (timelineRequests <= 0) {
				waitFor(timelineLS.getSecondsUntilReset());
				timelineRequests = timelineLS.getLimit();
			}

			Logger.debug("  -------- " + ttUser.username + " -------- ");
			String log = "Fetching tweets of " + ttUser.username;
			Long now = (new Date()).getTime();

			if (ttUser.feed == null)
				ttUser.feed = new TwitterFeed();

			if (!ttUser.feed.totallyScanned) { // if is not complete, get from the older of the list
				Logger.debug(log + " because is not totally scanned");
				request = crawler.getTweets(ttUser, TwitterTweet.getFirstTweetUnixTime(ttUser.id), Long.MAX_VALUE, timelineRequests);

				averageTimePost = getAveragePostTime(request.tweets);
			} else if (ttUser.feed.averageTimePost > 0 && now - ttUser.feed.lastTimeScanned > ttUser.feed.averageTimePost) { // if is complete, do a rescan for newer posts
				Logger.debug(log + " because was last scanned in " + (now - ttUser.feed.lastTimeScanned) + " and average time post is "
						+ ttUser.feed.averageTimePost);

				request = crawler.getTweets(ttUser, TwitterTweet.getLastTweetUnixTime(ttUser.id), timelineRequests);

				averageTimePost = (getAveragePostTime(request.tweets) + ttUser.feed.averageTimePost) / 2;
			} else {
				Logger.debug("Skipping " + ttUser.username);
				continue;
			}

			timelineRequests = request.requestRemaining;
			Set<TwitterUser> newUsers = persistData(ttUser, request, averageTimePost);
			usersToRescan.addAll(newUsers);
		}
	}

	private Set<TwitterUser> persistData(TwitterUser ttUser, TwitterRequest request, long averageTimePost) throws TwitterException {
		int usersSaved = 0;
		int tweetsSaved = 0;
		Set<TwitterUser> usersToRescan = Sets.newHashSet();

		List<TwitterTweet> tweets = request.tweets;

		ttUser.feed = new TwitterFeed();
		ttUser.feed.averageTimePost = averageTimePost;
		ttUser.feed.lastTimeScanned = new Date().getTime();
		ttUser.feed.totallyScanned = request.gotAllTweets;

		ttUser.save();
		usersSaved++;
		userIDs.add(ttUser.id);

		Set<TwitterUser> newUsers = Sets.newHashSet();
		for (TwitterTweet tweet : tweets) {
			if (tweet.author != null && userIDs.add(tweet.author.id)) {
				newUsers.add(tweet.author);
			}

			tweet.save();
			tweetsSaved++;
		}

		long friendCursor = -1;
		long followerCursor = -1;
		TwitterRequest reqFriends = null;
		TwitterRequest reqFollowers = null;

		RateLimitStatus timelineLS = crawler.getRateLimitStatus().get("/statuses/user_timeline");
		timelineRequests = timelineLS.getRemaining();
		do {
			reqFriends = getFriends(ttUser.username, friendCursor);
			if (reqFriends != null) {
				for (TwitterUser newUser : reqFriends.users) {
					if (userIDs.add(newUser.id)) {
						newUsers.add(newUser);
					}
				}
				friendCursor = reqFriends.cursor;
			}

			reqFollowers = getFollowers(ttUser.username, followerCursor);
			if (reqFollowers != null) {
				for (TwitterUser newUser : reqFollowers.users) {
					if (userIDs.add(newUser.id)) {
						newUsers.add(newUser);
					}
				}
				followerCursor = reqFollowers.cursor;
			}

			for (TwitterUser newUser : newUsers) {
				TwitterRequest req = null;
				do {
					try {
						Logger.debug("Requests remaining: " + timelineRequests);
						if (timelineRequests <= 1) {
							timelineLS = crawler.getRateLimitStatus().get("/statuses/user_timeline");
							waitFor(timelineLS.getSecondsUntilReset());
							timelineRequests = timelineLS.getLimit();
						}

						req = crawler.getTweets(newUser, 1);
						timelineRequests = req.requestRemaining;
						tweets = req.tweets;
					} catch (TwitterException e) {
						if (e.exceededRateLimitation()) {
							waitFor(timelineLS.getSecondsUntilReset());
							timelineRequests = timelineLS.getLimit();
						}
					}
				} while (req == null);

				if (tweets.size() == 0)
					continue;

				int languageCount = 0;
				for (TwitterTweet tweet : tweets) {
					if (tweet.language != null && tweet.language.equals(language))
						languageCount++;
				}

				int percentage = (languageCount * 100) / tweets.size();

				if (percentage >= languagePercentage) {
					newUser.toRescan = true;
					newUser.feed = new TwitterFeed();
					newUser.feed.lastTimeScanned = new Date().getTime();
					newUser.feed.totallyScanned = req.gotAllTweets;
					newUser.feed.averageTimePost = getAveragePostTime(tweets);

					usersToRescan.add(newUser);

					for (TwitterTweet tweet : tweets) {
						tweet.save();
						tweetsSaved++;
					}
				} else {
					newUser.toRescan = false;
				}

				newUser.save();
				usersSaved++;
			}

			newUsers.clear();

			Logger.debug("users saved: " + usersSaved);
			Logger.debug("tweets saved: " + tweetsSaved);
		} while ((reqFriends != null && reqFollowers != null) && reqFriends.requestLimitReached || reqFollowers.requestLimitReached);

		return usersToRescan;
	}

	private TwitterRequest getFriends(String username, long cursor) {
		TwitterRequest req = null;

		int errorCount = 0;
		while (errorCount < 10) {
			try {
				RateLimitStatus ls = crawler.getRateLimitStatus().get("/friends/list");
				if (ls.getRemaining() == 0) {
					waitFor(ls.getSecondsUntilReset());
				}

				req = crawler.getFriends(username, cursor, 5);
				break;
			} catch (TwitterException e) {
				errorCount++;
				e.printStackTrace();
			}
		}

		return req;
	}

	private TwitterRequest getFollowers(String username, long cursor) {
		TwitterRequest req = null;

		int errorCount = 0;
		while (errorCount < 10) {
			try {
				RateLimitStatus ls = crawler.getRateLimitStatus().get("/followers/list");

				if (ls.getRemaining() == 0) {
					waitFor(ls.getSecondsUntilReset());
				}

				req = crawler.getFollowers(username, cursor, 5);
				break;
			} catch (TwitterException e) {
				errorCount++;
				e.printStackTrace();
			}
		}

		return req;
	}

	private void waitFor(int seconds) {
		Logger.info("Waiting for " + seconds + " seconds until twitter rate limit resets");

		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Long getAveragePostTime(List<TwitterTweet> tweets) {
		if (tweets == null || tweets.size() == 0)
			return 0l;

		Long averagePostTime = 0l;

		long a = new Date().getTime();
		for (int i = 0; i < tweets.size() - 1; i++) {
			long b = tweets.get(i + 1).createdTime.getTime();
			averagePostTime += a - b;
			a = b;
		}

		averagePostTime /= tweets.size();
		return averagePostTime;
	}

	private Set<String> getSources(String sourceFile) {
		Set<String> sources = Sets.newHashSet();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(play.Play.application().getFile(sourceFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sources.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sources;
	}

}
