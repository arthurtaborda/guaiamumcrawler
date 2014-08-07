package models.facebook;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import models.Page;

import org.jongo.Find;
import org.jongo.MongoCollection;

import uk.co.panaxiom.playjongo.PlayJongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

/**
 *
 * @author arthurtaborda
 */
public class FBComment {

	@JsonProperty("_id")
	public String id;

	public String message;

	public Date createdTime;

	public String authorId;

	public String postId;

	@JsonIgnore
	public FBPost post;

	@JsonIgnore
	public FBProfile author;

	public Long likeCount;

	public FBComment() {
	}

	public FBComment(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FBComment)) {
			return false;
		}

		if (((FBComment) o).id.equals(this.id)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "FBComment [id=" + id + ", message=" + message + ", createdTime=" + createdTime + ", post=" + post + ", author=" + author + ", likeCount="
				+ likeCount + "]";
	}

	private static MongoCollection db(String name) {
		return PlayJongo.getCollection(name);
	}

	public static Page<FBComment> list(int page, int limit, String sort, String order, String filter, String postId) {
		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		Find f = db("fbcomments").find("{message: #, postId: #}", regex, postId);

		List<FBComment> data = new ArrayList<>();
		int total = f.as(FBComment.class).count();
		page = Page.adjustPage(page, total, limit);

		if (total > 0) {
			f = f.limit(limit).skip((page - 1) * limit).sort("{" + sort + ": " + order + "1}");

			Set<String> ids = new HashSet<>();
			for (Iterator<FBComment> iterator = f.as(FBComment.class).iterator(); iterator.hasNext();) {
				FBComment fbComment = iterator.next();
				data.add(fbComment);
				ids.add(fbComment.authorId);
			}

			List<FBProfile> authors = Lists.newArrayList(db("fbprofiles").find("{_id: { $in: #}}", ids).as(FBProfile.class).iterator());

			Map<String, FBProfile> profileMap = new HashMap<>();
			for (FBProfile fbProfile : authors) {
				profileMap.put(fbProfile.id, fbProfile);
			}

			for (FBComment p : data) {
				p.author = profileMap.get(p.authorId);
			}
		}

		return new Page<FBComment>(data, total, page, limit);
	}

	public static List<FBComment> listCommentsById(List<String> ids) {
		return Lists.newArrayList(db("fbcomments").find("{_id: { $in: #}}", ids).as(FBComment.class).iterator());
	}

	public void save() {
		db("fbcomments").save(this);
	}
}
