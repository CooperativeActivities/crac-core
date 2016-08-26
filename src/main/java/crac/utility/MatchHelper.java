package crac.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import crac.daos.CracUserDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.Notification;
import crac.relationmodels.UserCompetenceRel;
import crac.utilityModels.TravelledCompetence;
import crac.utilityModels.TravelledCompetenceCollection;

@Service
public class MatchHelper {

	@Autowired
	CracUserDAO userDAO;

	@Autowired
	UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	CompetenceAugmenter competenceAugmenter;

	private static final double CRITERIA = 0.2;
	private static final double PROFICIENCE_FACTOR = 0.9;

	public HashMap<Long, Task> findMatch(CracUser user) {
		
		HashMap<Long, TravelledCompetence> relatedCompetences = new HashMap<Long, TravelledCompetence>();

		Set<Competence> userCompetences = new HashSet<Competence>();
		
		for(UserCompetenceRel ucr : user.getCompetenceRelationships()){
			userCompetences.add(ucr.getCompetence());
		}
		
		ArrayList<TravelledCompetenceCollection> competenceStacks = augmentAll(userCompetences);

		makeDependantOnUser(competenceStacks, user);


		return null;

	}

	public HashMap<Long, CracUser> findMatch(Task task) {

		return null;

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

}
