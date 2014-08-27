package job;

import global.Global;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.facebook.FBComment;
import models.facebook.FBFeed;
import models.facebook.FBPost;
import models.facebook.profile.FBProfile;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import play.Logger;
import crawler.FacebookCrawler;

public class FacebookJob implements Job {

	private static final int FETCH_SIZE = 1000;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Logger.info("FACEBOOK JOB STARTED");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();

		String sourceFile = dataMap.getString("sourceFile");
		List<String> sources = getSources(sourceFile);

		FacebookCrawler crawler = Global.getFacebookCrawler();
		for (final String sourceId : sources) {
			Logger.debug("Job analysing source " + sourceId);
			Long averageTimePost = 0l;
			List<FBPost> posts = null;

			FBProfile source = FBProfile.get(sourceId);

			Long now = (new Date()).getTime();
			try {
				if (source != null) {
					String log = "Fetching posts of " + source.getType() + " " + source.name;

					if (source.feed == null)
						source.feed = new FBFeed();

					if (!source.feed.totallyScanned) { // if is not complete, get from the older of the list
						Logger.debug(log + " because is not totally scanned");
						posts = crawler.fetchPostsUntil(source, FETCH_SIZE, source.feed.lastTimeScanned / 1000);

						averageTimePost = getAveragePostTime(posts);
					} else if (source.feed.averageTimePost > 0 && now - source.feed.lastTimeScanned > source.feed.averageTimePost) { // if is complete, do a rescan for newer posts
						Logger.debug(log + " because was last scanned in " + (now - source.feed.lastTimeScanned) + " and average time post is "
								+ source.feed.averageTimePost);

						posts = crawler.fetchPostsSince(source, FETCH_SIZE, source.feed.lastTimeScanned / 1000);
						averageTimePost = (getAveragePostTime(posts) + source.feed.averageTimePost) / 2;
					} else {
						Logger.debug("Skipping " + source.getType() + " " + source.name);
						continue;
					}
				} else { //if this source is new, get first 1000
					source = crawler.fetchProfile(sourceId);

					Logger.debug("Fetching posts of " + source.getType() + " " + source.name);

					posts = crawler.fetchPosts(source, FETCH_SIZE);

					if (posts.size() > 0)
						source.feed.posts = posts;

					averageTimePost = getAveragePostTime(posts);
				}
			} catch (Exception e) {
				e.printStackTrace();

				try {
					Thread.sleep(60000); //sleep for 1 min
				} catch (InterruptedException e1) {
				}

				JobExecutionException e2 = new JobExecutionException(e);
				e2.setRefireImmediately(true);
				throw e2;
			}

			if (posts.size() > 0 && posts.size() < FETCH_SIZE) {
				source.feed.totallyScanned = true;
			}

			source.feed.lastTimeScanned = now;
			source.feed.averageTimePost = averageTimePost;

			source.save();

			int i = 0;
			int j = 0;
			int k = 0;
			Logger.debug("Fetching comments of " + source.getType() + " " + source.name);
			for (FBPost fbPost : posts) {
				if (!FBProfile.exists(fbPost.authorId)) {
					fbPost.author.save();
					j++;
				}
				List<FBComment> comments = crawler.fetchCommentsFromPostId(fbPost.id.toString());

				for (FBComment fbComment : comments) {
					fbComment.save();
					k++;
				}

				fbPost.save();
				i++;
			}
			Logger.debug("posts saved: " + i);
			Logger.debug("authors saved: " + j);
			Logger.debug("comments saved: " + k);
		}
	}

	private Long getAveragePostTime(List<FBPost> posts) {
		if (posts.size() == 0)
			return 0l;

		Long averagePostTime = 0l;

		long a = new Date().getTime();
		for (int i = 0; i < posts.size() - 1; i++) {
			long b = posts.get(i + 1).createdTime.getTime();
			averagePostTime += a - b;
			a = b;
		}

		averagePostTime /= posts.size();
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
