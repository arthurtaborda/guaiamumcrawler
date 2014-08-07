package models;

//@Entity
//@Table(name = "TWITTER_TWEET")
public class TwitterTweet {

	//	@Id
	//	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public Long id;
	//
	//	@Column(name = "TITLE")
	public String title;
	//
	//	@Column(name = "USERNAME")
	public String username;
	//
	//	@Column(name = "VIEW_COUNT")
	public Long viewCount;
	//
	//	@Column(name = "COMMENT_COUNT")
	public Integer commentCount;
	//
	//	@Column(name = "LIKE_COUNT")
	public Integer likeCount;
	//
	//	@Column(name = "DISLIKE_COUNT")
	public Integer dislikeCount;
	//
	public Long uploadDate;
	//
	//	@Column(name = "LINK")
	public String link;

	//
	//	/**
	//	 *
	//	 */
	//	public TwitterTweet() {
	//		super();
	//	}
	//
	//	/**
	//	 * @param id
	//	 * @param title
	//	 * @param viewCount
	//	 * @param commentCount
	//	 * @param likeCount
	//	 * @param dislikeCount
	//	 * @param date
	//	 * @param link
	//	 */
	//	public TwitterTweet(String title, String username, Long viewCount, Integer commentCount, Integer likeCount, Integer dislikeCount, Long uploadDate,
	//			String link) {
	//		super();
	//		this.title = title;
	//		this.username = username;
	//		this.viewCount = viewCount;
	//		this.commentCount = commentCount;
	//		this.likeCount = likeCount;
	//		this.dislikeCount = dislikeCount;
	//		this.uploadDate = uploadDate;
	//		this.link = link;
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public static List<TwitterTweet> findByUsername(String username) {
	//		List<TwitterTweet> videos;
	//
	//		if (username == null || username.isEmpty())
	//			username = "%";
	//
	//		try {
	//			videos = (List<TwitterTweet>) JPA.em().createQuery("from YoutubeVideo y where y.username like ?").setParameter(1, username).getResultList();
	//		} catch (javax.persistence.NoResultException e) {
	//			return null;
	//		}
	//
	//		return videos;
	//	}
	//
	//	/**
	//	 * @return the uploadDate
	//	 */
	//	@Temporal(TemporalType.TIMESTAMP)
	//	@Column(name = "UPLOAD_DATE")
	//	public Date getUploadDate() {
	//		return new Date((long) uploadDate * 1000);
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public static List<String> listUsernames() {
	//		return (List<String>) JPA.em().createQuery("select distinct c.username from YoutubeVideo c").getResultList();
	//	}
	//
	//@SuppressWarnings("unchecked")
	public static Page<TwitterTweet> page(int page, int pageSize, String sortBy, String order, String filter, String username) {
		return null;
		//			if (username == null || username.isEmpty())
		//				username = "%";
		//	
		//			Long total = (Long) JPA.em().createQuery("select count(c) from YoutubeVideo c where lower(c.title) like ? AND username like ?")
		//					.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, username).getSingleResult();
		//	
		//			if (page <= 0) { // if page is 0 or less, get last page.
		//				if (total < pageSize) {
		//					page = 1;
		//				} else {
		//					page = new BigDecimal(total).divide(new BigDecimal(pageSize)).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		//				}
		//			}
		//	
		//			List<TwitterTweet> data = JPA.em()
		//					.createQuery("from YoutubeVideo c where lower(c.title) like ? AND username like ? order by c." + sortBy + " " + order)
		//					.setParameter(1, "%" + filter.toLowerCase() + "%").setParameter(2, username).setFirstResult((page - 1) * pageSize).setMaxResults(pageSize)
		//					.getResultList();
		//			return new Page<TwitterTweet>(data, total, page, pageSize);
	}
	//
	//	public void save() {
	//		JPA.em().persist(this);
	//	}
	//
	//	public void delete() {
	//		JPA.em().remove(this);
	//	}
}
