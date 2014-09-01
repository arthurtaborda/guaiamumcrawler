package global;

import job.FacebookJob;
import job.TwitterJob;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import crawler.FacebookCrawler;
import crawler.TwitterCrawler;

public class Global extends GlobalSettings {

	private static TwitterCrawler ttCrawler;
	private static FacebookCrawler fbCrawler;

	private String twitterSource;
	private String facebookSource;
	private String language;
	private String languagePercentage;

	private Scheduler sched;

	public static TwitterCrawler getTwitterCrawler() {
		return ttCrawler;
	}

	public static FacebookCrawler getFacebookCrawler() {
		return fbCrawler;
	}

	private void scheduleTwitter() throws SchedulerException {
		boolean ttJobExists = sched.checkExists(new JobKey("ttJob", "twitter"));

		if (!ttJobExists) {
			//@formatter:off
			JobDetail ttJob = JobBuilder.newJob(TwitterJob.class)
					.withIdentity("ttJob", "twitter")
				    .usingJobData("sourceFile", twitterSource)
				    .usingJobData("language", language)
				    .usingJobData("languagePercentage", languagePercentage)
					.build();

			Trigger ttTrigger = TriggerBuilder.newTrigger()
					.withIdentity("ttTrigger", "twitter").startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInHours(5)
							.repeatForever())
						.build();
			//@formatter:on

			sched.scheduleJob(ttJob, ttTrigger);
		}
	}

	private void scheduleFacebook() throws SchedulerException {
		boolean fbJobExists = sched.checkExists(new JobKey("fbJob", "facebook"));

		if (!fbJobExists) {
			//@formatter:off
			JobDetail fbJob = JobBuilder.newJob(FacebookJob.class)
					.withIdentity("fbJob", "facebook")
				    .usingJobData("sourceFile", facebookSource)
					.build();

			Trigger fbTrigger = TriggerBuilder.newTrigger()
					.withIdentity("fbTrigger", "facebook").startNow()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInHours(5)
							.repeatForever())
						.build();
			//@formatter:on

			sched.scheduleJob(fbJob, fbTrigger);
		}
	}

	public void onStart(Application app) {
		try {
			DetectorFactory.loadProfile(play.Play.application().getFile("profiles"));
		} catch (LangDetectException e2) {
			e2.printStackTrace();
		}

		twitterSource = app.configuration().getString("source.file.twitter");
		facebookSource = app.configuration().getString("source.file.facebook");

		language = app.configuration().getString("language");
		languagePercentage = app.configuration().getString("languagePercentage");

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
			sched = new org.quartz.impl.StdSchedulerFactory().getScheduler();
			sched.start();
			scheduleTwitter();
			scheduleFacebook();
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