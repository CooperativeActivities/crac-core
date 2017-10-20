package crac.models.db.relation;

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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import lombok.Data;

/**
 * The competence-task-relationship entity
 * @author David Hondl
 *
 */
@Data
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
	@Column(name = "needed_proficiency_level")
	private int neededProficiencyLevel;
	
	//0-100
	@Column(name = "importance_level")
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
	
	@JsonIgnore
	public CompetenceTaskRel copy(Task t){
		CompetenceTaskRel c = new CompetenceTaskRel();
		c.setCompetence(competence);
		c.setNeededProficiencyLevel(neededProficiencyLevel);
		c.setImportanceLevel(importanceLevel);
		c.setMandatory(mandatory);
		c.setTask(t);
		return c;
	}
	
}
