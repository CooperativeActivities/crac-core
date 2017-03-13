package crac.models.output;

import crac.models.db.entities.Competence;

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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getRelationValue() {
		return relationValue;
	}

	public void setRelationValue(double relationValue) {
		this.relationValue = relationValue;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
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

}
