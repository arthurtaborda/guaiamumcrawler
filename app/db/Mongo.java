package db;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import uk.co.panaxiom.playjongo.PlayJongo;

public class Mongo {

	public static MongoCollection get(String name) {
		return PlayJongo.getCollection(name);
	}

	public static Jongo db() {
		return PlayJongo.jongo();
	}
}
