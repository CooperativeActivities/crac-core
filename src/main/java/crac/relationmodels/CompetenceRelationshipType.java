package crac.relationmodels;

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
	private int distanceVal;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "type", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceRelationship> mappedRelationships;

	public CompetenceRelationshipType() {
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

	public Set<CompetenceRelationship> getMappedRelationships() {
		return mappedRelationships;
	}

	public void setMappedRelationships(Set<CompetenceRelationship> mappedRelationships) {
		this.mappedRelationships = mappedRelationships;
	}

	public int getDistanceVal() {
		return distanceVal;
	}

	public void setDistanceVal(int distanceVal) {
		this.distanceVal = distanceVal;
	}
	
	
	
}
