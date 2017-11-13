package crac.models.db.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.module.matching.interfaces.SyncableCrac;
import lombok.Data;

/**
 * Mapping competences to its competence areas.
 * @author Claudia Vojinovic-Peer
 *
 */
@Data
@Entity
@Table(name = "mapping_competencearea_competence")
@GenericGenerator(name="crac-mapping" , strategy="increment")
public class MappingCompetenceAreaCompetence implements SyncableCrac {
	
	@Id
	@GeneratedValue(generator = "crac-mapping", strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "competence_id")
	private Competence competence;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "competenceArea_id")
	private CompetenceArea competenceArea;
		
	@Override
	public void setDeprecated(boolean deprecated) {
	}

	@Override
	public long getId() {
		return this.id;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingCompetenceAreaCompetence other = (MappingCompetenceAreaCompetence) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

}
