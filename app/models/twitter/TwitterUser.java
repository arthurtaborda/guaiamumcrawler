package models.twitter;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import db.Mongo;

public class TwitterUser {

	@JsonProperty("_id")
	public Long id;

	public String name;

	public String username;

	public String location;

	public String description;

	public Integer tweetCount;

	public Integer followerCount;

	public Integer followingCount;

	public TwitterFeed feed;

	public boolean toRescan;

	public TwitterUser() {
	}

	public TwitterUser(Long id, String name, String username, String location, String description, Integer followerCount, Integer followingCount) {
		this.id = id;
		this.name = name;
		this.username = username;
		this.location = location;
		this.description = description;
		this.followerCount = followerCount;
		this.followingCount = followingCount;
	}

	public static TwitterUser get(String username) {
		try {
			TwitterUser user = Mongo.get("ttusers").find("{username: #}", username).as(TwitterUser.class).next();
			return user;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static Set<Long> getIds() {
		Iterator<TwitterUser> it = Mongo.get("ttusers").find("{}, {_id: 1}").as(TwitterUser.class).iterator();

		Set<Long> ids = Sets.newHashSet();
		while (it.hasNext()) {
			TwitterUser user = it.next();
			ids.add(user.id);
		}

		return ids;
	}

	public static Set<TwitterUser> getUsersToRescan() {
		Set<TwitterUser> set = Sets.newHashSet(Mongo.get("ttusers").find("{toRescan: #}", true).as(TwitterUser.class).iterator());

		return set;
	}

	public void save() {
		Mongo.get("ttusers").save(this);
	}

	public static Long getId(String username) {
		TwitterUser user = get(username);
		if (user != null)
			return user.id;
		return 0l;
	}

	public static List<TwitterUser> getUsersWithTweets() {
		try {
			List<Long> ids = Mongo.get("tweets").distinct("userId").as(Long.class);
			List<TwitterUser> user = Lists.newArrayList(Mongo.get("ttusers").find("{_id: {$in: #}}", ids).as(TwitterUser.class).iterator());
			return user;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;

		TwitterUser user = null;
		if (obj instanceof TwitterUser) {
			user = (TwitterUser) obj;

			if (this.id == user.id || this.username == user.username)
				equals = true;
		}
		return equals;
	}
}
