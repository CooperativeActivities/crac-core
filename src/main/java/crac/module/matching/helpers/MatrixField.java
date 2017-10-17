package crac.module.matching.helpers;

import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import lombok.Data;

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
