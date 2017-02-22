package crac.models.input;

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

	public Long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(Long competenceId) {
		this.competenceId = competenceId;
	}

	public int getNeededProficiencyLevel() {
		return neededProficiencyLevel;
	}

	public void setNeededProficiencyLevel(int neededProficiencyLevel) {
		this.neededProficiencyLevel = neededProficiencyLevel;
	}

	public int getImportanceLevel() {
		return importanceLevel;
	}

	public void setImportanceLevel(int importanceLevel) {
		this.importanceLevel = importanceLevel;
	}

	public int getMandatory() {
		return mandatory;
	}

	public void setMandatory(int mandatory) {
		this.mandatory = mandatory;
	}

}
