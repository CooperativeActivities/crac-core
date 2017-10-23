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

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.utility.NotificationConfiguration;
import crac.module.factories.NotificationFactory;
import crac.module.notifier.Notification;
import lombok.Data;

/**
 * The user-task-relationship entity
 * @author David Hondl
 *
 */
@Data
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

	public Evaluation triggerEval(NotificationFactory nf){
		Evaluation e = new Evaluation(this);
		
		NotificationConfiguration nc = NotificationConfiguration.create().put("task", task.toShort()).put("evaluation", e.getId());
		Notification es = nf.createSystemNotification(EvaluationNotification.class, user, nc);
		
		e.setNotificationId(es.getNotificationId());
		
		this.evaluation = e;
		this.setEvalTriggered(true);
		nf.getUserTaskRelDAO().save(this);
		
		nc.put("task", task).put("evaluationid", e);

		es.configure(nc);
		
		return e;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserTaskRel other = (UserTaskRel) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
