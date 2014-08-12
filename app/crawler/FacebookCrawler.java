package crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import models.facebook.FBComment;
import models.facebook.FBFeed;
import models.facebook.FBPost;
import models.facebook.profile.FBGroup;
import models.facebook.profile.FBPage;
import models.facebook.profile.FBProfile;
import models.facebook.profile.FBUser;

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

	public FBProfile fetchProfile(String profileId) {
		JsonObject feed = null;

		try {
			feed = facebookClient.fetchObject("v2.0/" + profileId, JsonObject.class, Parameter.with("fields", "name,username"),
					Parameter.with("metadata", true));
		} catch (Exception e) {
			feed = facebookClient.fetchObject("v2.0/" + profileId, JsonObject.class, Parameter.with("fields", "name,email"), Parameter.with("metadata", true));
		}

		profileId = feed.getString("id");

		String username = null;
		String profileName = feed.getString("name");
		String type = feed.getJsonObject("metadata").getString("type");
		FBProfile profile = FBProfile.get(profileId);

		try {
			username = feed.getString("username");
		} catch (Exception e) {
			username = null;
		}
		if (profile == null) {
			if (type.equals("page")) {
				profile = new FBPage(profileId, profileName, username);
			} else if (type.equals("group")) {
				username = feed.getString("email").split("@")[0];
				profile = new FBGroup(profileId, profileName, username);
			} else if (type.equals("user")) {
				profile = new FBUser(profileId, profileName, username);
			}
		}

		if (profile.feed == null)
			profile.feed = new FBFeed();

		return profile;
	}

	public List<FBPost> fetchPosts(FBProfile profile, Integer limit) {
		return fetchPosts(profile, limit, new Parameter[] {});
	}

	public List<FBPost> fetchPostsSince(FBProfile profile, Integer limit, Long since) {
		return fetchPosts(profile, limit, Parameter.with("since", since));
	}

	public List<FBPost> fetchPostsUntil(FBProfile profile, Integer limit, Long until) {
		return fetchPosts(profile, limit, Parameter.with("until", until));
	}

	public List<FBPost> fetchPosts(FBProfile profile, Integer limit, Long since, Long until) {
		return fetchPosts(profile, limit, Parameter.with("since", since), Parameter.with("until", until));
	}

	private List<FBPost> fetchPosts(FBProfile profile, Integer limit, Parameter... parameters) {
		List<FBPost> posts = new ArrayList<FBPost>();
		String profileId = profile.id;
		String profileType = profile.getType();

		String feedUrl = "/posts";
		if (profileType.equals("group"))
			feedUrl = "/feed";

		List<Parameter> list = new LinkedList<Parameter>(Arrays.asList(parameters));
		list.add(Parameter.with("limit", limit));
		list.add(Parameter.with("fields", "status_type,from,id,message,link,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)"));

		Parameter[] param = new Parameter[list.size()];
		Connection<JsonObject> connection = facebookClient.fetchConnection("v2.0/" + profileId + feedUrl, JsonObject.class, list.toArray(param));

		int i = 0;
		Set<String> ids = FBPost.getIds();
		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Post p = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Post.class);
				String postId = p.getId();

				// if is null, this type of post is useless, because it has
				// no comments or messages. It only happens with pages
				// apparently.
				if (profileType.equals("page") && p.getStatusType() == null || ids.contains(postId)) {
					continue;
				} else {
					ids.add(postId);
				}

				FBProfile fbAuthor = null;
				if (profileType.equals("group") || profileType.equals("user")) {
					fbAuthor = new FBUser(p.getFrom().getId(), p.getFrom().getName());
				} else {
					fbAuthor = new FBPage(p.getFrom().getId(), p.getFrom().getName());
				}

				FBPost fbPost = new FBPost(postId);

				fbPost.id = postId;
				fbPost.profile = profile;
				fbPost.profileId = profileId;
				fbPost.author = fbAuthor;
				fbPost.authorId = p.getFrom().getId();
				fbPost.message = p.getMessage();
				fbPost.createdTime = p.getCreatedTime();
				fbPost.commentCount = !jsonObject.has("comments") ? 0 : jsonObject.getJsonObject("comments").getJsonObject("summary").getLong("total_count");
				fbPost.likeCount = !jsonObject.has("likes") ? 0 : jsonObject.getJsonObject("likes").getJsonObject("summary").getLong("total_count");
				fbPost.shareCount = p.getSharesCount();

				posts.add(fbPost);
				i++;
				if (i >= limit)
					break;

			}
			if (i >= limit) {
				return posts;
			}
		}

		return posts;
	}
}
