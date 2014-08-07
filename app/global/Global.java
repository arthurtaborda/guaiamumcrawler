package global;

import java.net.UnknownHostException;

import job.FacebookJob;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import play.Application;
import play.GlobalSettings;
import play.Logger;

import com.mongodb.MongoClient;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import crawler.FacebookCrawler;
import dao.FacebookCommentDAO;
import dao.FacebookFeedDAO;
import dao.FacebookPostDAO;

public class Global extends GlobalSettings {

	private static FacebookPostDAO fbPostDao;
	private static FacebookCommentDAO fbCommentDao;
	private static FacebookFeedDAO fbProfileDao;
	private static FacebookCrawler crawler;

	public static FacebookCrawler getCrawler() {
		return crawler;
	}

	public static FacebookPostDAO getFbPostDao() {
		return fbPostDao;
	}

	public static FacebookCommentDAO getFbCommentDao() {
		return fbCommentDao;
	}

	public static FacebookFeedDAO getFbFeedDao() {
		return fbProfileDao;
	}

	private void schedule() throws SchedulerException {
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

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

	private void loadDAO() throws UnknownHostException {
		Morphia m = new Morphia();
		MongoClient mongo = new MongoClient("localhost", 27017);

		Datastore datastore = m.createDatastore(mongo, "guaiamum");

		//		fbPostDao = new FacebookPostDAO(datastore);
		//		fbCommentDao = new FacebookCommentDAO(datastore);
		//		fbProfileDao = new FacebookFeedDAO(datastore);
	}

	public void onStart(Application app) {
		try {
			Logger.info("Loading mongodb...");
			loadDAO();
			Logger.info("Database loaded sucessfully");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String appId = app.configuration().getString("fb.id");
		String appSecret = app.configuration().getString("fb.secret");

		FacebookClient fbClient = new DefaultFacebookClient(new DefaultFacebookClient().obtainAppAccessToken(appId, appSecret).getAccessToken());
		crawler = new FacebookCrawler(fbClient);

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