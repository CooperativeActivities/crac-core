package crac.module.matching.helpers;

import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;

public class AugmentedSimpleCompetence {
	
	private SimpleCompetence comp;
	private Competence concreteComp;
	private double similarity;
	private int stepsDone;
	private int paths;
	
	public AugmentedSimpleCompetence(SimpleCompetence comp) {
		this.comp = comp;
		similarity = 0;
		stepsDone = 0;
		concreteComp = null;
		paths = 0;
	}

	public SimpleCompetence getComp() {
		return comp;
	}

	public void setComp(SimpleCompetence comp) {
		this.comp = comp;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
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

	public void loadConcreteCompetence(CompetenceDAO competenceDAO){
		this.concreteComp = competenceDAO.findOne(this.comp.getId());
	}

	public int getPaths() {
		return paths;
	}

	public void setPaths(int paths) {
		this.paths = paths;
	}

}
