package db.factory;

import play.Configuration;
import uk.co.panaxiom.playjongo.MongoClientFactory;

public class MongoFactory extends MongoClientFactory {

	public MongoFactory(Configuration config) {
		super(config);
	}

	public String getDBName() {
		return "guaiamum";
	}

}
