package crawler;

import java.util.ArrayList;
import java.util.List;

import models.facebook.FacebookComment;
import models.facebook.FacebookPost;
import models.facebook.FacebookProfile;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonObject;
import com.restfb.types.Comment;
import com.restfb.types.Post;

public class FacebookCrawler {

	private FacebookClient facebookClient;

	public FacebookCrawler(FacebookClient facebookClient) {
		this.facebookClient = facebookClient;
	}

	public List<FacebookComment> fetchCommentsFromPostKey(String postKey) {
		Connection<JsonObject> connection = facebookClient.fetchConnection(postKey + "/comments", JsonObject.class,
				Parameter.with("fields", "from,message,like_count,created_time"), Parameter.with("limit", 250));

		List<FacebookComment> comments = new ArrayList<FacebookComment>();

		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Comment c = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Comment.class);

				FacebookComment fbComment = new FacebookComment(c.getId());
				FacebookProfile fbAuthor = FacebookProfile.findByProfileId(c.getFrom().getId());

				if (fbAuthor == null) {
					String type = "user";
					if (c.getFrom().getCategory() != null)
						type = "page";

					fbAuthor = new FacebookProfile(c.getFrom().getId(), c.getFrom().getName(), type);
					fbAuthor.save();
				}

				fbComment.setLikeCount(c.getLikeCount());
				fbComment.setMessage(c.getMessage());
				fbComment.setAuthor(fbAuthor);
				fbComment.setCreatedTime(c.getCreatedTime());

				comments.add(fbComment);
			}
		}

		return comments;
	}

	public FacebookProfile fetchSource(String sourceId) {

		JsonObject feed = facebookClient.fetchObject("v2.0/" + sourceId, JsonObject.class, Parameter.with("fields", "name"), Parameter.with("metadata", true));

		String sourceName = feed.getString("name");
		String type = feed.getJsonObject("metadata").getString("type");

		return new FacebookProfile(sourceId, sourceName, type);
	}

	public List<FacebookPost> fetchPosts(String sourceId, Integer limit) {
		return fetchPosts(sourceId, limit, new Parameter[] {});
	}

	public List<FacebookPost> fetchPostsSince(String sourceId, Integer limit, Long since) {
		return fetchPosts(sourceId, limit, Parameter.with("since", since));
	}

	public List<FacebookPost> fetchPostsUntil(String sourceId, Integer limit, Long until) {
		return fetchPosts(sourceId, limit, Parameter.with("until", until));
	}

	public List<FacebookPost> fetchPosts(String sourceId, Integer limit, Long since, Long until) {
		return fetchPosts(sourceId, limit, Parameter.with("since", since), Parameter.with("until", until));
	}

	private List<FacebookPost> fetchPosts(String sourceId, Integer limit, Parameter... parameters) {
		List<FacebookPost> posts = new ArrayList<FacebookPost>();

		FacebookProfile fbSource = FacebookProfile.findByProfileId(sourceId);

		if (fbSource == null) {
			fbSource = fetchSource(sourceId);
			fbSource.setIsSource(true);
			fbSource.save();
		} else if (!fbSource.isSource()) {
			fbSource.setIsSource(true);
			FacebookProfile.update(fbSource);
		}

		String feedUrl = "/posts";
		if (fbSource.getType().equals("group"))
			feedUrl = "/feed";

		Parameter[] param = new Parameter[parameters.length + 2];
		param[parameters.length] = Parameter.with("limit", limit);
		param[parameters.length + 1] = Parameter.with("fields",
				"status_type,from,id,message,link,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)");

		Connection<JsonObject> connection = facebookClient.fetchConnection("v2.0/" + sourceId + feedUrl, JsonObject.class, param);

		int i = 0;
		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Post p = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Post.class);

				// if is null, this type of post is useless, because it has
				// no comments or messages. It only happens with pages
				// apparently.
				if (fbSource.getType().equals("page") && p.getStatusType() == null) {
					continue;
				}

				if (!FacebookPost.exists(p.getId())) {
					FacebookPost fbPost = new FacebookPost(p.getId());
					FacebookProfile fbAuthor = FacebookProfile.findByProfileId(p.getFrom().getId());

					if (fbAuthor == null) {
						// if this is coming from a group, the author must be
						// user.
						fbAuthor = new FacebookProfile(p.getFrom().getId(), p.getFrom().getName(), fbSource.getType().equals("group") ? "user" : "page");
						fbAuthor.save();
					}

					fbPost.setPostKey(p.getId());
					fbPost.setMessage(p.getMessage() == null || p.getMessage().isEmpty() ? "<NO MESSAGE>" : p.getMessage());
					fbPost.setAuthor(fbAuthor);
					fbPost.setCreatedTime(p.getCreatedTime());
					fbPost.setCommentCount(!jsonObject.has("comments") ? 0 : jsonObject.getJsonObject("comments").getJsonObject("summary")
							.getLong("total_count"));
					fbPost.setLikeCount(!jsonObject.has("likes") ? 0 : jsonObject.getJsonObject("likes").getJsonObject("summary").getLong("total_count"));
					fbPost.setShareCount(p.getSharesCount());
					fbPost.setSource(fbSource);

					posts.add(fbPost);
					i++;
					if (i >= limit)
						break;
				}
			}
			if (i >= limit) {
				return posts;
			}
		}

		return posts;
	}

}
