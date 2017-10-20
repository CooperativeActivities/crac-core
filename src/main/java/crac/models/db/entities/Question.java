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
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * The question entity
 * @author David Hondl
 *
 */
@Entity
@Table(name = "question")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Question {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "question_id")
	private long id;

	@NotNull
	private String name;
	
	@NotNull
	private String content;
	
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(mappedBy = "mappedQuestions")
	Set<Competence> mappedCompetences;

	public Question() {
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Set<Competence> getMappedCompetences() {
		return mappedCompetences;
	}

	public void setMappedCompetences(Set<Competence> mappedCompetences) {
		this.mappedCompetences = mappedCompetences;
	}


}
