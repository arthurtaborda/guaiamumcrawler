package models.facebook.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.facebook.FBFeed;
import models.facebook.FBPost;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.collect.Lists;

import db.Mongo;

@JsonTypeInfo(use = Id.CLASS, property = "_class")
public abstract class FBProfile {

	@JsonProperty("_id")
	public String id;

	public String name;

	public String username;

	public FBFeed feed;

	@JsonIgnore
	public String link;

	public FBProfile() {
	}

	public FBProfile(String id) {
		this.id = id;
	}

	public FBProfile(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public FBProfile(String id, String name, String username) {
		this.id = id;
		this.name = name;
		this.username = username;
	}

	public void setId() {
	}

	public void setLink() {
	}

	public abstract String getLink();

	public abstract String getType();

	@Override
	public String toString() {
		return "FBProfile [id=" + id + ", name=" + name + ", feed=" + feed + ", link=" + link + "]";
	}

	public static List<FBProfile> listSources() {
		List<String> ids = Mongo.get("fbposts").distinct("profileId").as(String.class);
		List<FBProfile> profiles = Lists.newArrayList(Mongo.get("fbprofiles").find("{_id: { $in: #}, feed: {$exists: #}}", ids, true).as(FBProfile.class)
				.iterator());

		Map<String, FBProfile> map = new HashMap<>();
		for (FBProfile p : profiles) {
			if (p.id != null)
				map.put(p.id, p);
		}

		List<FBPost> posts = Lists.newArrayList(Mongo.get("fbposts").find("{profileId: {$exists: #}}", true).as(FBPost.class).iterator());
		for (FBPost p : posts) {
			if (map.get(p.profileId).feed.posts == null) {
				map.get(p.profileId).feed.posts = new ArrayList<>();
			}

			map.get(p.profileId).feed.posts.add(p);
		}

		return profiles;
	}

	public void save() {
		Mongo.get("fbprofiles").save(this);
	}

	public static FBProfile get(String id) {
		FBProfile p = Mongo.get("fbprofiles").findOne("{_id: #}", id).as(FBProfile.class);

		if (p == null) {
			p = Mongo.get("fbprofiles").findOne("{username: #}", id).as(FBProfile.class);
		}

		return p;
	}

	public static Boolean exists(String id) {
		return get(id) != null;
	}
}
