package crac.module.matching.helpers;

import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import lombok.Data;

/**
 * Helperclass that saves different values of the graph-traversal-process in addition to the actual competence
 * @author David Hondl
 *
 */
@Data
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

	public void loadConcreteCompetence(CompetenceDAO competenceDAO){
		this.concreteComp = competenceDAO.findOne(this.comp.getId());
	}


}
