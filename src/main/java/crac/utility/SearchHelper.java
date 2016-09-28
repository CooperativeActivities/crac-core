package crac.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.Notification;
import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserRelationship;
import crac.relationmodels.UserTaskRel;
import crac.utilityModels.EvaluatedTask;
import crac.utilityModels.EvaluatedUser;
import crac.utilityModels.TaskSearchLogger;
import crac.utilityModels.TravelledCompetence;
import crac.utilityModels.TravelledCompetenceCollection;

@Service
public class SearchHelper {

	@Autowired
	CracUserDAO userDAO;

	@Autowired
	TaskDAO taskDAO;

	@Autowired
	UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	UserTaskRelDAO userTaskRelDAO;

	@Autowired
	UserRelationshipDAO userRelationshipDAO;

	@Autowired
	CompetenceAugmenter competenceAugmenter;

	@Value("${crac.search.bindUserRelations}")
	private boolean bindUserRelations;

	@Value("${crac.search.bindUserExperience}")
	private boolean bindUserExperience;

	@Value("${crac.search.augmentationCriteria}")
	private double augmentationCriteria;

	@Value("${crac.search.decreaseLikeFactor}")
	private int decreaseUserLikeFactor;

	// SECTION FOR TASK-SEARCH

	@SuppressWarnings("unchecked")
	public ArrayList<EvaluatedTask> findMatch(CracUser user) {

		TaskSearchLogger logger = TaskSearchLogger.getInstance();

		// LOG THE NAME
		logger.setTitlePerson(user.getName());

		Set<Competence> userCompetences = new HashSet<Competence>();

		for (UserCompetenceRel ucr : user.getCompetenceRelationships()) {
			userCompetences.add(ucr.getCompetence());
		}

		ArrayList<TravelledCompetenceCollection> competenceStacks = augmentAll(userCompetences);
		if (bindUserExperience)
			makeDependantOnUser(competenceStacks, user);
		ArrayList<EvaluatedTask> evaluatedTasks = findBestTasks(competenceStacks);
		if (bindUserRelations)
			considerUserRelationships(evaluatedTasks, user);
		Collections.sort(evaluatedTasks);
		return evaluatedTasks;

	}

	private ArrayList<TravelledCompetenceCollection> augmentAll(Set<Competence> competences) {

		ArrayList<TravelledCompetenceCollection> competenceCollections = new ArrayList<TravelledCompetenceCollection>();

		for (Competence c : competences) {
			HashMap<Long, TravelledCompetence> cMap = competenceAugmenter.augment(c, augmentationCriteria);
			competenceCollections.add(new TravelledCompetenceCollection(c.getId(), cMap));
		}

		return competenceCollections;

	}

	private void makeDependantOnUser(ArrayList<TravelledCompetenceCollection> competenceCollections, CracUser user) {

		for (TravelledCompetenceCollection collection : competenceCollections) {

			UserCompetenceRel rel = userCompetenceRelDAO.findByUserAndCompetence(user,
					collection.getStackedCompetences().get(collection.getMainId()).getCompetence());

			double proficiencyValue = rel.getProficiencyValue();
			double likeValue = rel.getLikeValue();

			for (Entry<Long, TravelledCompetence> entry : collection.getStackedCompetences().entrySet()) {
				entry.getValue().setTravelled(entry.getValue().getTravelled() * proficiencyValue * likeValue);
			}
		}

		/*
		 * considerProficiency(competenceCollections, user);
		 * considerLikeValue(competenceCollections, user);
		 * considerUserRelationships(competenceCollections, user);
		 */
	}

	private void considerUserRelationships(ArrayList<EvaluatedTask> tasks, CracUser user) {
		for (EvaluatedTask task : tasks) {
			Set<UserTaskRel> rels = task.getTask().getUserRelationships();

			for (UserTaskRel rel : rels) {
				UserRelationship urel = userRelationshipDAO.findByC1AndC2(user, rel.getUser());
				if (urel != null) {
					task.setAssessment(task.getAssessment() * adjustLikeValue(urel.getLikeValue()));
				}
			}

		}
	}

	private double adjustLikeValue(double val) {
		double result = val;

		for (int i = 0; i < decreaseUserLikeFactor; i++) {
			result = (result + 1) / 2;
		}

		return result;
	}

