package crac.models.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.relation.UserTaskRel;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "evaluations")
public class Evaluation {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "evaluation_id")
	private long id;
		
	@JsonIdentityReference(alwaysAsId=true)
	@OneToOne(fetch = FetchType.LAZY)
	private UserTaskRel userTaskRel;
	
	@Column(name="like_val_others")
	private double likeValOthers;
	
	@Column(name="like_val_task")
	private double likeValTask;
	
	@Column(name="like_val_organisation")
	private double likeValOrganisation;
	
	private String feedback;
	
	@NotNull
	@Column(name="notification_id")
	private String notificationId;
	
	@NotNull
	private boolean filled;
	
	/**
	 * Defines a many to one relationship with the CracUser-entity
	 */
		
	/**
	 * constructors
	 */
	
	public Evaluation() {
	}
	
	public Evaluation(UserTaskRel userTaskRel) {
		this.userTaskRel = userTaskRel;
		this.filled = false;
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

	public double getLikeValOthers() {
		return likeValOthers;
	}

	public void setLikeValOthers(double likeValOthers) {
		this.likeValOthers = likeValOthers;
	}

	public double getLikeValTask() {
		return likeValTask;
	}

	public void setLikeValTask(double likeValTask) {
		this.likeValTask = likeValTask;
	}

	public double getLikeValOrganisation() {
		return likeValOrganisation;
	}

	public void setLikeValOrganisation(double likeValOrganisation) {
		this.likeValOrganisation = likeValOrganisation;
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

	public UserTaskRel getUserTaskRel() {
		return userTaskRel;
	}

	public void setUserTaskRel(UserTaskRel userTaskRel) {
		this.userTaskRel = userTaskRel;
	}

}
