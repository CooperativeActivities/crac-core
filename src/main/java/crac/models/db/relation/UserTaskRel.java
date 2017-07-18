package crac.models.db.relation;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.components.notifier.NotificationHelper;
import crac.components.notifier.notifications.EvaluationNotification;
import crac.components.utility.DataAccess;
import crac.enums.TaskParticipationType;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;

@Entity
@Table(name = "user_task_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserTaskRel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user_id")
	private CracUser user;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "task_id")
	private Task task;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "participation_type")
	private TaskParticipationType participationType;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "userTaskRel", cascade = CascadeType.PERSIST)
	private Evaluation evaluation;
	
	private boolean completed;

	private boolean evalTriggered;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "type_id")
	private TaskRelationshipType type;
	
	public UserTaskRel() {
		completed = false;
	}

	public long getId() {
		return id;
	}
	
	public Evaluation triggerEval(){
		Evaluation e = new Evaluation(this);
		
		EvaluationNotification es = new EvaluationNotification(user.getId(), task.getId(), e.getId());
		NotificationHelper.createNotification(es);
		
		e.setNotificationId(es.getNotificationId());
		es.setEvaluationId(e.getId());
		
		this.evaluation = e;
		this.setEvalTriggered(true);
		DataAccess.getRepo(UserTaskRelDAO.class).save(this);
		
		return e;
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

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public TaskParticipationType getParticipationType() {
		return participationType;
	}

	public void setParticipationType(TaskParticipationType participationType) {
		this.participationType = participationType;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public TaskRelationshipType getType() {
		return type;
	}

	public void setType(TaskRelationshipType type) {
		this.type = type;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	public boolean isEvalTriggered() {
		return evalTriggered;
	}

	public void setEvalTriggered(boolean evalTriggered) {
		this.evalTriggered = evalTriggered;
	}
	
}
