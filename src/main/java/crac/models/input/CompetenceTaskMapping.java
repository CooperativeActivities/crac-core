package crac.models.input;

import lombok.Data;

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
