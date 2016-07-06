package crac.models;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import crac.daos.CompetenceDAO;
import crac.daos.CompetenceRelationshipDAO;
import crac.elastic.ElasticCompetence;

@Service
public class CompetenceAugmenter {

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private CompetenceRelationshipDAO relationDAO;

	public void augmentWithNumber(Competence c, int originalDistance, int numberOfSteps,
			Set<ElasticCompetence> relatedCompetences) {

		System.out.println("Augmenting |" + c.getName() + "| with |" + numberOfSteps + "| steps to go!");

		relatedCompetences.add(
				new ElasticCompetence(c.getId(), c.getName(), c.getDescription(), originalDistance - numberOfSteps));

		List<CompetenceRelationship> list1 = relationDAO.findByCompetence1(c);

		List<CompetenceRelationship> list2 = relationDAO.findByCompetence2(c);

		if (list1 != null) {
			for (CompetenceRelationship cr : list1) {
				Competence targetC = cr.getCompetence2();

				System.out.println("Found competence: " + targetC.getName());
				if (numberOfSteps - 1 >= 0) {

					boolean allow = true;

					ElasticCompetence newElc = new ElasticCompetence(targetC.getId(), targetC.getName(),
							targetC.getDescription(), originalDistance - numberOfSteps);

					for (ElasticCompetence elc : relatedCompetences) {
						if (elc.getId() == newElc.getId() && elc.getTravelled() > newElc.getTravelled()) {
							elc.setBadPath(true);
						} else if (elc.getId() == newElc.getId() && !(elc.getTravelled() > newElc.getTravelled())) {
							allow = false;
							System.out.println("WORSE PATH");
						}
					}
					if (allow) {
						augmentWithNumber(targetC, originalDistance, numberOfSteps - 1, relatedCompetences);
					}
				} else {
					System.out.println("Distance too big! Discard: " + targetC.getName());
				}

			}
		} else {
			System.out.println("List1 of |" + c.getName() + "| is null!");
		}

		if (list2 != null) {
			for (CompetenceRelationship cr : list2) {
				Competence targetC = cr.getCompetence1();

				System.out.println("Found competence: " + targetC.getName());
				if (numberOfSteps - 1 >= 0) {

					boolean allow = true;

					ElasticCompetence newElc = new ElasticCompetence(targetC.getId(), targetC.getName(),
							targetC.getDescription(), originalDistance - numberOfSteps);

					for (ElasticCompetence elc : relatedCompetences) {
						if (elc.getId() == newElc.getId() && elc.getTravelled() > newElc.getTravelled()) {
							elc.setBadPath(true);
						} else if (elc.getId() == newElc.getId() && !(elc.getTravelled() > newElc.getTravelled())) {
							allow = false;
							System.out.println("WORSE PATH");
						}
					}
					if (allow) {
						augmentWithNumber(targetC, originalDistance, numberOfSteps - 1, relatedCompetences);
					}
				} else {
					System.out.println("Distance too big! Discard: " + targetC.getName());
				}

			}
		} else {
			System.out.println("List2 |" + c.getName() + "| is null!");
		}

	}

	public void augmentWithDistance(Competence c, int originalDistance, int distanceToGo,
			Set<ElasticCompetence> relatedCompetences) {

		System.out.println("Augmenting |" + c.getName() + "| with |" + distanceToGo + "| units to go!");
		relatedCompetences.add(new ElasticCompetence(c.getId(), c.getName(), c.getDescription(), originalDistance - distanceToGo));
		List<CompetenceRelationship> list1 = relationDAO.findByCompetence1(c);
		List<CompetenceRelationship> list2 = relationDAO.findByCompetence2(c);
		if (list1 != null) {
			for (CompetenceRelationship cr : list1) {
				Competence targetC = cr.getCompetence2();
				calcCompIntern(targetC, originalDistance, distanceToGo, cr, relatedCompetences);
			}
		} else {
			System.out.println("List1 of |" + c.getName() + "| is null!");
		}
		if (list2 != null) {
			for (CompetenceRelationship cr : list2) {
				Competence targetC = cr.getCompetence1();
				calcCompIntern(targetC, originalDistance, distanceToGo, cr, relatedCompetences);
			}
		} else {
			System.out.println("List2 |" + c.getName() + "| is null!");
		}
	}
	
	private void calcCompIntern(Competence targetC, int originalDistance, int distanceToGo, CompetenceRelationship cr, Set<ElasticCompetence> relatedCompetences){
		System.out.println("Found competence: " + targetC.getName());
		if (distanceToGo - cr.getType().getDistanceVal() >= 0) {

			boolean allow = true;

			ElasticCompetence newElc = new ElasticCompetence(targetC.getId(), targetC.getName(),
					targetC.getDescription(), originalDistance - distanceToGo + cr.getType().getDistanceVal());

			for (ElasticCompetence elc : relatedCompetences) {
				if (elc.getId() == newElc.getId()) {
					if (elc.getTravelled() > newElc.getTravelled()) {
						System.out.println("id: " + elc.getId() +" name: " + elc.getName() + " old: " + elc.getTravelled() + " new: "
								+ newElc.getTravelled());
						elc.setBadPath(true);
					} else {
						allow = false;
						System.out.println("DISSALOWED id: " + elc.getId() +" name: " + elc.getName());
					}
				}
			}
			if (allow) {
				augmentWithDistance(targetC, originalDistance, distanceToGo - cr.getType().getDistanceVal(),
						relatedCompetences);
			}
		} else {
			System.out.println("Distance too big! Discard: " + targetC.getName());
		}
	}

}
