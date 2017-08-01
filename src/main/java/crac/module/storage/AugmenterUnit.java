package crac.module.storage;

import java.util.List;

import crac.models.db.daos.CompetenceDAO;
import crac.models.storage.AugmentedSimpleCompetence;
import crac.models.storage.AugmentedSimpleCompetenceCollection;
import crac.models.storage.SimpleCompetence;
import crac.models.storage.SimpleCompetenceRelation;

public class AugmenterUnit {
	
	private CompetenceStorage storage;
	
	public AugmenterUnit(CompetenceStorage storage){
		this.storage = storage;
	}

	public AugmentedSimpleCompetenceCollection augment(SimpleCompetence c) {

		AugmentedSimpleCompetenceCollection competences = new AugmentedSimpleCompetenceCollection(c);

		augmentIntern(competences, competences.getMain());
		
		competences.loadCompetences(storage.getCompetenceDAO());

		competences.print();
		
		return competences;

	}

	private void augmentIntern(AugmentedSimpleCompetenceCollection collection,
			AugmentedSimpleCompetence target) {

		if (target.getStepsDone() <= 5 && target.getSimilarity() >= 0.2) {
			System.out.println("Target with ID "+target.getComp().getId()+" and distance "+target.getSimilarity()+" added!");
			collection.addCompetence(target);
			callChildren(collection, target);
		}

	}

	public void callChildren(AugmentedSimpleCompetenceCollection collection, AugmentedSimpleCompetence parent) {
		List<SimpleCompetenceRelation> rels = parent.getComp().getRelations();
		if (rels != null) {
			for (SimpleCompetenceRelation sc : rels) {
				AugmentedSimpleCompetence target = collection.loadOrCreate(sc.getRelated());
				
				target.setPaths(target.getPaths() + 1);
				
				if(target.getSimilarity() > 0){
					if(parent.getSimilarity() * sc.getDistance() > target.getSimilarity() ){
						System.out.println("found and updated: new "+parent.getSimilarity() * sc.getDistance()+" old: "+target.getSimilarity());
						updateValues(target, parent, sc.getDistance());
						augmentIntern(collection, target);
					}else{
						System.out.println("found and worse or equal");
					}
				}else{
					System.out.println("new competence found");
					updateValues(target, parent, sc.getDistance());
					augmentIntern(collection, target);
				}
				
			}
		}
	}

	private void updateValues(AugmentedSimpleCompetence target, AugmentedSimpleCompetence parent,
			double distance) {

		target.setStepsDone(parent.getStepsDone() + 1);
		target.setSimilarity((double) Math.round((parent.getSimilarity() * distance) * 100) / 100);

	}

}
