package global;

import job.FacebookJob;

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

import com.restfb.DefaultFacebookClient;

import crawler.FacebookCrawler;

public class Global extends GlobalSettings {
	private static FacebookCrawler crawler;

	public static FacebookCrawler getCrawler() {
		return crawler;
	}

	private void schedule() {
		try {
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
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void onStart(Application app) {
		String appId = app.configuration().getString("fb.id");
		String appSecret = app.configuration().getString("fb.secret");
		crawler = new FacebookCrawler(new DefaultFacebookClient(new DefaultFacebookClient().obtainAppAccessToken(appId, appSecret).getAccessToken()));

		//start the jobs
		//schedule();

		Logger.info("Application has started");
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

}