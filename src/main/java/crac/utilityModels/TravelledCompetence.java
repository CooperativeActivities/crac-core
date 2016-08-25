package crac.utilityModels;

import crac.models.Competence;

public class TravelledCompetence {
	
	private Competence competence;
	
	private double travelled;

	public TravelledCompetence(Competence competence, double travelled) {
		this.competence = competence;
		this.travelled = travelled;
	}

	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}

	public double getTravelled() {
		return travelled;
	}

	public void setTravelled(double travelled) {
		this.travelled = travelled;
	}
	
	

}
