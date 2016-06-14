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

	public static final String NS = "http://www.fhhagenberg.at/crac/compentencies";
	private OntModel model;
	private JSONConverter converter = new JSONConverter();
	
	public CompetenceOntology() {
		model = ModelFactory.createOntologyModel();
		InputStream is = FileManager.get().open("./resources/competencies.owl");
		model.read(is, NS);
	}
	
	public void addBasicCompetency(String competencyId){
		OntClass compentency = model.getOntClass(NS + "#Competency");
		compentency.createIndividual(NS + "#" + competencyId);
	}
	
	public void addSubCompentency(String newCompentencyId, String superCompentencyId){
		
	}
	
	public void addCompetencyToPerson(String personId, String competencyId, int profLevel){
		Individual person = model.getIndividual(NS + "#"+personId);
		Individual competence = model.getIndividual(NS+"#"+ competencyId);
		ObjectProperty isCompetency = model.getObjectProperty(NS + "#isCompetency");
		ObjectProperty hasCompetency = model.getObjectProperty(NS + "#hasCompetency");
				
		//createBlank Node
		Resource blankNode = model.createResource();
		DatatypeProperty level = model.getDatatypeProperty(NS + "#level");
		blankNode.addProperty(level, Integer.toString(profLevel));
		blankNode.addProperty(isCompetency, competence);
		
		person.addProperty(hasCompetency, blankNode);		
		
		JSONObject jsonPers = converter.convertIndividual(person,model);
	}



	public void print() {
		model.write(System.out, "RDF/XML");		
	}

}
