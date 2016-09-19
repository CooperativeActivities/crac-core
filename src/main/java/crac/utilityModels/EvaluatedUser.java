package crac.utilityModels;

import crac.models.CracUser;

public class EvaluatedUser implements Comparable{
	
	private CracUser User;
	
	private double assessment;

	public EvaluatedUser(CracUser user, double assessment) {
		User = user;
		this.assessment = assessment;
	}

	public CracUser getUser() {
		return User;
	}

	public void setUser(CracUser user) {
		User = user;
	}

	public double getAssessment() {
		return assessment;
	}

	public void setAssessment(double assessment) {
		this.assessment = assessment;
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
