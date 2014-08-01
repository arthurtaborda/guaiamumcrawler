package models.facebook;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.JPA;

@Entity
@Table(name = "facebook_profile")
public class FacebookProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long dbId;

	@Column(name = "profile_id", unique = true)
	private String profileId;

	@Column
	private String name;

	@Column
	private String type;

	@Column(name = "is_source")
	private Boolean isSource;

	@Column(name = "last_time_scanned")
	private Long lastTimeScanned;

	@Column(name = "average_time_post")
	private Long averageTimePost;

	@Column(name = "totally_scanned")
	private Boolean totallyScanned;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "profile_id")
	private List<FacebookPost> posts;

	@OneToMany(cascade = { CascadeType.ALL })
	@JoinColumn(name = "profile_id")
	private List<FacebookComment> comments;

	public FacebookProfile() {
	}

	public FacebookProfile(String profileId, String name) {
		this.profileId = profileId;
		this.name = name;
	}

	public FacebookProfile(String profileId, String name, String type) {
		this.profileId = profileId;
		this.name = name;
		this.type = type;
	}

	public Long getDbId() {
		return dbId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean isSource() {
		return isSource;
	}

	public void setIsSource(Boolean isSource) {
		this.isSource = isSource;
	}

	public String getLink() {
		return "http://facebook.com/" + profileId;
	}

	public List<FacebookPost> getPosts() {
		return posts;
	}

	public void setPosts(List<FacebookPost> posts) {
		this.posts = posts;
	}

	public List<FacebookComment> getComments() {
		return comments;
	}

	public void setComments(List<FacebookComment> comments) {
		this.comments = comments;
	}

	public Long getLastTimeScanned() {
		return lastTimeScanned;
	}

	public void setLastTimeScanned(Long lastTimeScanned) {
		this.lastTimeScanned = lastTimeScanned;
	}

	public Long getAverageTimePost() {
		return averageTimePost;
	}

	public void setAverageTimePost(Long averageTimePost) {
		this.averageTimePost = averageTimePost;
	}

	public Boolean isTotallyScanned() {
		return totallyScanned;
	}

	public void setTotallyScanned(Boolean totallyScanned) {
		this.totallyScanned = totallyScanned;
	}

	@Override
	public String toString() {
		return "FacebookProfile [id=" + dbId + ", name=" + name + "]";
	}

	public static FacebookProfile findByProfileId(String key) {
		FacebookProfile profile;

		try {
			profile = (FacebookProfile) JPA.em().createQuery("from FacebookProfile where profileId = ?").setParameter(1, key).getSingleResult();
		} catch (javax.persistence.NoResultException e) {
			return null;
		}

		return profile;
	}

	public void save() {
		JPA.em().persist(this);
	}

	public void delete() {
		JPA.em().remove(this);
	}

	public static void update(FacebookProfile profile) {
		JPA.em().merge(profile);
	}

	public static FacebookProfile findById(long id) {
		return JPA.em().find(FacebookProfile.class, id);
	}

	@SuppressWarnings("unchecked")
	public static List<FacebookProfile> listProfiles() {
		return (List<FacebookProfile>) JPA.em().createQuery("from FacebookProfile").getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<FacebookProfile> listSources() {
		return (List<FacebookProfile>) JPA.em().createQuery("from FacebookProfile where isSource=true").getResultList();
	}
}
