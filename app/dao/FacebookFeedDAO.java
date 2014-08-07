package dao;

import java.net.UnknownHostException;
import java.util.List;

import models.facebook.FBFeed;
import models.facebook.FBProfile;

public class FacebookFeedDAO extends DAO<FBFeed> {

	public FacebookFeedDAO(String connectionURL, String dbname, String collectionName, Class<FBFeed> type) throws UnknownHostException {
		super(connectionURL, dbname, collectionName, type);
	}

	public boolean exists(String id) {
		return super.exists("id", id);
	}

	public FBProfile getUser(String id) {
		//		Query<FBProfile> q = getDs().createQuery(FBProfile.class).field("id").equal(id);
		//
		//		return q.get();
		//	}
		//
		//	public void update(String profileId, Map<String, Object> fields) {
		//		Query<FBUser> q = getDs().createQuery(FBUser.class).field("id").equal(profileId);
		//
		//		UpdateOperations<FBUser> updates = getDs().createUpdateOperations(FBUser.class);
		//
		//		for (Map.Entry<String, Object> entry : fields.entrySet()) {
		//			updates.set(entry.getKey(), entry.getValue());
		//		}
		//
		//		getDs().update(q, updates);
		//	}
		//
		//	public List<FBFeed> listSources() {
		//		Query<FBFeed> q = getDs().createQuery(FBFeed.class);
		//
		//		return q.asList();
		return null;
	}

	@Override
	protected void ensureIndex() {
		// TODO Auto-generated method stub

	}

	public List<FBFeed> listSources() {
		// TODO Auto-generated method stub
		return null;
	}
}
