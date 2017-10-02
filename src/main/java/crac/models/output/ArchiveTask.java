package crac.models.output;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

import crac.enums.ConcreteTaskState;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;

public class ArchiveTask {

	private long id;

	private String name;

	private String description;

	private Calendar startTime;

	private Calendar endTime;

	private boolean evalComplete;

	private boolean evalTriggered;
	
	@JsonIdentityReference(alwaysAsId = true)
	private Evaluation evaluation;

	public ArchiveTask(UserTaskRel rel) {
		Task t = rel.getTask();
		this.id = t.getId();
		this.name = t.getName();
		this.description = t.getDescription();
		this.startTime = t.getStartTime();
		this.endTime = t.getEndTime();
		if (rel.getEvaluation() != null) {
			this.evalComplete = rel.getEvaluation().isFilled();
		} else {
			this.evalComplete = false;
		}
		this.evalTriggered = rel.isEvalTriggered();
		this.evaluation = rel.getEvaluation();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public boolean isEvalComplete() {
		return evalComplete;
	}

	public void setEvalComplete(boolean evalComplete) {
		this.evalComplete = evalComplete;
	}

	public boolean isEvalTriggered() {
		return evalTriggered;
	}

	public void setEvalTriggered(boolean evalTriggered) {
		this.evalTriggered = evalTriggered;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

}
