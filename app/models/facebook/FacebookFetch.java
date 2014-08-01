package models.facebook;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class FacebookFetch {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long dbId;

	@ManyToOne()
	@JoinColumn(name = "source_id")
	private FacebookProfile source;

	@Column
	private String link;

	@Column
	private String next;

	@Column
	private String previous;

	public FacebookFetch(FacebookProfile source, String link, String next, String previous) {
		this.source = source;
		this.link = link;
		this.next = next;
		this.previous = previous;
	}

	public FacebookProfile getSource() {
		return source;
	}

	public void setSource(FacebookProfile source) {
		this.source = source;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public Long getDbId() {
		return dbId;
	}

}
