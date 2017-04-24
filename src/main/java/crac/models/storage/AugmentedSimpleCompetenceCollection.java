package crac.models.storage;

import java.util.ArrayList;

import crac.components.storage.CompetenceStorage;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;

public class AugmentedSimpleCompetenceCollection {

	private AugmentedSimpleCompetence main;
	private ArrayList<AugmentedSimpleCompetence> augmented;

	public AugmentedSimpleCompetenceCollection(Competence main) {
		this.main = new AugmentedSimpleCompetence(CompetenceStorage.getCompetence(main.getId()));
		this.main.setStepsDone(0);
		this.main.setSimilarity(1);
		this.augmented = new ArrayList<AugmentedSimpleCompetence>();
	}
	
	public AugmentedSimpleCompetenceCollection(SimpleCompetence main) {
		this.main = new AugmentedSimpleCompetence(main);
		this.main.setStepsDone(0);
		this.main.setSimilarity(1);
		this.augmented = new ArrayList<AugmentedSimpleCompetence>();
	}

	public double compare(AugmentedSimpleCompetenceCollection other){
		if(other.getMain().getComp().getId() == this.getMain().getComp().getId()){
			return 1.0;
		}
		for(AugmentedSimpleCompetence aut : this.augmented){
			for(AugmentedSimpleCompetence auo : other.getAugmented()){
				if(auo.getComp().getId() == aut.getComp().getId()){
					double val = aut.getSimilarity() * auo.getSimilarity();
					if(val > 0.2){
						return val;
					}
				}
			}
		}
		return 0.0;
	}

	public double compare(AugmentedSimpleCompetence other){
		for(AugmentedSimpleCompetence aut : this.augmented){
			if(aut.getComp().getId() == other.getComp().getId()){
				return aut.getSimilarity();
			}
		}
		return 0.0;
	}
	
	public AugmentedSimpleCompetence getMain() {
		return main;
	}

	public void setMain(AugmentedSimpleCompetence main) {
		this.main = main;
	}

	public ArrayList<AugmentedSimpleCompetence> getAugmented() {
		return augmented;
	}

	public void setAugmented(ArrayList<AugmentedSimpleCompetence> augmented) {
		this.augmented = augmented;
	}

	public boolean containsComp(AugmentedSimpleCompetence c) {
		for (AugmentedSimpleCompetence a : augmented) {
			if (a.getComp() == c.getComp()) {
				return true;
			}
		}
		return false;
	}

	public AugmentedSimpleCompetence loadOrCreate(SimpleCompetence c) {
		for (AugmentedSimpleCompetence a : augmented) {
			if (a.getComp() == c) {
				return a;
			}
		}
		return new AugmentedSimpleCompetence(c);
	}

	public void addCompetence(AugmentedSimpleCompetence c) {
		this.augmented.add(c);
	}

	public void addCompetence(SimpleCompetence c) {
		augmented.add(new AugmentedSimpleCompetence(c));
	}

	public void loadCompetences(CompetenceDAO competenceDAO) {
		main.loadConcreteCompetence(competenceDAO);
		for (AugmentedSimpleCompetence ac : augmented) {
			ac.loadConcreteCompetence(competenceDAO);
		}
	}

	public void print() {
		boolean loaded = false;
		if (main.getConcreteComp() != null) {
			loaded = true;
		}

		System.out.println("_____________________________");
		if (loaded) {
			System.out.println("CONCRETE COMPETENCE HAS BEEN LOADED");
		}
		System.out.println("Main-Competence");
		System.out.println("ID: " + main.getComp().getId() + " | distance: " + main.getSimilarity() + " steps: "
				+ main.getStepsDone());
		if (loaded) {
			System.out.println("concrete name: " + main.getConcreteComp().getName());
		}
		System.out.println("---------------------------");
		System.out.println("Augmented Competences");
		for (AugmentedSimpleCompetence a : augmented) {
			System.out.println("ID: " + a.getComp().getId() + " | distance: " + a.getSimilarity() + " steps: "
					+ a.getStepsDone() + " paths: "+a.getPaths());
			if (loaded) {
				System.out.println("concrete name: " + a.getConcreteComp().getName());
			}
		}
		System.out.println("_____________________________");

	}

}
