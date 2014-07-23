/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.JPA;

/**
 * @author arthur
 *
 */
@Entity
@Table(name = "facebook_posts")
public class FacebookPost {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "post_id")
	private List<FacebookComment> comments;

	@Column(name = "post_key", unique = true, nullable = false, updatable = false)
	private String postKey;

	@Column(columnDefinition = "text")
	private String message;

	private Long createdTime;

	@ManyToOne()
	@JoinColumn(name = "profile_id")
	private FacebookProfile author;

	@ManyToOne()
	@JoinColumn(name = "source_id")
	private FacebookProfile source;

	@Column(name = "comment_count")
	private Long commentCount;

	@Column(name = "like_count")
	private Long likeCount;

	@Column(name = "share_count")
	private Long shareCount;

	// necessary
	public FacebookPost() {
	}

	public FacebookPost(String postKey) {
		this.postKey = postKey;
	}

	public FacebookPost(String postKey, String message, Long createdTime) {
		this.postKey = postKey;
		this.message = message;
		this.createdTime = createdTime;
	}

	public FacebookPost(String postKey, String message, Long createdTime, List<FacebookComment> comments) {
		this.postKey = postKey;
		this.message = message;
		this.createdTime = createdTime;
		this.comments = comments;
	}

	public static FacebookPost findByPostKey(String key) {
		FacebookPost post;

		try {
			post = (FacebookPost) JPA.em().createQuery("from FacebookPost c where c.postKey = ?").setParameter(1, key).getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}

		return post;
	}

	@SuppressWarnings("unchecked")
	public static Page<FacebookPost> page(int page, int pageSize, String sortBy, String order, String filter, String sourceId) {
		Long total = (Long) JPA.em().createQuery("select count(c) from FacebookPost c where lower(c.message) like ? AND source.profileId = ?")
				.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, sourceId).getSingleResult();

		page = adjustPage(page, total, pageSize);

		List<FacebookPost> data = JPA.em()
				.createQuery("from FacebookPost c where lower(c.message) like ? AND source.profileId = ? order by c." + sortBy + " " + order)
				.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, sourceId).setFirstResult((page - 1) * pageSize).setMaxResults(pageSize)
				.getResultList();
		return new Page<FacebookPost>(data, total, page, pageSize);
	}

	@SuppressWarnings("unchecked")
	public static Page<FacebookPost> page(int page, int pageSize, String sortBy, String order, String filter) {
		Long total = (Long) JPA.em().createQuery("select count(c) from FacebookPost c where lower(c.message) like ?")
				.setParameter(1, "%" + filter.toLowerCase() + "%").getSingleResult();

		page = adjustPage(page, total, pageSize);

		List<FacebookPost> data = JPA.em().createQuery("from FacebookPost c where lower(c.message) like ? order by c." + sortBy + " " + order)
				.setParameter(1, "%" + filter.toLowerCase() + "%").setFirstResult((page - 1) * pageSize).setMaxResults(pageSize).getResultList();
		return new Page<FacebookPost>(data, total, page, pageSize);
	}

	private static int adjustPage(int page, Long total, int pageSize) {
		if (page <= 0) { // if page is 0 or less, get last page.
			if (total < pageSize) {
				page = 1;
			} else {
				BigDecimal bd = new BigDecimal(total).divide(new BigDecimal(pageSize)).setScale(0, BigDecimal.ROUND_HALF_UP);
				page = bd.intValue();
			}
		}

		return page;
	}

	public void save() {
		JPA.em().persist(this);
	}

	public void delete() {
		JPA.em().remove(this);
	}

	public static void update(FacebookPost post) {
		JPA.em().merge(post);
	}

	public static FacebookPost findById(long id) {
		return JPA.em().find(FacebookPost.class, id);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FacebookPost)) {
			return false;
		}

		if (((FacebookPost) o).postKey.equals(this.postKey)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + (this.postKey != null ? this.postKey.hashCode() : 0);
		return hash;
	}

	public Long getId() {
		return id;
	}

	public List<FacebookComment> getComments() {
		return comments;
	}

	public void setComments(List<FacebookComment> comments) {
		this.comments = comments;
	}

	public String getPostKey() {
		return postKey;
	}

	public void setPostKey(String postKey) {
		this.postKey = postKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLink() {
		String[] ids = this.postKey.split("_");
		return "http://facebook.com/" + ids[0] + "/posts/" + ids[1];
	}

	public Long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}

	public Long getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	public Long getShareCount() {
		return shareCount;
	}

	public void setShareCount(Long shareCount) {
		this.shareCount = shareCount;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time")
	public Date getCreatedTime() {
		return new Date((long) createdTime * 1000);
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime.getTime() / 1000;
	}

	public FacebookProfile getAuthor() {
		return author;
	}

	public void setAuthor(FacebookProfile author) {
		this.author = author;
	}

	public FacebookProfile getSource() {
		return source;
	}

	public void setSource(FacebookProfile source) {
		this.source = source;
	}
}
