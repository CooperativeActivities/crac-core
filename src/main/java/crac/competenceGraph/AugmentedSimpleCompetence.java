package crac.competenceGraph;

import crac.daos.CompetenceDAO;
import crac.models.Competence;

public class AugmentedSimpleCompetence {
	
	private SimpleCompetence comp;
	private Competence concreteComp;
	private double travelledDistance;
	private int stepsDone;
	private double finalValue;
	
	public AugmentedSimpleCompetence(SimpleCompetence comp) {
		this.comp = comp;
		travelledDistance = 0;
		stepsDone = 0;
		concreteComp = null;
		finalValue = 0;
	}

	public SimpleCompetence getComp() {
		return comp;
	}

	public void setComp(SimpleCompetence comp) {
		this.comp = comp;
	}

	public double getTravelledDistance() {
		return travelledDistance;
	}

	public void setTravelledDistance(double travelledDistance) {
		this.travelledDistance = travelledDistance;
	}

	public int getStepsDone() {
		return stepsDone;
	}

	public void setStepsDone(int stepsDone) {
		this.stepsDone = stepsDone;
	}

	public Competence getConcreteComp() {
		return concreteComp;
	}

	public void setConcreteComp(Competence concreteComp) {
		this.concreteComp = concreteComp;
	}

	public double getFinalValue() {
		return finalValue;
	}

	public void setFinalValue(double finalValue) {
		this.finalValue = finalValue;
	}
	
	public void loadConcreteCompetence(CompetenceDAO competenceDAO){
		this.concreteComp = competenceDAO.findOne(this.comp.getId());
	}

}
