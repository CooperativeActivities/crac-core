package crac.models.storage;

import crac.models.Competence;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserCompetenceRel;

public class MatrixField {
	
	private CompetenceTaskRel taskRelation;
	private UserCompetenceRel userRelation;
	private double val;
	
	public MatrixField(CompetenceTaskRel taskRelation, UserCompetenceRel userRelation, double val) {
		this.taskRelation = taskRelation;
		this.userRelation = userRelation;
		this.val = val;
	}
		
	public CompetenceTaskRel getTaskRelation() {
		return taskRelation;
	}

	public void setTaskRelation(CompetenceTaskRel taskRelation) {
		this.taskRelation = taskRelation;
	}

	public UserCompetenceRel getUserRelation() {
		return userRelation;
	}

	public void setUserRelation(UserCompetenceRel userRelation) {
		this.userRelation = userRelation;
	}

	public double getVal() {
		return val;
	}
	
	public void setVal(double val) {
		this.val = val;
	}
	
}
