package controllers;

import global.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.facebook.FBComment;
import models.facebook.FBPost;
import models.facebook.profile.FBProfile;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.fbComments;
import views.html.fbPosts;
import crawler.FacebookCrawler;

public class FacebookController extends Controller {

	private static final FacebookCrawler crawler = Global.getCrawler();

	public static Result index(String sourceId, String sourceName) {
		return redirect(routes.FacebookController.fbPosts(1, "createdTime", "-", "", sourceId, sourceName));
	}

	public static Result comments(String postKey) {
		return redirect(routes.FacebookController.fbComments(1, "createdTime", "", "", postKey));
	}

	public static Result fbPosts(int page, String sortBy, String order, String filter, String sourceId, String sourceName) {
		if (sourceId == null || sourceId.isEmpty()) {
			return ok(fbPosts.render(FBPost.list(page, 10, sortBy, order, filter), sortBy, order, filter, sourceId, sourceName, FBProfile.listSources()));

		} else {
			return ok(fbPosts.render(FBPost.list(page, 10, sortBy, order, filter, sourceId), sortBy, order, filter, sourceId, sourceName,
					FBProfile.listSources()));

		}
	}

	public static Result fbComments(int page, String sortBy, String order, String filter, String postId) {
		return ok(fbComments.render(FBComment.list(page, 10, sortBy, order, filter, postId), sortBy, order, filter, postId));
	}

	public static Result fetchCommentsFromPostKey(String postId) {
		List<FBComment> comments = crawler.fetchCommentsFromPostId(postId);

		for (FBComment fbComment : comments) {
			fbComment.save();
		}

		return comments(postId);
	}

	public static Result fetchPosts(String profileId, Integer limit) {
		FBProfile fbProfile = null;

		fbProfile = crawler.fetchProfile(profileId);

		List<FBPost> posts = crawler.fetchPosts(fbProfile, limit);

		fbProfile.feed.posts = posts;
		fbProfile.feed.lastTimeScanned = (new Date()).getTime();

		fbProfile.save();
		for (FBPost fbPost : posts) {
			if (FBProfile.get(fbPost.authorId) == null) {
				fbPost.author.save();
			}

			fbPost.save();
		}

		return index(fbProfile.id, fbProfile.name);
	}

	public static Result generateXlsFile(String ids) {
		String[] tmpIdList = ids.split(",");

		List<FBComment> comments = FBComment.listCommentsById(Arrays.asList(tmpIdList));

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Comments");

		Map<String, Object[]> data = new HashMap<String, Object[]>();
		data.put("1", new Object[] { "Date", "Message", "Comment ID", "User ID" });

		int i = 1;
		for (FBComment c : comments)
			data.put(String.valueOf(i++), new Object[] { c.createdTime, c.message, c.id, c.author.name });

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
