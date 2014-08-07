package models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.CLASS, property = "_class")
public class MessageEntity {

	@JsonProperty("_id")
	public String id;

	public String message;

	public Date createdTime;

	public MessageEntity(String id) {
		this.id = id;
	}

}
