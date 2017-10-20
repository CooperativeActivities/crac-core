package crac.module.storage;

import java.util.List;

import crac.models.db.daos.CompetenceDAO;
import crac.module.matching.helpers.AugmentedSimpleCompetence;
import crac.module.matching.helpers.AugmentedSimpleCompetenceCollection;
import crac.module.matching.helpers.SimpleCompetence;
import crac.module.matching.helpers.SimpleCompetenceRelation;

/**
 * Class that handles the graph-traversal-process and returns the result (the agumented competences)
 * @author David Hondl
 *
 */
public class AugmenterUnit {

	private CompetenceStorage storage;

	public AugmenterUnit(CompetenceStorage storage) {
		this.storage = storage;
	}

	/**
	 * Augment a single SimpleCompetence and return a AugmentedSimpleCompetenceCollection with it as mainId
	 * @param c
	 * @return AugmentedSimpleCompetenceCollection
	 */
	public AugmentedSimpleCompetenceCollection augment(SimpleCompetence c) {

		AugmentedSimpleCompetenceCollection competences = new AugmentedSimpleCompetenceCollection(c);

		augmentIntern(competences, competences.getMain());

		competences.loadCompetences(storage.getCompetenceDAO());

		if (storage.isPrint()) {
			competences.print();
		}
		return competences;

	}

	/**
	 * Recursive method, that augments the SimpleCompetence
	 * @param collection
	 * @param target
	 */
	private void augmentIntern(AugmentedSimpleCompetenceCollection collection, AugmentedSimpleCompetence target) {

		if (target.getStepsDone() <= storage.getMaxSteps() && target.getSimilarity() >= storage.getMinSimilarity()) {
			/*System.out.println("Target with ID " + target.getComp().getId() + " and distance " + target.getSimilarity()
					+ " added!");*/
			collection.addCompetence(target);
			callChildren(collection, target);
		}

	}

	/**
	 * Method, that calls the augmentInter()-method for all related competences, as long as the rules for further traversal are not violated
	 * @param collection
	 * @param parent
	 */
	public void callChildren(AugmentedSimpleCompetenceCollection collection, AugmentedSimpleCompetence parent) {
		List<SimpleCompetenceRelation> rels = parent.getComp().getRelations();
		if (rels != null) {
			for (SimpleCompetenceRelation sc : rels) {
				AugmentedSimpleCompetence target = collection.loadOrCreate(sc.getRelated());

				target.setPaths(target.getPaths() + 1);

				if (target.getSimilarity() > 0) {
					if (parent.getSimilarity() * sc.getDistance() > target.getSimilarity()) {
						/*System.out.println("found and updated: new " + parent.getSimilarity() * sc.getDistance()
								+ " old: " + target.getSimilarity());*/
						updateValues(target, parent, sc.getDistance());
						augmentIntern(collection, target);
					} else {
						/*System.out.println("found and worse or equal");*/
					}
				} else {
					/*System.out.println("new competence found");*/
					updateValues(target, parent, sc.getDistance());
					augmentIntern(collection, target);
				}

			}
		}
	}

	/**
	 * Method that updates the values for traversed competences
	 * @param target
	 * @param parent
	 * @param distance
	 */
	private void updateValues(AugmentedSimpleCompetence target, AugmentedSimpleCompetence parent, double distance) {

		target.setStepsDone(parent.getStepsDone() + 1);
		target.setSimilarity((double) Math.round((parent.getSimilarity() * distance) * 100) / 100);

	}

}
