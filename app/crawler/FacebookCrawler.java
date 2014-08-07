package crawler;

import java.util.ArrayList;
import java.util.List;

import models.facebook.FBComment;
import models.facebook.FBGroup;
import models.facebook.FBPage;
import models.facebook.FBPost;
import models.facebook.FBProfile;
import models.facebook.FBUser;

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

	public List<FBComment> fetchCommentsFromPostId(String postId) {
		Connection<JsonObject> connection = facebookClient.fetchConnection(postId + "/comments", JsonObject.class,
				Parameter.with("fields", "from,message,like_count,created_time"), Parameter.with("filter", "stream"));

		List<FBComment> comments = new ArrayList<FBComment>();

		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Comment c = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Comment.class);

				FBComment fbComment = new FBComment(c.getId());
				FBProfile fbAuthor = FBProfile.get(c.getFrom().getId());

				if (fbAuthor == null) {
					if (c.getFrom().getCategory() != null)
						fbAuthor = new FBPage(c.getFrom().getId(), c.getFrom().getName());
					else
						fbAuthor = new FBUser(c.getFrom().getId(), c.getFrom().getName());

					fbAuthor.save();
				}

				fbComment.authorId = fbAuthor.id;
				fbComment.postId = postId;
				fbComment.likeCount = c.getLikeCount();
				fbComment.message = c.getMessage();
				fbComment.createdTime = c.getCreatedTime();

				comments.add(fbComment);
			}
		}

		return comments;
	}

	public FBProfile fetchProfileFeed(String profileId) {
		JsonObject feed = facebookClient.fetchObject("v2.0/" + profileId, JsonObject.class, Parameter.with("fields", "name"), Parameter.with("metadata", true));

		profileId = feed.getString("id");
		String profileName = feed.getString("name");
		String type = feed.getJsonObject("metadata").getString("type");

		FBProfile profile = FBProfile.get(profileId);
		if (profile == null) {
			if (type.equals("page")) {
				profile = new FBPage(profileId, profileName);
			} else if (type.equals("group")) {
				profile = new FBGroup(profileId, profileName);
			} else if (type.equals("user")) {
				profile = new FBUser(profileId, profileName);
			}
		}

		return profile;
	}

	public List<FBPost> fetchPosts(String sourceId, String sourceType, Integer limit) {
		return fetchPosts(sourceId, sourceType, limit, new Parameter[] {});
	}

	public List<FBPost> fetchPostsSince(String sourceId, String sourceType, Integer limit, Long since) {
		return fetchPosts(sourceId, sourceType, limit, Parameter.with("since", since));
	}

	public List<FBPost> fetchPostsUntil(String sourceId, String sourceType, Integer limit, Long until) {
		return fetchPosts(sourceId, sourceType, limit, Parameter.with("until", until));
	}

	public List<FBPost> fetchPosts(String profileId, String profileType, Integer limit, Long since, Long until) {
		return fetchPosts(profileId, profileType, limit, Parameter.with("since", since), Parameter.with("until", until));
	}

	private List<FBPost> fetchPosts(String profileId, String profileType, Integer limit, Parameter... parameters) {
		List<FBPost> posts = new ArrayList<FBPost>();

		String feedUrl = "/posts";
		if (profileType.equals("group"))
			feedUrl = "/feed";

		Parameter[] param = new Parameter[parameters.length + 2];
		param[parameters.length] = Parameter.with("limit", limit);
		param[parameters.length + 1] = Parameter.with("fields",
				"status_type,from,id,message,link,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)");

		Connection<JsonObject> connection = facebookClient.fetchConnection("v2.0/" + profileId + feedUrl, JsonObject.class, param);

		int i = 0;
		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Post p = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Post.class);

				// if is null, this type of post is useless, because it has
				// no comments or messages. It only happens with pages
				// apparently.
				if (profileType.equals("page") && p.getStatusType() == null) {
					continue;
				}

				String postId = p.getId();

				if (!FBPost.exists(postId)) {
					FBPost fbPost = new FBPost(postId);
					FBProfile fbAuthor = FBProfile.get(p.getFrom().getId());

					if (fbAuthor == null) {
						// if this is coming from a group, the author must be
						// user.
						if (profileType.equals("group") || profileType.equals("user")) {
							fbAuthor = new FBUser(p.getFrom().getId(), p.getFrom().getName());
						} else {
							fbAuthor = new FBPage(p.getFrom().getId(), p.getFrom().getName());
						}
						fbAuthor.save();
					}

					fbPost.id = postId;
					fbPost.profileId = profileId;
					fbPost.authorId = fbAuthor.id;
					fbPost.message = p.getMessage() == null || p.getMessage().isEmpty() ? "<NO MESSAGE>" : p.getMessage();
					fbPost.createdTime = p.getCreatedTime();
					fbPost.commentCount = !jsonObject.has("comments") ? 0 : jsonObject.getJsonObject("comments").getJsonObject("summary")
							.getLong("total_count");
					fbPost.likeCount = !jsonObject.has("likes") ? 0 : jsonObject.getJsonObject("likes").getJsonObject("summary").getLong("total_count");
					fbPost.shareCount = p.getSharesCount();

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
