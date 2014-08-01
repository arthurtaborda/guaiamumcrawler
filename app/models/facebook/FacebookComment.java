/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models.facebook;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import models.Page;
import play.db.jpa.JPA;

/**
 *
 * @author arthur-ubuntu
 */
@Entity()
@Table(name = "facebook_comments")
public class FacebookComment {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long dbId;

	@ManyToOne()
	@JoinColumn(name = "post_id")
	private FacebookPost post;

	@Column(name = "comment_key")
	private String commentKey;

	@ManyToOne()
	@JoinColumn(name = "profile_id")
	private FacebookProfile author;

	@Column(name = "message", nullable = false, columnDefinition = "text")
	private String message;

	private Long createdTime;

	@Column(name = "like_count")
	private Long likeCount;

	public FacebookComment() {
	}

	public FacebookComment(String commentId) {
		this.commentKey = commentId;
	}

	public FacebookComment(String commentId, FacebookPost post, FacebookProfile author, String message, long createdTime) {
		this(commentId);

		this.post = post;
		this.author = author;
		this.message = message;
		this.createdTime = createdTime;
	}

	public void save() {
		JPA.em().persist(this);
	}

	public void delete() {
		JPA.em().remove(this);
	}

	@SuppressWarnings("unchecked")
	public static List<FacebookComment> listCommentsByKey(String... idList) {
		return JPA.em().createQuery("from FacebookComment c where c.commentKey IN (:idList)").setParameter("idList", Arrays.asList(idList)).getResultList();
	}

	public static Page<FacebookComment> page(int page, int pageSize, String sortBy, String order, String filter, String postKey) {
		Long total = (Long) JPA.em()
				.createQuery("select count(c) from FacebookComment c, FacebookPost p where c.post = p.id and lower(c.message) like ? and p.postKey = ?")
				.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, postKey).getSingleResult();

		if (page <= 0) { // if page is 0 or less, get last page.
			if (total < pageSize) {
				page = 1;
			} else {
				BigDecimal bd = new BigDecimal(total).divide(new BigDecimal(pageSize)).setScale(0, BigDecimal.ROUND_HALF_UP);
				page = bd.intValue();
			}
		}

		@SuppressWarnings("unchecked")
		List<FacebookComment> data = JPA
				.em()
				.createQuery(
						"select c from FacebookComment c, FacebookPost p where c.post = p.id and lower(c.message) like ? and p.postKey = ? order by c."
								+ sortBy + " " + order).setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, postKey)
				.setFirstResult((page - 1) * pageSize).setMaxResults(pageSize).getResultList();
		return new Page<FacebookComment>(data, total, page, pageSize);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FacebookComment)) {
			return false;
		}

		if (((FacebookComment) o).commentKey.equals(this.commentKey)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + (this.commentKey != null ? this.commentKey.hashCode() : 0);
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FacebookComment [id=" + dbId + ", post=" + post + ", commentKey=" + commentKey + ", author=" + author + ", message=" + message
				+ ", createdTime=" + createdTime + "]";
	}

	public Long getDbId() {
		return dbId;
	}

	public FacebookPost getPost() {
		return post;
	}

	public void setPost(FacebookPost post) {
		this.post = post;
	}

	public String getCommentKey() {
		return commentKey;
	}

	public void setCommentKey(String commentKey) {
		this.commentKey = commentKey;
	}

	public FacebookProfile getAuthor() {
		return author;
	}

	public void setAuthor(FacebookProfile author) {
		this.author = author;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time")
	public Date getCreatedTime() {
		return new Date((long) createdTime * 1000);
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime.getTime() / 1000;
	}
}
