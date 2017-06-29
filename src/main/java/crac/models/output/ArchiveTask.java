package crac.models.output;

import java.util.Calendar;

import crac.enums.TaskState;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;

public class ArchiveTask {
	
	private long id;

	private String name;

	private String description;

	private Calendar startTime;

	private Calendar endTime;
	
	private boolean evaluated;

	private boolean evaluationTriggered;
	
	public ArchiveTask(UserTaskRel rel){
		Task t = rel.getTask();
		this.id = t.getId();
		this.name = t.getName();
		this.description = t.getDescription();
		this.startTime = t.getStartTime();
		this.endTime = t.getEndTime();
		this.evaluated = rel.isEvaluated();
		this.evaluationTriggered = rel.isEvaluationTriggered();
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

	public boolean isEvaluated() {
		return evaluated;
	}

	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}

	public boolean isEvaluationTriggered() {
		return evaluationTriggered;
	}

	public void setEvaluationTriggered(boolean evaluationTriggered) {
		this.evaluationTriggered = evaluationTriggered;
	}

}
