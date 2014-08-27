package global;

import job.FacebookJob;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import crawler.FacebookCrawler;
import crawler.TwitterCrawler;

public class Global extends GlobalSettings {

	private static TwitterCrawler ttCrawler;
	private static FacebookCrawler fbCrawler;

	public static TwitterCrawler getTwitterCrawler() {
		return ttCrawler;
	}

	public static FacebookCrawler getFacebookCrawler() {
		return fbCrawler;
	}

	private void schedule() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

		boolean exists = sched.checkExists(new JobKey("fbJob", "group1"));

		if (!exists) {
			//@formatter:off
			JobDetail job = JobBuilder.newJob(FacebookJob.class)
					.withIdentity("fbJob", "group1")
				    .usingJobData("sourceFile", "facebookSources")
					.build();

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("fbTrigger", "group1").startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInHours(5)
							.repeatForever())
						.build();
			//@formatter:on

			sched.scheduleJob(job, trigger);
		}
	}

	public void onStart(Application app) {
		if (fbCrawler == null) {
			String fbAppId = app.configuration().getString("fb.id");
			String fbAppSecret = app.configuration().getString("fb.secret");

			FacebookClient fbClient = new DefaultFacebookClient(new DefaultFacebookClient().obtainAppAccessToken(fbAppId, fbAppSecret).getAccessToken());
			fbCrawler = new FacebookCrawler(fbClient);
		}

		if (ttCrawler == null) {
			String ttAppId = app.configuration().getString("tt.key");
			String ttAppSecret = app.configuration().getString("tt.secret");
			String ttToken = app.configuration().getString("tt.token.key");
			String ttTokenSecret = app.configuration().getString("tt.token.secret");

			Twitter twitter = TwitterFactory.getSingleton();

			try {
				twitter.setOAuthConsumer(ttAppId, ttAppSecret);
				twitter.setOAuthAccessToken(new AccessToken(ttToken, ttTokenSecret));
			} catch (Exception e) {
			}

			ttCrawler = new TwitterCrawler(twitter);
		}

		try {
			Logger.info("Loading jobs...");
			if (false)
				schedule();
			Logger.info("Jobs loaded sucessfully");
		} catch (SchedulerException e1) {
			e1.printStackTrace();
		}

		Logger.info("Application has started");
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

}