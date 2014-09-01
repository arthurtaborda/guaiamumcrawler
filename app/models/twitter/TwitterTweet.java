package models.twitter;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import models.Page;
import models.facebook.FBComment;

import org.jongo.Find;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import db.Mongo;

public class TwitterTweet {

	@JsonProperty("_id")
	public Long id;

	public Integer retweetCount;

	public Integer favoriteCount;

	public Date createdTime;

	public String message;

	public String language;

	public Long userId;

	public Long authorId;

	@JsonIgnore
	public TwitterUser user;

	@JsonIgnore
	public TwitterUser author;

	public boolean isRetweet;

	public void save() {
		Mongo.get("tweets").save(this);
	}

	public static Set<Long> getIds() {
		Iterator<TwitterUser> it = Mongo.get("tweets").find("{}, {_id: 1}").as(TwitterUser.class).iterator();

		Set<Long> ids = Sets.newHashSet();
		while (it.hasNext()) {
			TwitterUser user = it.next();
			ids.add(user.id);
		}

		return ids;
	}

	public static TwitterTweet getFirstTweet(long id) {
		return Mongo.get("tweets").find("{userId: #}", id).limit(1).sort("{createdTime: 1}").as(TwitterTweet.class).next();
	}

	public static long getFirstTweetUnixTime(long id) {
		TwitterTweet t = getFirstTweet(id);

		if (t != null)
			return t.createdTime.getTime();

		return 0;
	}

	public static long getLastTweetUnixTime(long id) {
		TwitterTweet t = getLastTweet(id);

		if (t != null)
			return t.createdTime.getTime();

		return 0;
	}

	public static TwitterTweet getLastTweet(long id) {
		return Mongo.get("tweets").find("{userId: #}", id).limit(1).sort("{createdTime: -1}").as(TwitterTweet.class).next();
	}

	public static Page<TwitterTweet> list(int page, int limit, String sort, String order, String filter) {
		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		Find f = Mongo.get("tweets").find("{message: #}", regex);

		return list(page, limit, sort, order, filter, f);
	}

	public static Page<TwitterTweet> list(int page, int limit, String sort, String order, String filter, String username) {
		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		Long id = TwitterUser.getId(username);
		Find f = Mongo.get("tweets").find("{message: #, userId: #}", regex, id);

		return list(page, limit, sort, order, filter, f);
	}

	private static Page<TwitterTweet> list(int page, int limit, String sort, String order, String filter, Find f) {
		List<TwitterTweet> data = Lists.newArrayList();
		int total = f.as(FBComment.class).count();
		page = Page.adjustPage(page, total, limit);

		if (total > 0) {
			f = f.limit(limit).skip((page - 1) * limit).sort("{" + sort + ": " + order + "1}");

			Set<Long> ids = Sets.newHashSet();
			for (Iterator<TwitterTweet> iterator = f.as(TwitterTweet.class).iterator(); iterator.hasNext();) {
				TwitterTweet fbComment = iterator.next();
				data.add(fbComment);
				ids.add(fbComment.userId);
			}

			List<TwitterUser> users = Lists.newArrayList(Mongo.get("ttusers").find("{_id: { $in: #}}", ids).as(TwitterUser.class).iterator());

			Map<Long, TwitterUser> profileMap = Maps.newHashMap();
			for (TwitterUser fbProfile : users) {
				profileMap.put(fbProfile.id, fbProfile);
			}

			for (TwitterTweet p : data) {
				p.user = profileMap.get(p.userId);
			}
		}

		return new Page<TwitterTweet>(data, total, page, limit);
	}
}
