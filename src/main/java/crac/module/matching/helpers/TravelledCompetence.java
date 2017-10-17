package crac.module.matching.helpers;

import crac.models.db.entities.Competence;
import lombok.Data;

@Data
public class TravelledCompetence {
	
	private Competence competence;
	private double travelled;
	private double calculatedScore;

	public TravelledCompetence(Competence competence, double travelled, double calculatedScore) {
		this.competence = competence;
		this.travelled = travelled;
	}

}
