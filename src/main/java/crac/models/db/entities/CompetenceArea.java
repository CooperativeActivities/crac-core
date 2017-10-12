package crac.models.db.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.module.matching.interfaces.SyncableCrac;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "competence_area")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetenceArea implements SyncableCrac {

	@Id
	@Column(name = "competence_area_id")
	private long id;

	@NotNull
	@Type(type="text")
	private String name;
	
	@NotNull
	@Column(columnDefinition="TEXT")
	private String description;
	
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(mappedBy = "competenceAreas")
	Set<Competence> mappedCompetences;

	@Getter
	@Setter
	private boolean deprecated;

	public CompetenceArea() {
		deprecated = false;
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

	public Set<Competence> getMappedCompetences() {
		return mappedCompetences;
	}

	public void setMappedCompetences(Set<Competence> mappedCompetences) {
		this.mappedCompetences = mappedCompetences;
	}
	
	
	
}
