package crac.models.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.Competence;
import crac.models.Task;

@Entity
@Table(name = "competence_task_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetenceTaskRel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "competence_id")
	private Competence competence;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "task_id")
	private Task task;
	
	//0-100
	private int neededProficiencyLevel;
	
	//0-100
	private int importanceLevel;
	
	private boolean mandatory;

	public CompetenceTaskRel() {
		this.neededProficiencyLevel = 0;
		this.importanceLevel = 0;
		this.mandatory = false;
	}
	
	public CompetenceTaskRel(Competence competence, Task task, int neededProficiencyLevel, int importanceLevel, boolean mandatory) {
		this.competence = competence;
		this.task = task;
		this.neededProficiencyLevel = neededProficiencyLevel;
		this.importanceLevel = importanceLevel;
		this.mandatory = mandatory;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public int getNeededProficiencyLevel() {
		return neededProficiencyLevel;
	}

	public void setNeededProficiencyLevel(int neededProficiencyLevel) {
		this.neededProficiencyLevel = neededProficiencyLevel;
	}

	public int getImportanceLevel() {
		return importanceLevel;
	}

	public void setImportanceLevel(int importanceLevel) {
		this.importanceLevel = importanceLevel;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
}
