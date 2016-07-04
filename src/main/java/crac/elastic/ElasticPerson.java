package crac.elastic;

import java.util.Date;
import java.util.Set;

import crac.models.Attachment;
import crac.models.Competence;
import crac.models.Group;
import crac.models.Project;
import crac.models.Role;
import crac.models.Task;
import crac.models.UserCompetenceRel;

public class ElasticPerson {

	private long id;

	private String name;
	
	private Set<ElasticCompetence> setCompetences;
	
	private Set<ElasticCompetence> relatedCompetences;

	
	public ElasticPerson(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public ElasticPerson() {
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
	public Set<ElasticCompetence> getRelatedCompetences() {
		return relatedCompetences;
	}

	public void setRelatedCompetences(Set<ElasticCompetence> relatedCompetences) {
		this.relatedCompetences = relatedCompetences;
	}

	public Set<ElasticCompetence> getSetCompetences() {
		return setCompetences;
	}

	public void setSetCompetences(Set<ElasticCompetence> setCompetences) {
		this.setCompetences = setCompetences;
	}

}
