package job;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.facebook.FBPost;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dao.FacebookCommentDAO;
import dao.FacebookFeedDAO;
import dao.FacebookPostDAO;

public class FacebookJob implements Job {

	private FacebookPostDAO fbPostDao;
	//private FacebookCommentDAO fbCommentDao;
	private FacebookFeedDAO fbProfileDao;

	public FacebookJob(FacebookPostDAO fbPostDao, FacebookCommentDAO fbCommentDao, FacebookFeedDAO fbProfileDao) {
		this.fbPostDao = fbPostDao;
		//this.fbCommentDao = fbCommentDao;
		this.fbProfileDao = fbProfileDao;
	}

	private static final int FETCH_SIZE = 1000;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//		Logger.info("FACEBOOK JOB STARTED");
		//
		//		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		//
		//		String sourceFile = dataMap.getString("sourceFile");
		//		List<String> sources = getSources(sourceFile);
		//
		//		FacebookCrawler crawler = Global.getCrawler();
		//		for (final String sourceId : sources) {
		//			Long averageTimePost = 0l;
		//			List<FBPost> posts = null;
		//
		//			FBFeed source = fbProfileDao.find(sourceId);
		//
		//			if (source == null) { //if this source is new, get first 1000
		//				source = crawler.fetchFeed(sourceId);
		//				posts = crawler.fetchPosts(source.id, source.getType(), FETCH_SIZE);
		//
		//				if (posts.size() > 0)
		//					source = posts.get(0).profile;
		//
		//				averageTimePost = getAveragePostTime(posts);
		//			} else if (!source.totallyScanned) { // if exists but is not complete, get from the older of the list
		//				posts = crawler.fetchPostsUntil(source.id, source.getType(), FETCH_SIZE, source.lastTimeScanned);
		//
		//				averageTimePost = getAveragePostTime(posts);
		//			} else if (source.lastTimeScanned > source.averageTimePost) { // if is complete, do a rescan for newer posts
		//				posts = crawler.fetchPostsSince(source.id, source.getType(), FETCH_SIZE, source.lastTimeScanned);
		//				averageTimePost = (getAveragePostTime(posts) + source.averageTimePost) / 2;
		//			} else {
		//				continue;
		//			}
		//
		//			//Logger.debug("SOURCE: " + source.name);
		//
		//			int i = 0;
		//			int j = 0;
		//			for (FBPost fbPost : posts) {
		//				if (!fbPostDao.exists(fbPost.id)) {
		//					List<FBComment> comments = crawler.fetchCommentsFromPostId(fbPost.id.toString());
		//					fbPost.comments = comments;
		//					fbPostDao.save(fbPost);
		//
		//					i++;
		//				} else
		//					j++;
		//			}
		//			Logger.debug("number of posts saved: " + i);
		//			Logger.debug("number of posts already existing: " + j);
		//
		//			if (posts.size() < FETCH_SIZE) {
		//				source.totallyScanned = true;
		//			}
		//
		//			source.lastTimeScanned = (new Date()).getTime();
		//			source.averageTimePost = averageTimePost;
		//
		//			fbProfileDao.save(source);
		//		}

	}

	private Long getAveragePostTime(List<FBPost> posts) {
		Long averagePostTime = 0L;

		for (int i = 0; i < posts.size(); i++) {
			if (i < posts.size() - 1) {
				averagePostTime += posts.get(i).createdTime.getTime();
			}
		}

		averagePostTime /= posts.size() - 1;
		return averagePostTime;
	}

	private List<String> getSources(String sourceFile) {
		List<String> sources = new ArrayList<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(play.Play.application().getFile(sourceFile)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sources.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sources;
	}
}
