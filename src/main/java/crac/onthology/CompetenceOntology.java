package crac.onthology;

import java.io.InputStream;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.json.simple.JSONObject;

public class CompetenceOntology {

	public static final String NS = "https://core.crac.at";
	private OntModel model;
	
	public CompetenceOntology() {
		model = ModelFactory.createOntologyModel();
		InputStream is = FileManager.get().open("./resources/competencies.owl");
		model.read(is, NS);
	}
	
	public void addBasicCompetency(String compentenceId){
		OntClass compentence = model.getOntClass(NS + "#Competency");
		compentence.createIndividual(NS + "#" + compentenceId);
	}
	
	public void addSubCompentency(String newCompentenceId, String superCompentenceId){
		
	}
	
	public void print() {
		model.write(System.out, "RDF/XML");		
	}

}
