package crac.module.matching.helpers;

import java.util.ArrayList;

import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import crac.module.storage.CompetenceStorage;
import lombok.Data;

/**
 * A class that holds multiple AugmentedSimpleCompetence-objects and performs actions on them
 * It contains a main-competence and all competences related to it (augmented)
 * @author David Hondl
 *
 */
@Data
public class AugmentedSimpleCompetenceCollection {

	private AugmentedSimpleCompetence main;
	private ArrayList<AugmentedSimpleCompetence> augmented;

	public AugmentedSimpleCompetenceCollection(Competence main, CompetenceStorage cs) {
		this.main = new AugmentedSimpleCompetence(cs.getCompetence(main.getId()));
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

	/**
	 * This method takes in another collection and compares how their main-competences are related to each other
	 * @param other
	 * @return double
	 */
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

	/**
	 * This method takes in another AugmentedSimpleCompetence and compares how it is related to the augmented-competences of this collection
	 * @param other
	 * @return double
	 */
	public double compare(AugmentedSimpleCompetence other){
		for(AugmentedSimpleCompetence aut : this.augmented){
			if(aut.getComp().getId() == other.getComp().getId()){
				return aut.getSimilarity();
			}
		}
		return 0.0;
	}
	
	/**
	 * This method evaluates if given AugmentedSimpleCompetence is part of the augmented-competences
	 * @param c
	 * @return boolean
	 */
	public boolean containsComp(AugmentedSimpleCompetence c) {
		for (AugmentedSimpleCompetence a : augmented) {
			if (a.getComp() == c.getComp()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds given SimpleCompetence to the collection or updates an existing competence if it equals the given one
	 * Returns the newly created or updated AugmentedSimpleCompetence
	 * @param c
	 * @return AugmentedSimpleCompetence
	 */
	public AugmentedSimpleCompetence loadOrCreate(SimpleCompetence c) {
		for (AugmentedSimpleCompetence a : augmented) {
			if (a.getComp() == c) {
				return a;
			}
		}
		return new AugmentedSimpleCompetence(c);
	}

	/**
	 * Adds target AugmentedSimpleCompetence to the augmented competences
	 * @param c
	 */
	public void addCompetence(AugmentedSimpleCompetence c) {
		this.augmented.add(c);
	}

	/**
	 * Creates a AugmentedSimpleCompetence from given SimpleCompetence and adds it to the augmented competences
	 * @param c
	 */
	public void addCompetence(SimpleCompetence c) {
		augmented.add(new AugmentedSimpleCompetence(c));
	}

	/**
	 * Loads the concrete competences from the database, based on the SimpleCompetences
	 * @param competenceDAO
	 */
	public void loadCompetences(CompetenceDAO competenceDAO) {
		main.loadConcreteCompetence(competenceDAO);
		for (AugmentedSimpleCompetence ac : augmented) {
			ac.loadConcreteCompetence(competenceDAO);
		}
	}

	/**
	 * Prints the collection
	 */
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
