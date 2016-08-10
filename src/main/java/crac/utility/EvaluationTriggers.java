package crac.utility;

import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.notifier.NotificationHelper;
import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserRelationship;

public class EvaluationTriggers {

	public static String lowerFriendship(CracUser c1, CracUser c2, UserRelationshipDAO userRelDAO) {
		UserRelationship rel = userRelDAO.findByC1AndC2(c1, c2);

		if (rel == null) {
			rel = new UserRelationship();
			rel.setC1(c1);
			rel.setC2(c2);
			rel.setFriends(false);
			rel.setLikeValue(-1);
		} else {
			rel.setLikeValue(rel.getLikeValue() - 1);
		}

		userRelDAO.save(rel);

		return "Like value between user "+c1.getName()+" and user "+c2.getName()+"lowered";
	}

	public static String increaseFriendship(CracUser c1, CracUser c2, UserRelationshipDAO userRelDAO) {
		UserRelationship rel = userRelDAO.findByC1AndC2(c1, c2);

		if (rel == null) {
			rel = new UserRelationship();
			rel.setC1(c1);
			rel.setC2(c2);
			rel.setFriends(false);
			rel.setLikeValue(1);
		} else {
			rel.setLikeValue(rel.getLikeValue() + 1);
		}

		userRelDAO.save(rel);
		
		if(!rel.isFriends() && rel.getLikeValue() > 2){
			NotificationHelper.createFriendSuggestion(c1.getId(), c2.getId());
			NotificationHelper.createFriendSuggestion(c2.getId(), c1.getId());
			return "Like value between user "+c1.getName()+" and user "+c2.getName()+"increased, friend suggestion sent";
		}

		return "Like value between user "+c1.getName()+" and user "+c2.getName()+"increased";
	}

	public static String lowerCompetenceLike(CracUser cu, Competence co, UserCompetenceRelDAO userCompRelDAO) {
		UserCompetenceRel rel = userCompRelDAO.findByUserAndCompetence(cu, co);
		
		if (rel == null) {
			rel = new UserCompetenceRel();
			rel.setCompetence(co);
			rel.setLikeValue(-1);
			rel.setUser(cu);
		} else {
			rel.setLikeValue(rel.getLikeValue() - 1);
		}

		userCompRelDAO.save(rel);
		
		return "Like value for competence "+co.getName()+" increased for user "+cu.getName();
	}

	public static String increaseCompetenceLike(CracUser cu, Competence co, UserCompetenceRelDAO userCompRelDAO) {
		UserCompetenceRel rel = userCompRelDAO.findByUserAndCompetence(cu, co);
		
		if (rel == null) {
			rel = new UserCompetenceRel();
			rel.setCompetence(co);
			rel.setLikeValue(1);
			rel.setUser(cu);
		} else {
			rel.setLikeValue(rel.getLikeValue() + 1);
		}

		userCompRelDAO.save(rel);
		
		return "increased";
	}

}
