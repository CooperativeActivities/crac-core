package crac.models.output;

import crac.models.db.entities.Competence;
import lombok.Data;

@Data
public class CompetenceRelationDetails {
	
	private Long id;
	private String name;
	private int neededProficiencyLevel;
	private int importanceLevel;
	private double relationValue;
	private boolean mandatory;
	
	public CompetenceRelationDetails(Competence c){
		this.id = c.getId();
		this.name = c.getName();
		this.relationValue = 0;
		this.mandatory = false;
		this.neededProficiencyLevel = 0;
		this.importanceLevel = 0;
	}

}
