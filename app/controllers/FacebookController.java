package controllers;

import global.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.facebook.FacebookComment;
import models.facebook.FacebookPost;
import models.facebook.FacebookProfile;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.fbComments;
import views.html.fbPosts;
import crawler.FacebookCrawler;

public class FacebookController extends Controller {

	private static final FacebookCrawler crawler = Global.getCrawler();

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
		List<FacebookComment> comments = crawler.fetchCommentsFromPostKey(postKey);
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
		String sourceName = crawler.fetchSource(sourceId).getName();
		List<FacebookPost> posts = crawler.fetchPosts(sourceId, limit);

		for (FacebookPost facebookPost : posts) {
			facebookPost.save();

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
