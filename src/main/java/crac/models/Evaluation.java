package crac.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "evaluations")
public class Evaluation {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "evaluation_id")
	private long id;
	
	private int likeValOthers;
	
	private int likeValTask;
	
	private int likeValOrganisation;

	private int grade;
	
	private String feedback;
	
	@NotNull
	private String notificationId;
	
	@NotNull
	private boolean filled;
	
	/**
	 * Defines a many to one relationship with the CracUser-entity
	 */
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user_id")
	private CracUser user;
	
	/**
	 * constructors
	 */
	
	public Evaluation(CracUser user) {
		this.user = user;
		this.filled = false;
	}

	public Evaluation() {
	}

	/**
	 * getters and setters
	 */
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CracUser getUser() {
		return user;
	}

	public void setUser(CracUser user) {
		this.user = user;
	}

	public int getLikeValOthers() {
		return likeValOthers;
	}

	public void setLikeValOthers(int likeValOthers) {
		this.likeValOthers = likeValOthers;
	}

	public int getLikeValTask() {
		return likeValTask;
	}

	public void setLikeValTask(int likeValTask) {
		this.likeValTask = likeValTask;
	}

	public int getLikeValOrganisation() {
		return likeValOrganisation;
	}

	public void setLikeValOrganisation(int likeValOrganisation) {
		this.likeValOrganisation = likeValOrganisation;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public boolean isFilled() {
		return filled;
	}

	public void setFilled(boolean filled) {
		this.filled = filled;
	}
		
}
