package crac.module.matching.filter.matching;

import java.util.ArrayList;
import java.util.HashMap;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * This matching-filter modifies the matching score by adjusting competence-similarities based on the like-value of every user to the other users connected to the task
 * @author David Hondl
 *
 */
public class UserRelationFilter extends ConcreteFilter {
	
	private double userLikeVal;
	private boolean calc = true;

	public UserRelationFilter() {
		super("UserRelationFilter");
	}

	@Override
	public void apply(FilterParameters fp) {
		MatrixField m = fp.getM();

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
		
		m.setVal(newVal);

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
