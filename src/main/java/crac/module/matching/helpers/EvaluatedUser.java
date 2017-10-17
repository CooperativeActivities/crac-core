package crac.module.matching.helpers;

import crac.models.db.entities.CracUser;
import lombok.Data;

@Data
public class EvaluatedUser implements Comparable<Object>{
	
	private CracUser User;
	
	private double assessment;
	
	private boolean doable;

	public EvaluatedUser(CracUser user, double assessment) {
		User = user;
		this.assessment = assessment;
		this.doable = true;
	}

	@Override
	public int compareTo(Object user) {
		if(((EvaluatedUser) user).getAssessment() > this.getAssessment()){
			return 1;
		}else if(((EvaluatedUser) user).getAssessment() == this.getAssessment()){
			return 0;
		}else{
			return -1;
		}
	}

	
}
