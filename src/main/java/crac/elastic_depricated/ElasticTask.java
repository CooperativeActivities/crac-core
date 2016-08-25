package crac.elastic_depricated;

import java.sql.Timestamp;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;

public class ElasticTask {
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	private String id;

	private Set<ElasticCompetence> neededCompetences;

	private String name;

	private String description;

	public ElasticTask(String id , String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public ElasticTask() {
	}

	public Set<ElasticCompetence> getNeededCompetences() {
		return neededCompetences;
	}

	public void setNeededCompetences(Set<ElasticCompetence> neededCompetences) {
		this.neededCompetences = neededCompetences;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
}
