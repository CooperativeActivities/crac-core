package crac.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.Notification;
import crac.relationmodels.UserCompetenceRel;
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
	CompetenceAugmenter competenceAugmenter;

	private static final double CRITERIA = 0.2;
	private static final double PROFICIENCE_FACTOR = 0.9;
	
	//SECTION FOR TASK-SEARCH

	@SuppressWarnings("unchecked")
	public ArrayList<EvaluatedTask> findMatch(CracUser user) {
		
		TaskSearchLogger logger = TaskSearchLogger.getInstance();
		
		//LOG THE NAME
		logger.setTitlePerson(user.getName());

		Set<Competence> userCompetences = new HashSet<Competence>();
		
		for(UserCompetenceRel ucr : user.getCompetenceRelationships()){
			userCompetences.add(ucr.getCompetence());
		}
		
		ArrayList<TravelledCompetenceCollection> competenceStacks = augmentAll(userCompetences);
		makeDependantOnUser(competenceStacks, user);
		ArrayList<EvaluatedTask> evaluatedTasks = findBestTasks(competenceStacks);
		Collections.sort(evaluatedTasks);
		return evaluatedTasks;

	}
	
	private ArrayList<TravelledCompetenceCollection> augmentAll(Set<Competence> competences) {

		ArrayList<TravelledCompetenceCollection> competenceCollections = new ArrayList<TravelledCompetenceCollection>();
		
		for (Competence c : competences) {	
			HashMap<Long, TravelledCompetence> cMap = competenceAugmenter.augment(c, CRITERIA);
			competenceCollections.add(new TravelledCompetenceCollection(c.getId(), cMap));
		}
		
		return competenceCollections;

	}

	private void makeDependantOnUser(ArrayList<TravelledCompetenceCollection> competenceCollections, CracUser user) {

		considerProficiency(competenceCollections, user);
		considerUserRelationships(competenceCollections, user);

	}

	//TODO implement a function, that uses relationship values of a group to influence the value
	private void considerUserRelationships(ArrayList<TravelledCompetenceCollection> competenceCollections, CracUser user) {
		
	}
	
	private void considerProficiency(ArrayList<TravelledCompetenceCollection> competenceCollections, CracUser user) {

		for (TravelledCompetenceCollection collection : competenceCollections) {
			double proficiencyValue = userCompetenceRelDAO
					.findByUserAndCompetence(user,
							collection.getStackedCompetences().get(collection.getMainId()).getCompetence())
					.getProficiencyValue();
			for (Entry<Long, TravelledCompetence> entry : collection.getStackedCompetences().entrySet()) {
				entry.getValue().setTravelled(entry.getValue().getTravelled() * PROFICIENCE_FACTOR * proficiencyValue);
			}
		}

	}
	
	private ArrayList<EvaluatedTask> findBestTasks(ArrayList<TravelledCompetenceCollection> competenceStacks){
		ArrayList<EvaluatedTask> evaluatedTasks = new ArrayList<EvaluatedTask>();
		
		for(Task task : taskDAO.findAll()){
			TaskSearchLogger logger = TaskSearchLogger.getInstance();
			logger.setTitleTask(task.getName());
			double comparationValue = compareTaskWithUser(competenceStacks, task.getNeededCompetences());
			if(comparationValue > 0){
				evaluatedTasks.add(new EvaluatedTask(task, comparationValue));
			}
		}
		
		return evaluatedTasks;
	}
	
	private double compareTaskWithUser(ArrayList<TravelledCompetenceCollection> competenceStacks, Set<Competence> singleCompetences){
		double completeValue = 0;
		double rowCount = 1;
		TaskSearchLogger logger = TaskSearchLogger.getInstance();
		TaskSearchLogger.emptyInstance();
		for(Competence taskC : singleCompetences){
			double rowValue = 0;
			
			//DATA GETS LOGGED
			logger.addColumnTitle(taskC.getName());
		
			for(TravelledCompetenceCollection userStack : competenceStacks){
				double additionalValue = compareCompetenceWithAugmented(taskC, userStack);
				rowValue += additionalValue;
				
				//DATA GETS LOGGED		
				logger.addRowTitle(userStack.getStackedCompetences().get(userStack.getMainId()).getCompetence().getName());
				logger.addValue(additionalValue, (int) rowCount - 1);
			}
			completeValue += rowValue;
			rowCount++;
			
		}
		//DATA GETS PRINTED
		logger.print();
		
		return completeValue/rowCount;
	}
	
	private double compareCompetenceWithAugmented(Competence taskC, TravelledCompetenceCollection userCStack){
		
		HashMap<Long, TravelledCompetence> userC = userCStack.getStackedCompetences();
		
		if(userC.containsKey(taskC.getId())){
			return userC.get(taskC.getId()).getTravelled();
		}else{
			return 0.0;
		}
		
	}
	
	//SECTION FOR USER-SEARCH
	
	@SuppressWarnings("unchecked")
	public ArrayList<EvaluatedUser> findMatch(Task task) {
		
		TaskSearchLogger logger = TaskSearchLogger.getInstance();
		
		//LOG THE NAME
		logger.setTitleTask(task.getName());

		Set<Competence> taskCompetences = task.getNeededCompetences();
		
		ArrayList<TravelledCompetenceCollection> competenceStacks = augmentAll(taskCompetences);
		//makeDependantOnTask(competenceStacks, task);
		ArrayList<EvaluatedUser> evaluatedUsers = findBestUsers(competenceStacks);
		Collections.sort(evaluatedUsers);
		return evaluatedUsers;

	}

	private ArrayList<EvaluatedUser> findBestUsers(ArrayList<TravelledCompetenceCollection> competenceStacks){
		ArrayList<EvaluatedUser> evaluatedUsers = new ArrayList<EvaluatedUser>();
		
		for(CracUser user : userDAO.findAll()){
			TaskSearchLogger logger = TaskSearchLogger.getInstance();
			logger.setTitlePerson(user.getName());
			
			Set<Competence> userCompetences = new HashSet<Competence>();
			
			for(UserCompetenceRel ucr : user.getCompetenceRelationships()){
				userCompetences.add(ucr.getCompetence());
			}
			
			double comparationValue = compareTaskWithUser(competenceStacks, userCompetences);
			if(comparationValue > 0){
				evaluatedUsers.add(new EvaluatedUser(user, comparationValue));
			}
		}
		
		return evaluatedUsers;
	}
	
}
