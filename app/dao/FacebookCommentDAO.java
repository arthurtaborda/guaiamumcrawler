package dao;

import java.net.UnknownHostException;
import java.util.List;

import models.Page;
import models.facebook.FBComment;

public class FacebookCommentDAO extends DAO<FBComment> {

	public FacebookCommentDAO(String connectionURL, String dbname, String collectionName, Class<FBComment> type) throws UnknownHostException {
		super(connectionURL, dbname, collectionName, type);
	}

	public boolean exists(String id) {
		return false;//super.collection.
	}

	public List<FBComment> listCommentsById(List<String> idList) {
		//		Query<FBComment> q = getDs().createQuery(FBComment.class);
		//		q.field("id").in(idList);
		//
		//		return q.asList();
		return null;
	}

	public Page<FBComment> page(int page, int pageSize, String sortBy, String order, String filter, String sourceId, String postId) {
		//		Query<FBFeed> q = getDs().createQuery(FBFeed.class).field("id").equal(sourceId);
		//		q.filter("posts.id =", postId);
		//		if (filter.length() > 0)
		//			q.filter("posts.comments.message =", Pattern.compile(filter, Pattern.CASE_INSENSITIVE));
		//
		//		Logger.debug(q.toString());
		//		Long total = (long) q.get().posts.get(0).comments.size();//getDs().createAggregation(FBFeed.class).;
		//
		//		page = Page.adjustPage(page, total, pageSize);
		//
		//		q.order(order + " posts.comments." + sortBy);
		//		q.offset((page - 1) * pageSize);
		//		q.limit(pageSize);
		//
		//		FBFeed source = q.get();
		//		FBPost post = source.posts.get(0);
		//		List<FBComment> data = post.comments;
		//
		//		if (data == null) {
		//			data = new ArrayList<>();
		//		} else {
		//			for (FBComment fbComment : data) {
		//				fbComment.post = post;
		//			}
		//		}
		//
		//		return new Page<FBComment>(data, total, page, pageSize);
		return null;
	}

	@Override
	protected void ensureIndex() {
		// TODO Auto-generated method stub

	}
}
