package dao;

import java.net.UnknownHostException;

import models.Page;
import models.facebook.FBPost;

public class FacebookPostDAO extends DAO<FBPost> {

	public FacebookPostDAO(String connectionURL, String dbname, String collectionName, Class<FBPost> type) throws UnknownHostException {
		super(connectionURL, dbname, collectionName, type);
	}

	public FBPost get(String sourceId, String postId) {
		//
		//		Query<FBFeed> q = getDs().createQuery(FBFeed.class).field("id").equal(sourceId);
		//		q.field("posts.id").equal(postId);
		//
		//		FBPost post = null;
		//		FBFeed source = q.get();
		//
		//		if (source.posts != null && source.posts.size() > 0)
		//			post = source.posts.get(0);
		//
		//		return post;
		//	}
		//
		//	public boolean exists(String id) {
		//		return super.exists("id", id);
		//	}
		//
		//	private Query<FBFeed> getQuery(int page, int pageSize, String sortBy, String order, String filter) {
		//		Query<FBFeed> q = getDs().createQuery(FBFeed.class);
		//		Pattern regex = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		//		if (filter.length() > 0) {
		//			q.filter("posts elem", BasicDBObjectBuilder.start("message", regex).get());
		//		}
		//
		//		//q.
		//
		//		//System.out.println(getDs().createQuery(FBFeed.class).filter("posts elem", BasicDBObjectBuilder.start("message", regex).append(, val).get());
		//
		//		Long total = q.countAll();
		//
		//		page = Page.adjustPage(page, total, pageSize);
		//
		//		q.order(order + " posts." + sortBy);
		//		q.offset((page - 1) * pageSize);
		//		q.limit(pageSize);
		//
		//		System.out.println(q.toString());
		//
		//		return q;
		return null;
	}

	public Page<FBPost> page(int page, int pageSize, String sortBy, String order, String filter, String sourceId) {
		//		Query<FBFeed> q = getQuery(page, pageSize, sortBy, order, filter);
		//		q.field("id").equal(sourceId);
		//
		//		FBFeed source = q.get();
		//		List<FBPost> data = source.posts;
		//
		//		for (FBPost fbPost : data) {
		//			fbPost.source = source;
		//		}
		//
		//		Long total = (long) data.size();
		//		page = Page.adjustPage(page, total, pageSize);
		//
		//		return new Page<FBPost>(data, total, page, pageSize);
		return null;
	}

	public Page<FBPost> page(int page, int pageSize, String sortBy, String order, String filter) {
		//		Query<FBFeed> q = getQuery(page, pageSize, sortBy, order, filter);
		//
		//		List<FBFeed> source = q.asList();
		//		List<FBPost> data = new ArrayList<>();
		//
		//		Map<String, FBFeed> map = new HashMap<>();
		//
		//		for (FBFeed fbFeed : source) {
		//			map.put(fbFeed.id, fbFeed);
		//			data.addAll(fbFeed.posts);
		//			for (FBPost fbPost : data) {
		//				fbPost.source = fbFeed;
		//			}
		//		}

		//		for (FBPost fbPost : data) {
		//			fbPost.source = 
		//		}
		//		
		//
		//		data.addAll(fbFeed.posts);
		//		for (; i < data.size(); i++) {
		//			data.get(i).source = fbFeed;
		//		}
		//
		//		Long total = (long) data.size();
		//		page = Page.adjustPage(page, total, pageSize);
		//
		//		return new Page<FBPost>(data, total, page, pageSize);
		return null;
	}

	@Override
	protected void ensureIndex() {
		// TODO Auto-generated method stub

	}
}
