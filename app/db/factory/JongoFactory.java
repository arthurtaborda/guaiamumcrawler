package db.factory;

import org.jongo.Mapper;
import org.jongo.marshall.jackson.JacksonMapper;

import uk.co.panaxiom.playjongo.JongoMapperFactory;

import com.fasterxml.jackson.databind.MapperFeature;

public class JongoFactory implements JongoMapperFactory {

	public Mapper create() {
		return new JacksonMapper.Builder().enable(MapperFeature.AUTO_DETECT_GETTERS).build();
	}

}
