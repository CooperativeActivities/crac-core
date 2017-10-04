package crac.models.output;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import crac.enums.ConcreteTaskState;
import crac.models.db.entities.Task;

public class TaskShort {
	
	private long id;

	private String name;

	private String description;

	private Calendar startTime;

	private Calendar endTime;
	
	private ConcreteTaskState taskState;
	
	private boolean readyToPublish;
	
	public TaskShort(Task t){
		this.id = t.getId();
		this.name = t.getName();
		this.description = t.getDescription();
		this.startTime = t.getStartTime();
		this.endTime = t.getEndTime();
		this.taskState = t.getTaskState();
		this.readyToPublish = t.isReadyToPublish();
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

	public ConcreteTaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(ConcreteTaskState taskState) {
		this.taskState = taskState;
	}

	public boolean isReadyToPublish() {
		return readyToPublish;
	}

	public void setReadyToPublish(boolean readyToPublish) {
		this.readyToPublish = readyToPublish;
	}

}
