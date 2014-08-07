package controllers;

import java.util.ArrayList;

import models.TwitterTweet;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.ttTweets;

public class TwitterController extends Controller {

	//	private static final String APP_KEY = Play.application().configuration().getString("tt.key");
	//	private static final String APP_SECRET = Play.application().configuration().getString("tt.secret");

	public static Result index(String username) {
		return redirect(routes.TwitterController.ttTweets(1, "uploadDate", "desc", "", username));
	}

	public static Result ttTweets(int page, String sortBy, String order, String filter, String username) {
		return ok(ttTweets.render(TwitterTweet.page(page, 10, sortBy, order, filter, username), sortBy, order, filter, username, new ArrayList<String>()));
	}
}
