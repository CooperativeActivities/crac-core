package crac.relationmodels;

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

@Entity
@Table(name = "competence_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetenceRelationship {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "competence_relationship_id")
	private long id;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "type_id")
	private CompetenceRelationshipType type;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "competence_1")
	private Competence competence1;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "competence_2")
	private Competence competence2;
	
	private boolean uniDirection;
	
	public CompetenceRelationship() {
		this.uniDirection = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CompetenceRelationshipType getType() {
		return type;
	}

	public void setType(CompetenceRelationshipType type) {
		this.type = type;
	}

	public Competence getCompetence1() {
		return competence1;
	}

	public void setCompetence1(Competence competence1) {
		this.competence1 = competence1;
	}

	public Competence getCompetence2() {
		return competence2;
	}

	public void setCompetence2(Competence competence2) {
		this.competence2 = competence2;
	}

	public boolean isUniDirection() {
		return uniDirection;
	}

	public void setUniDirection(boolean uniDirection) {
		this.uniDirection = uniDirection;
	}
	
	
}
