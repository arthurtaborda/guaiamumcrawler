package models.facebook;

import models.MessageEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author arthurtaborda
 */
public class FBComment extends MessageEntity {

	public String authorId;

	public String sourceId;

	@JsonIgnore
	public FBPost post;

	@JsonIgnore
	public FBProfile author;

	public Long likeCount;

	public FBComment() {
		super(null);
	}

	public FBComment(String id) {
		super(id);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FBComment)) {
			return false;
		}

		if (((FBComment) o).id.equals(this.id)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "FBComment [id=" + id + ", message=" + message + ", createdTime=" + createdTime + ", post=" + post + ", author=" + author + ", likeCount="
				+ likeCount + "]";
	}
}
