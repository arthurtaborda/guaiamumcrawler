package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.FacebookComment;
import models.FacebookPost;
import models.FacebookProfile;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import play.Play;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.fbComments;
import views.html.fbPosts;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.json.JsonObject;
import com.restfb.types.Comment;
import com.restfb.types.Post;

public class FacebookController extends Controller {

	private static final String APP_ID = Play.application().configuration().getString("fb.id");
	private static final String APP_SECRET = Play.application().configuration().getString("fb.secret");

	private static FacebookClient facebookClient = new DefaultFacebookClient(new DefaultFacebookClient().obtainAppAccessToken(APP_ID, APP_SECRET)
			.getAccessToken());

	public static Result index(String sourceId, String sourceName) {
		return redirect(routes.FacebookController.fbPosts(1, "createdTime", "desc", "", sourceId, sourceName));
	}

	public static Result comments(String postKey) {
		return redirect(routes.FacebookController.fbComments(1, "createdTime", "asc", "", postKey));
	}

	@Transactional(readOnly = true)
	public static Result fbPosts(int page, String sortBy, String order, String filter, String sourceId, String sourceName) {
		if (sourceId == null || sourceId.isEmpty()) {
			return ok(fbPosts.render(FacebookPost.page(page, 10, sortBy, order, filter), sortBy, order, filter, sourceId, sourceName,
					FacebookProfile.listSources()));

		} else {
			return ok(fbPosts.render(FacebookPost.page(page, 10, sortBy, order, filter, sourceId), sortBy, order, filter, sourceId, sourceName,
					FacebookProfile.listSources()));

		}
	}

	@Transactional(readOnly = true)
	public static Result fbComments(int page, String sortBy, String order, String filter, String postKey) {
		return ok(fbComments.render(FacebookComment.page(page, 10, sortBy, order, filter, postKey), sortBy, order, filter, postKey));
	}

	@Transactional
	public static Result fetchCommentsFromPostKey(String postKey) {
		Connection<JsonObject> connection = facebookClient.fetchConnection(postKey + "/comments", JsonObject.class, Parameter.with("limit", 5000));

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

				fbComment.setMessage(c.getMessage());
				fbComment.setAuthor(fbAuthor);
				fbComment.setCreatedTime(c.getCreatedTime());

				comments.add(fbComment);
			}
		}

		FacebookPost post = FacebookPost.findByPostKey(postKey);

		if (post != null) {
			post.setComments(comments);
			post.save();

			return comments(postKey);
		} else {
			return index("", "");
		}
	}

	@Transactional
	public static Result fetchPosts(String sourceId, Integer limit) {
		JsonObject feed = facebookClient.fetchObject("v2.0/" + sourceId, JsonObject.class, Parameter.with("fields", "name"), Parameter.with("metadata", true));

		String sourceName = feed.getString("name");
		String type = feed.getJsonObject("metadata").getString("type");

		FacebookProfile fbSource = FacebookProfile.findByProfileId(sourceId);

		if (fbSource == null) {
			fbSource = new FacebookProfile(sourceId, sourceName, type);
			fbSource.setIsSource(true);
			fbSource.save();
		} else {
			fbSource.setIsSource(true);
			FacebookProfile.update(fbSource);
		}

		if (limit > 250)
			limit = 250;

		String feedUrl = "/posts";
		if (type.equals("group"))
			feedUrl = "/feed";

		Connection<JsonObject> connection = facebookClient.fetchConnection("v2.0/" + sourceId + feedUrl, JsonObject.class, Parameter.with("limit", limit),
				Parameter.with("fields", "from,id,message,link,created_time,shares,likes.limit(1).summary(true),comments.limit(1).summary(true)"));

		int i = 0;
		for (List<JsonObject> jsonObjects : connection) {
			for (JsonObject jsonObject : jsonObjects) {
				Post p = facebookClient.getJsonMapper().toJavaObject(jsonObject.toString(), Post.class);

				FacebookPost dbPost = FacebookPost.findByPostKey(p.getId());
				if (dbPost == null) {
					// if this is coming from a group, the author must be an
					// user.
					if (type.equals("group"))
						type = "user";

					FacebookPost fbPost = new FacebookPost(p.getId());
					FacebookProfile fbAuthor = FacebookProfile.findByProfileId(p.getFrom().getId());

					if (fbAuthor == null) {
						fbAuthor = new FacebookProfile(p.getFrom().getId(), p.getFrom().getName(), type);
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

					fbPost.save();
					i++;
				}
			}
			if (i >= limit)
				break;
		}

		return index(sourceId, sourceName);
	}

	@Transactional
	public static Result generateXlsFile(String ids) {
		String[] idList = ids.split(",");
		List<FacebookComment> comments = FacebookComment.listCommentsByKey(idList);

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Comments");

		Map<String, Object[]> data = new HashMap<String, Object[]>();
		data.put("1", new Object[] { "Date", "Message", "Comment ID", "User ID" });

		int i = 1;
		for (FacebookComment c : comments)
			data.put(String.valueOf(i++), new Object[] { c.getCreatedTime(), c.getMessage(), c.getCommentKey(), c.getAuthor().getName() });

		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date)
					cell.setCellValue((Date) obj);
				else if (obj instanceof Boolean)
					cell.setCellValue((Boolean) obj);
				else if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Double)
					cell.setCellValue((Double) obj);
			}
		}

		File file = new File("comments.xls");
		try {
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.close();
			System.out.println("Excel written successfully..");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ok(file);
	}
}
