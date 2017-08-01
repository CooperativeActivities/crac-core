package crac.module.matching.helpers;

import java.util.ArrayList;
import java.util.List;

import crac.models.db.entities.Competence;

public class SimpleCompetence {

	Long id;
	List<SimpleCompetenceRelation> relations;
	
	public SimpleCompetence(Competence comp) {
		this.id = comp.getId();
		this.relations = new ArrayList<SimpleCompetenceRelation>();
	}
	
	public void addRelation(SimpleCompetenceRelation rel){
		this.relations.add(rel);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<SimpleCompetenceRelation> getRelations() {
		return relations;
	}
	public void setRelations(List<SimpleCompetenceRelation> relations) {
		this.relations = relations;
	}
	
	
	
}
