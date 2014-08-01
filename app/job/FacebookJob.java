package job;

import global.Global;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.facebook.FacebookComment;
import models.facebook.FacebookPost;
import models.facebook.FacebookProfile;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import play.Logger;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.F.Promise;
import crawler.FacebookCrawler;

public class FacebookJob implements Job {

	private static final int FETCH_SIZE = 1000;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Logger.info("FACEBOOK JOB STARTED");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		String sourceFile = dataMap.getString("sourceFile");
		List<String> sources = getSources(sourceFile);

		FacebookCrawler crawler = Global.getCrawler();
		for (final String sourceId : sources) {
			Long averageTimePost = 0l;
			List<FacebookPost> posts = null;

			//HACK FOR PLAY BUG
			F.Promise<FacebookProfile> promise = null;
			try {
				promise = JPA.withTransactionAsync(new F.Function0<F.Promise<FacebookProfile>>() {
					@Override
					public F.Promise<FacebookProfile> apply() throws Throwable {
						return Promise.promise(new F.Function0<FacebookProfile>() {
							@Override
							public FacebookProfile apply() throws Throwable {
								return (FacebookProfile) JPA.em().createQuery("from FacebookProfile where profileId = ?").setParameter(1, sourceId)
										.getSingleResult();
							}
						});
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}

			FacebookProfile source = promise.get(1000);
			//HACK FOR PLAY BUG

			if (source == null) { //if this source is new, get first 1000
				posts = crawler.fetchPosts(sourceId, FETCH_SIZE);

				if (posts.size() > 0)
					source = posts.get(0).getSource();

				averageTimePost = getAveragePostTime(posts);
			} else if (!source.isTotallyScanned()) { // if exists but is not complete, get from the older of the list
				posts = crawler.fetchPostsUntil(sourceId, FETCH_SIZE, source.getLastTimeScanned());

				averageTimePost = getAveragePostTime(posts);
			} else if (source.getLastTimeScanned() > source.getAverageTimePost()) { // if is complete, do a rescan for newer posts
				posts = crawler.fetchPostsSince(sourceId, FETCH_SIZE, source.getLastTimeScanned());
				averageTimePost = (getAveragePostTime(posts) + source.getAverageTimePost()) / 2;
			} else {
				continue;
			}

			Logger.debug("SOURCE: " + source.getName());

			int i = 0;
			int j = 0;
			for (FacebookPost fbPost : posts) {
				if (!FacebookPost.exists(fbPost.getPostKey())) {
					List<FacebookComment> comments = crawler.fetchCommentsFromPostKey(fbPost.getPostKey());
					fbPost.setComments(comments);
					fbPost.save();

					i++;
				} else
					j++;
			}
			Logger.debug("number of posts saved: " + i);
			Logger.debug("number of posts already existing: " + j);

			if (posts.size() < FETCH_SIZE) {
				source.setTotallyScanned(true);
			}

			source.setLastTimeScanned((new Date()).getTime());
			source.setAverageTimePost(averageTimePost);

			source.save();
		}

	}

	private Long getAveragePostTime(List<FacebookPost> posts) {
		Long averagePostTime = 0L;

		for (int i = 0; i < posts.size(); i++) {
			if (i < posts.size() - 1) {
				averagePostTime += posts.get(i).getCreatedTime().getTime();
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
