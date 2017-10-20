package crac.models.input;

import lombok.Data;

/**
 * Helperclass that maps json-input to data that can be used to create a competence-task-relationship
 * Also handles default values for the relationship
 * @author David Hondl
 *
 */
@Data
public class CompetenceTaskMapping {
	
	private Long competenceId;
	private int neededProficiencyLevel;
	private int importanceLevel;
	private int mandatory;

	public CompetenceTaskMapping() {
		this.competenceId = 0l;
		this.neededProficiencyLevel = -200;
		this.importanceLevel = -200;
		this.mandatory = -1;
	}

}
