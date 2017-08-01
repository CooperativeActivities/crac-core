package crac.module.matching.filter.matching;

import java.util.ArrayList;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.CracMatchingFilter;

public class UserRelationFilter extends CracMatchingFilter {
	
	private CracUser setUser;
	private double userLikeVal;
	private boolean calc = true;

	public UserRelationFilter() {
		super("UserRelationFilter");
	}

	@Override
	public Double apply(MatrixField m) {

		double value = m.getVal();
		CracUser user = m.getUserRelation().getUser();
		Task task = m.getTaskRelation().getTask();

		if(calc){
			calc = false;
			calcRelatedVal(user, task);
		}
			
		double newVal = value * (1 + (1 - value) * userLikeVal/100);
		
		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}

		System.out.println("Applied: "+super.speakString());
		
		return newVal;

	}

	private void calcRelatedVal(CracUser user, Task t) {
		ArrayList<UserRelationship> others = new ArrayList<>();
		double likeAverage = 0;
		for (UserTaskRel trel : t.getUserRelationships()) {
			if (trel.getParticipationType() == TaskParticipationType.PARTICIPATING) {
				for (UserRelationship urel : trel.getUser().getUserRelationshipsAs1()) {
					if (urel.getC2().getId() == user.getId()) {
						others.add(urel);
					}
				}
				for (UserRelationship urel : trel.getUser().getUserRelationshipsAs2()) {
					if (urel.getC1().getId() == user.getId()) {
						others.add(urel);
					}
				}
			}
		}
		
		for(UserRelationship rel : others){
			likeAverage += rel.getLikeValue();
		}
		
		if(others.size() > 0){
			userLikeVal = likeAverage/others.size();
		}else{
			userLikeVal = 0;
		}
		
	}

}
