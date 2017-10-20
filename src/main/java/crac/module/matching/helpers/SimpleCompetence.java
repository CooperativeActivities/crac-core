package crac.module.matching.helpers;

import java.util.ArrayList;
import java.util.List;

import crac.models.db.entities.Competence;
import lombok.Data;

/**
 * Helperclass that represents a simplified version of the competence-class
 * @author David Hondl
 *
 */
@Data
public class SimpleCompetence {

	private Long id;
	private List<SimpleCompetenceRelation> relations;
	
	public SimpleCompetence(Competence comp) {
		this.id = comp.getId();
		this.relations = new ArrayList<SimpleCompetenceRelation>();
	}
	
	public void addRelation(SimpleCompetenceRelation rel){
		this.relations.add(rel);
	}

}
