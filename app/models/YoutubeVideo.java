package models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.JPA;

@Entity
@Table(name = "YOUTUBE_VIDEOS")
public class YoutubeVideo {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public Long id;

	@Column(name = "TITLE")
	public String title;

	@Column(name = "USERNAME")
	public String username;

	@Column(name = "VIEW_COUNT")
	public Long viewCount;

	@Column(name = "COMMENT_COUNT")
	public Integer commentCount;

	@Column(name = "LIKE_COUNT")
	public Integer likeCount;

	@Column(name = "DISLIKE_COUNT")
	public Integer dislikeCount;

	public Long uploadDate;

	@Column(name = "LINK")
	public String link;

	/**
	 *
	 */
	public YoutubeVideo() {
		super();
	}

	/**
	 * @param id
	 * @param title
	 * @param viewCount
	 * @param commentCount
	 * @param likeCount
	 * @param dislikeCount
	 * @param date
	 * @param link
	 */
	public YoutubeVideo(String title, String username, Long viewCount, Integer commentCount, Integer likeCount, Integer dislikeCount, Long uploadDate,
			String link) {
		super();
		this.title = title;
		this.username = username;
		this.viewCount = viewCount;
		this.commentCount = commentCount;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.uploadDate = uploadDate;
		this.link = link;
	}

	@SuppressWarnings("unchecked")
	public static List<YoutubeVideo> findByUsername(String username) {
		List<YoutubeVideo> videos;

		if (username == null || username.isEmpty())
			username = "%";

		try {
			videos = (List<YoutubeVideo>) JPA.em().createQuery("from YoutubeVideo y where y.username like ?").setParameter(1, username).getResultList();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}

		return videos;
	}

	/**
	 * @return the uploadDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPLOAD_DATE")
	public Date getUploadDate() {
		return new Date((long) uploadDate * 1000);
	}

	@SuppressWarnings("unchecked")
	public static List<String> listUsernames() {
		return (List<String>) JPA.em().createQuery("select distinct c.username from YoutubeVideo c").getResultList();
	}

	@SuppressWarnings("unchecked")
	public static Page<YoutubeVideo> page(int page, int pageSize, String sortBy, String order, String filter, String username) {

		if (username == null || username.isEmpty())
			username = "%";

		int total = (Integer) JPA.em().createQuery("select count(c) from YoutubeVideo c where lower(c.title) like ? AND username like ?")
				.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, username).getSingleResult();

		if (page <= 0) { // if page is 0 or less, get last page.
			if (total < pageSize) {
				page = 1;
			} else {
				page = new BigDecimal(total).divide(new BigDecimal(pageSize)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			}
		}

		List<YoutubeVideo> data = JPA.em()
				.createQuery("from YoutubeVideo c where lower(c.title) like ? AND username like ? order by c." + sortBy + " " + order)
				.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, username).setFirstResult((page - 1) * pageSize).setMaxResults(pageSize)
				.getResultList();
		return new Page<YoutubeVideo>(data, total, page, pageSize);
	}

	public void save() {
		JPA.em().persist(this);
	}

	public void delete() {
		JPA.em().remove(this);
	}
}
