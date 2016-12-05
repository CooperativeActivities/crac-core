package crac.competenceGraph;

import java.util.ArrayList;

import crac.daos.CompetenceDAO;
import crac.models.Competence;

public class CompetenceCollection {
	private Competence main;
	private ArrayList<Competence> augmented;
	
	public CompetenceCollection(Competence main) {
		this.main = main;
	}
	
	public Competence getMain() {
		return main;
	}
	
	public void setMain(Competence main) {
		this.main = main;
	}
	
	public ArrayList<Competence> getAugmented() {
		return augmented;
	}
	
	public void setAugmented(ArrayList<Competence> augmented) {
		this.augmented = augmented;
	}
	
	public void addCompetence(Competence c){
		augmented.add(c);
	}
	
}
