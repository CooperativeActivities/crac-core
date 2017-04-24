package crac.components.matching.filter;

import java.util.ArrayList;

import crac.components.matching.CracFilter;
import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.models.storage.MatrixField;

public class UserRelationFilter extends CracFilter {

	public UserRelationFilter() {
		super("UserRelationFilter");
	}

	@Override
	public double apply(MatrixField m) {

		double value = m.getVal();
		CracUser user = m.getUserRelation().getUser();
		Task task = m.getTaskRelation().getTask();

		double newVal = value;

		ArrayList<UserRelationship> others = getRelatedPersons(user, task);

		for (UserRelationship rel : others) {
			double likeLevel = rel.getLikeValue();
			newVal = value * (1 + ((value / 4) * likeLevel / 2));
		}
		
		if (newVal > 1) {
			newVal = 1;
		} else if(newVal < 0){
			newVal = 0;
		}

		return newVal;

	}

	private ArrayList<UserRelationship> getRelatedPersons(CracUser user, Task t) {
		ArrayList<UserRelationship> others = new ArrayList<>();
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
		return others;
	}

}
