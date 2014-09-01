/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facebook;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import models.Page;
import models.facebook.profile.FBProfile;

import org.jongo.Find;
import org.jongo.MongoCollection;

import uk.co.panaxiom.playjongo.PlayJongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author arthur
 *
 */
public class FBPost {

	@JsonProperty("_id")
	public String id;

	public String message;

	public Date createdTime;

	public String authorId;

	public String profileId;

	@JsonIgnore
	public FBProfile author;

	@JsonIgnore
	public FBProfile profile;

	@JsonIgnore
	public List<FBComment> comments;

	public Long commentCount;

	public Long likeCount;

	public Long shareCount;

	// necessary
	public FBPost() {
	}

	public FBPost(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FBPost)) {
			return false;
		}

		if (((FBPost) o).id.equals(this.id)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	public void setId() {
	}

	public void setLink() {
	}

	public String getLink() {
		String[] ids = this.id.toString().split("_");
		return "http://facebook.com/" + ids[0] + "/posts/" + ids[1];
	}

	private static MongoCollection db(String name) {
		return PlayJongo.getCollection(name);
	}

	public static Page<FBPost> list(int page, int limit, String sort, String order, String filter) {
		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		Find f = db("fbposts").find("{message: #}", regex);

		return list(page, limit, sort, order, filter, f);
	}

	public static Page<FBPost> list(int page, int limit, String sort, String order, String filter, String profileId) {
		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		Find f = db("fbposts").find("{message: #, profileId: #}", regex, profileId);

		return list(page, limit, sort, order, filter, f);
	}

	private static Page<FBPost> list(int page, int limit, String sort, String order, String filter, Find f) {
		List<FBPost> data = Lists.newArrayList();
		int total = f.as(FBPost.class).count();
		page = Page.adjustPage(page, total, limit);

		if (total > 0) {
			f = f.limit(limit).skip((page - 1) * limit).sort("{" + sort + ": " + order + "1}");

			Set<String> ids = Sets.newHashSet();
			for (Iterator<FBPost> iterator = f.as(FBPost.class).iterator(); iterator.hasNext();) {
				FBPost fbPost = iterator.next();
				data.add(fbPost);
				ids.add(fbPost.profileId);
			}

			List<FBProfile> profiles = Lists.newArrayList(db("fbprofiles").find("{_id: { $in: #}}", ids).as(FBProfile.class).iterator());

			Map<String, FBProfile> profileMap = Maps.newHashMap();
			for (FBProfile fbProfile : profiles) {
				profileMap.put(fbProfile.id, fbProfile);
			}

			for (FBPost p : data) {
				p.profile = profileMap.get(p.profileId);
			}
		}

		return new Page<FBPost>(data, total, page, limit);
	}

	public static Set<String> getIds() {
		Iterator<FBPost> it = db("fbposts").find("{}, {_id: 1}").as(FBPost.class).iterator();

		Set<String> ids = Sets.newHashSet();
		while (it.hasNext()) {
			FBPost post = it.next();
			ids.add(post.id);
		}

		return ids;
	}

	public void save() {
		db("fbposts").save(this);
	}

	public static FBPost get(String id) {
		return db("fbposts").findOne("{_id: #}", id).as(FBPost.class);
	}

	public static Boolean exists(String id) {
		return get(id) != null;
	}
}
