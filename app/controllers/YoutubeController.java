package controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import models.YoutubeVideo;

import org.joda.time.DateTime;

import play.mvc.Controller;
import play.mvc.Result;
import util.ExcelUtil;
import views.html.ytVideos;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.util.ServiceException;

public class YoutubeController extends Controller {

	public static final String YOUTUBE_VIDEO_LINK = "http://www.youtube.com/watch?v=";
	public static final String YOUTUBE_GDATA_SERVER = "http://gdata.youtube.com";
	public static final String USER_FEED_PREFIX = YOUTUBE_GDATA_SERVER + "/feeds/api/users/";
	public static final String UPLOADS_FEED_SUFFIX = "/uploads";

	public static Result index(String username) {
		return redirect(routes.YoutubeController.ytVideos(1, "uploadDate", "desc", "", username));
	}

	public static Result ytVideos(int page, String sortBy, String order, String filter, String username) {
		return ok(ytVideos.render(YoutubeVideo.page(page, 10, sortBy, order, filter, username), sortBy, order, filter, username, YoutubeVideo.listUsernames()));
	}

	public static Result fetchVideos(String username) throws MalformedURLException, IOException, ServiceException {

		List<VideoEntry> videoEntries;
		YouTubeService service = new YouTubeService("Youtube Fetcher");

		int i = 1;
		int maxResults = 50;
		do {
			String url = USER_FEED_PREFIX + username + UPLOADS_FEED_SUFFIX + "?max-results=" + maxResults + "&start-index=" + i;

			videoEntries = service.getFeed(new URL(url), VideoFeed.class).getEntries();
			for (VideoEntry ve : videoEntries) {
				getYoutubeVideo(ve, username).save();
				i++;
			}
		} while (videoEntries.size() >= maxResults);

		return index(username);
	}

	public static Result downloadXlsFile(String username) {
		List<YoutubeVideo> videos = YoutubeVideo.findByUsername(username);

		String[] columnHeader = new String[] { "Título", "Visualizações", "Qtde Comentários", "Qtde Gostei", "Qtde Não Gostei", "Data de Envio", "Link" };

		Object[][] dataList = new Object[videos.size()][columnHeader.length];

		int i = 0;
		for (YoutubeVideo yt : videos) {
			dataList[i++] = new Object[] { yt.title, yt.viewCount, yt.commentCount, yt.likeCount, yt.dislikeCount,
					new DateTime(yt.uploadDate).toString("HH:mm:ss dd/MM/yyyy"), yt.link };
		}

		File file = ExcelUtil.generateXlsFile("Videos", columnHeader, dataList);

		response().setHeader("Content-Disposition", "attachment; filename=youtube-videos.xls");

		return ok(file);
	}

	private static YoutubeVideo getYoutubeVideo(VideoEntry ve, String username) {
		String title = ve.getTitle().getPlainText();

		Long viewCount = ve.getStatistics() != null ? ve.getStatistics().getViewCount() : 0;

		Integer commentCount = ve.getComments() != null && ve.getComments().getFeedLink() != null ? ve.getComments().getFeedLink().getCountHint() : 0;

		Integer likeCount = ve.getYtRating() != null ? ve.getYtRating().getNumLikes() : 0;
		Integer dislikeCount = ve.getYtRating() != null ? ve.getYtRating().getNumDislikes() : 0;

		Long uploadDate = ve.getMediaGroup() != null && ve.getMediaGroup().getUploaded() != null ? ve.getMediaGroup().getUploaded().getValue() : 0;

		String link = ve.getMediaGroup() != null && ve.getMediaGroup().getPlayer() != null ? ve.getMediaGroup().getPlayer().getUrl() : "";

		return new YoutubeVideo(title, username, viewCount, commentCount, likeCount, dislikeCount, uploadDate, link);
	}

}
