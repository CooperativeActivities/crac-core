package crac.competenceGraph;

import java.util.ArrayList;

import crac.daos.CompetenceDAO;
import crac.models.Competence;

public class AugmentedSimpleCompetenceCollection {

	private AugmentedSimpleCompetence main;
	private ArrayList<AugmentedSimpleCompetence> augmented;

	public AugmentedSimpleCompetenceCollection(Competence main) {
		this.main = new AugmentedSimpleCompetence(CompetenceStorage.getCompetence(main.getId()));
		this.main.setStepsDone(0);
		this.main.setTravelledDistance(1);
		this.augmented = new ArrayList<AugmentedSimpleCompetence>();
	}
	
	public AugmentedSimpleCompetenceCollection(SimpleCompetence main) {
		this.main = new AugmentedSimpleCompetence(main);
		this.main.setStepsDone(0);
		this.main.setTravelledDistance(1);
		this.augmented = new ArrayList<AugmentedSimpleCompetence>();
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
		System.out.println("ID: " + main.getComp().getId() + " | distance: " + main.getTravelledDistance() + " steps: "
				+ main.getStepsDone());
		if (loaded) {
			System.out.println("concrete name: " + main.getConcreteComp().getName());
		}
		System.out.println("---------------------------");
		System.out.println("Augmented Competences");
		for (AugmentedSimpleCompetence a : augmented) {
			System.out.println("ID: " + a.getComp().getId() + " | distance: " + a.getTravelledDistance() + " steps: "
					+ a.getStepsDone());
			if (loaded) {
				System.out.println("concrete name: " + a.getConcreteComp().getName());
			}
		}
		System.out.println("_____________________________");

	}

}