	/*
	 * private void considerProficiency(ArrayList<TravelledCompetenceCollection>
	 * competenceCollections, CracUser user) {
	 * 
	 * for (TravelledCompetenceCollection collection : competenceCollections) {
	 * double proficiencyValue = userCompetenceRelDAO
	 * .findByUserAndCompetence(user,
	 * collection.getStackedCompetences().get(collection.getMainId()).
	 * getCompetence()) .getProficiencyValue(); for (Entry<Long,
	 * TravelledCompetence> entry :
	 * collection.getStackedCompetences().entrySet()) {
	 * entry.getValue().setTravelled(entry.getValue().getTravelled() *
	 * PROFICIENCE_FACTOR * proficiencyValue); } }
	 * 
	 * }
	 */

	private ArrayList<EvaluatedTask> findBestTasks(ArrayList<TravelledCompetenceCollection> competenceStacks) {
		ArrayList<EvaluatedTask> evaluatedTasks = new ArrayList<EvaluatedTask>();

		for (Task task : taskDAO.findAll()) {
			TaskSearchLogger logger = TaskSearchLogger.getInstance();
			logger.setTitleTask(task.getName());
			double comparationValue = compareTaskWithUser(competenceStacks, task.getNeededCompetences());
			if (comparationValue > 0) {
				evaluatedTasks.add(new EvaluatedTask(task, comparationValue));
			}
		}

		return evaluatedTasks;
	}

	private double compareTaskWithUser(ArrayList<TravelledCompetenceCollection> competenceStacks,
			Set<Competence> singleCompetences) {
		double completeValue = 0;
		double rowCount = 0;
		double count = 0;
		TaskSearchLogger logger = TaskSearchLogger.getInstance();
		TaskSearchLogger.emptyInstance();
		for (Competence taskC : singleCompetences) {
			double rowValue = 0;

			// DATA GETS LOGGED
			logger.addColumnTitle(taskC.getName());

			for (TravelledCompetenceCollection userStack : competenceStacks) {
				double additionalValue = compareCompetenceWithAugmented(taskC, userStack);
				if (additionalValue > 0) {
					count++;
					if (additionalValue > rowValue) {
						rowValue = additionalValue;
					}
				}

				// DATA GETS LOGGED
				logger.addRowTitle(
						userStack.getStackedCompetences().get(userStack.getMainId()).getCompetence().getName());
				logger.addValue(additionalValue, (int) rowCount);
			}
			completeValue += rowValue /* / count */;
			rowCount++;

		}
		// DATA GETS PRINTED
		logger.print();
		System.out.println("VALS: " + completeValue + " divided by " + rowCount);
		if (rowCount == 0)
			rowCount = 1;
		if (count == 0)
			count = 1;
		return completeValue / rowCount;
	}

	private double compareCompetenceWithAugmented(Competence taskC, TravelledCompetenceCollection userCStack) {

		HashMap<Long, TravelledCompetence> userC = userCStack.getStackedCompetences();

		if (userC.containsKey(taskC.getId())) {
			return userC.get(taskC.getId()).getTravelled();
		} else {
			return 0.0;
		}

	}

	// SECTION FOR USER-SEARCH

	@SuppressWarnings("unchecked")
	public ArrayList<EvaluatedUser> findMatch(Task task) {

		TaskSearchLogger logger = TaskSearchLogger.getInstance();

		// LOG THE NAME
		logger.setTitleTask(task.getName());

		Set<Competence> taskCompetences = task.getNeededCompetences();

		ArrayList<TravelledCompetenceCollection> competenceStacks = augmentAll(taskCompetences);
		// makeDependantOnTask(competenceStacks, task);
		ArrayList<EvaluatedUser> evaluatedUsers = findBestUsers(competenceStacks);
		Collections.sort(evaluatedUsers);
		return evaluatedUsers;

	}

	private ArrayList<EvaluatedUser> findBestUsers(ArrayList<TravelledCompetenceCollection> competenceStacks) {
		ArrayList<EvaluatedUser> evaluatedUsers = new ArrayList<EvaluatedUser>();

		for (CracUser user : userDAO.findAll()) {
			TaskSearchLogger logger = TaskSearchLogger.getInstance();
			logger.setTitlePerson(user.getName());

			Set<Competence> userCompetences = new HashSet<Competence>();

			for (UserCompetenceRel ucr : user.getCompetenceRelationships()) {
				userCompetences.add(ucr.getCompetence());
			}

			double comparationValue = compareTaskWithUser(competenceStacks, userCompetences);
			if (comparationValue > 0) {
				evaluatedUsers.add(new EvaluatedUser(user, comparationValue));
			}
		}

		return evaluatedUsers;
	}

}
