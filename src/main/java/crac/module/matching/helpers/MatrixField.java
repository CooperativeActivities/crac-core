package crac.module.matching.helpers;

import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import lombok.Data;

/**
 * Helperclass that represent a single field (similarity-value) of the CompetenceCollectionMatrix
 * Contains references to the relations between the user and task and the similarity value represented by the matrix-field
 * @author David Hondl
 *
 */
@Data
public class MatrixField {
	
	private CompetenceTaskRel taskRelation;
	private UserCompetenceRel userRelation;
	private double val;
	
	public MatrixField(CompetenceTaskRel taskRelation, UserCompetenceRel userRelation, double val) {
		this.taskRelation = taskRelation;
		this.userRelation = userRelation;
		this.val = val;
	}
	
}
