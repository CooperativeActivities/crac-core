package crac.models.db.relation;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

/**
 * The competence-relationship-type
 * @author David Hondl
 *
 */
@Data
@Entity
@Table(name = "competence_relationship_type")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetenceRelationshipType {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "type_id")
	private long id;

	@NotNull
	private String name;
	
	private String description;
	
	@NotNull
	@Column(name = "distance_val")
	private double distanceVal;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "type", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceRelationship> mappedRelationships;

	public CompetenceRelationshipType() {
	}
	
	public void update(CompetenceRelationshipType crt){
		if (crt.getName() != null) {
			this.setName(crt.getName());
		}
		if (crt.getDescription() != null) {
			this.setDescription(crt.getDescription());
		}
		if (crt.getDistanceVal() >= 0) {
			this.setDistanceVal(crt.getDistanceVal());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompetenceRelationshipType other = (CompetenceRelationshipType) obj;
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
