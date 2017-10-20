package crac.models.db.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.Competence;
import crac.module.matching.interfaces.SyncableCrac;
import lombok.Data;

/**
 * The competence-relationship entity
 * @author David Hondl
 *
 */
@Data
@Entity
@Table(name = "competence_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetenceRelationship implements SyncableCrac {

	@Id
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
	
	@Column(name = "uni_direction")
	private boolean uniDirection;
	
	private boolean deprecated;
	
	public CompetenceRelationship() {
		this.uniDirection = false;
	}
	
}
