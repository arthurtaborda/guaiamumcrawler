package models.facebook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jongo.MongoCollection;

import uk.co.panaxiom.playjongo.PlayJongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.collect.Lists;

@JsonTypeInfo(use = Id.CLASS, property = "_class")
public abstract class FBProfile {

	@JsonProperty("_id")
	public String id;

	public String name;

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

	private static MongoCollection db(String name) {
		return PlayJongo.getCollection(name);
	}

	public static List<FBProfile> listSources() {
		List<String> ids = db("fbposts").distinct("profileId").as(String.class);
		List<FBProfile> profiles = Lists.newArrayList(db("fbprofiles").find("{_id: { $in: #}, feed: {$exists: #}}", ids, true).as(FBProfile.class).iterator());

		Map<String, FBProfile> map = new HashMap<>();
		for (FBProfile p : profiles) {
			map.put(p.id, p);
			System.out.println(p);
		}

		List<FBPost> posts = Lists.newArrayList(db("fbposts").find().as(FBPost.class).iterator());
		for (FBPost p : posts) {
			if (map.get(p.profileId).feed.posts == null) {
				map.get(p.profileId).feed.posts = new ArrayList<>();
			}

			map.get(p.profileId).feed.posts.add(p);
		}

		return profiles;
	}

	public void save() {
		db("fbprofiles").save(this);
	}

	public static FBProfile get(String id) {
		return db("fbprofiles").findOne("{_id: #}", id).as(FBProfile.class);
	}

	public static Boolean exists(String id) {
		return get(id) != null;
	}
}
