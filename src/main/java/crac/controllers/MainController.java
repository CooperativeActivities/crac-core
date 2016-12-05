package crac.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.competenceGraph.CompetenceStorage;
import crac.competenceGraph.SimpleCompetence;
import crac.competenceGraph.AugmenterUnit;
import crac.competenceGraph.SimpleCompetenceRelation;
import crac.daos.CompetenceDAO;
import crac.daos.CompetencePermissionTypeDAO;
import crac.daos.CompetenceRelationshipDAO;
import crac.daos.CompetenceRelationshipTypeDAO;
import crac.daos.CompetenceTaskRelDAO;
import crac.daos.CracUserDAO;
import crac.daos.RepetitionDateDAO;
import crac.daos.RoleDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Role;
import crac.models.Task;
import crac.relationmodels.CompetencePermissionType;
import crac.relationmodels.CompetenceRelationship;
import crac.relationmodels.CompetenceRelationshipType;
import crac.relationmodels.CompetenceTaskRel;
import crac.relationmodels.UserCompetenceRel;
import crac.utility.CompetenceAugmenter;
import crac.utility.ElasticConnector;
import crac.utility.JSonResponseHelper;
import crac.utilityModels.RepetitionDate;
import crac.utilityModels.TravelledCompetence;

/**
 * The main-controller used for hello world and testing
 */
@Controller
public class MainController {
	
	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private RepetitionDateDAO repetitionDateDAO;

	@Autowired
	private CompetenceTaskRelDAO competenceTaskRelDAO;
	
	@Autowired
	private TaskDAO taskDAO;
	
	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;
	
	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;
	
	@Autowired
	private CompetenceAugmenter competenceAugmenter;
	
	@Value("${crac.elastic.url}")
    private String url;
	
	@Value("${crac.elastic.port}")
    private int port;
	
	@Value("${crac.boot.enable}")
    private boolean bootEnabled;
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/sync")
	@ResponseBody
	public ResponseEntity<String> sync() {
		System.out.println(CompetenceStorage.isSynced());
		CompetenceStorage.sync(competenceDAO, competenceRelationshipDAO);
		for(SimpleCompetence c : CompetenceStorage.getCompetences().values()){
			System.out.println("id: "+c.getId());
			for(SimpleCompetenceRelation scr : c.getRelations()){
				System.out.println("related: id: "+scr.getRelated().getId()+" distance: "+scr.getDistance());
			}
		}
		System.out.println(CompetenceStorage.isSynced());
		return JSonResponseHelper.successFullAction("Competences have been synchronized");
	}
	
	@RequestMapping("/test/{competence_id}")
	@ResponseBody
	public ResponseEntity<String> test(@PathVariable(value = "competence_id") Long competenceId) {
		
		CompetenceStorage.sync(competenceDAO, competenceRelationshipDAO);
		
		AugmenterUnit.augment(competenceDAO.findOne(competenceId), competenceDAO);
		
		return JSonResponseHelper.successFullAction("called");
	}

	@RequestMapping("/testMe/{competence_id}/{distance}/go")
	@ResponseBody
	public ResponseEntity<String> indexWord(@PathVariable(value = "distance") Double distance, @PathVariable(value = "competence_id") Long competenceId) {
		
		HashMap<Long, TravelledCompetence> relatedCompetences = competenceAugmenter.augment(competenceDAO.findOne(competenceId), distance);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(relatedCompetences));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
		
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/boot")
	@ResponseBody
	public ResponseEntity<String> boot() {
				
		if(!bootEnabled){
			return JSonResponseHelper.bootOff();
		}
		
		if(userDAO.findByName("Webmaster") != null){
			return JSonResponseHelper.alreadyBooted();
		}
		
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		CracUser myUser = userDAO.findByName(userDetails.getName());
		
		//Add roles
		
		Role userRole = new Role();
		userRole.setName("USER");
		roleDAO.save(userRole);
		
		Role editorRole = new Role();
		editorRole.setName("EDITOR");
		roleDAO.save(editorRole);
		
		//Add relationship types
		
		CompetenceRelationshipType t1 = new CompetenceRelationshipType(); 
		t1.setDescription("Basic type SMALL");
		t1.setDistanceVal(0.9);
		t1.setName("small");
		
		competenceRelationshipTypeDAO.save(t1);
		
		CompetenceRelationshipType t2 = new CompetenceRelationshipType(); 
		t2.setDescription("Competences are synonym to each other");
		t2.setDistanceVal(1.0);
		t2.setName("isSynonym");
		
		competenceRelationshipTypeDAO.save(t2);
		
		CompetenceRelationshipType t3 = new CompetenceRelationshipType(); 
		t3.setDescription("Competences are closely related to each other");
		t3.setDistanceVal(0.9);
		t3.setName("isSimilar");
		
		competenceRelationshipTypeDAO.save(t3);
		
		//Add competences
		
		CompetencePermissionType cPermType = new CompetencePermissionType();
		cPermType.setDescription("can be added by oneself, free of restrictions");
		cPermType.setName("restriction free");
		cPermType.setSelf(true);
		
		competencePermissionTypeDAO.save(cPermType);
		
		Competence basicHumanSkills = new Competence();
		basicHumanSkills.setCreator(myUser);
		basicHumanSkills.setDescription("The majority of people is able to do these things.");
		basicHumanSkills.setName("basic human skills");
		basicHumanSkills.setPermissionType(cPermType);
		
		Competence breathing = new Competence();
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");
		breathing.setPermissionType(cPermType);

		CompetenceRelationship basic_breathing = new CompetenceRelationship();
		basic_breathing.setCompetence1(basicHumanSkills);
		basic_breathing.setCompetence2(breathing);
		basic_breathing.setType(competenceRelationshipTypeDAO.findOne((long) 1));

		Competence walking = new Competence();
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");
		walking.setPermissionType(cPermType);

		CompetenceRelationship basic_walking = new CompetenceRelationship();
		basic_walking.setCompetence1(basicHumanSkills);
		basic_walking.setCompetence2(walking);
		basic_walking.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence swimming = new Competence();
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");
		swimming.setPermissionType(cPermType);

		CompetenceRelationship basic_swimming = new CompetenceRelationship();
		basic_swimming.setCompetence1(basicHumanSkills);
		basic_swimming.setCompetence2(swimming);
		basic_swimming.setType(competenceRelationshipTypeDAO.findOne((long) 3));
		
		Competence programming = new Competence();
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");
		programming.setPermissionType(cPermType);

		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");
		javascriptProgramming.setPermissionType(cPermType);

		CompetenceRelationship programming_javascriptProgramming = new CompetenceRelationship();
		programming_javascriptProgramming.setCompetence1(programming);
		programming_javascriptProgramming.setCompetence2(javascriptProgramming);
		programming_javascriptProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence phpProgramming = new Competence();
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");
		phpProgramming.setPermissionType(cPermType);

		CompetenceRelationship programming_phpProgramming = new CompetenceRelationship();
		programming_phpProgramming.setCompetence1(programming);
		programming_phpProgramming.setCompetence2(phpProgramming);
		programming_phpProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		competenceDAO.save(basicHumanSkills);
		competenceDAO.save(breathing);
		competenceDAO.save(walking);
		competenceDAO.save(swimming);
		competenceDAO.save(programming);
		competenceDAO.save(javascriptProgramming);
		competenceDAO.save(phpProgramming);
		
		competenceRelationshipDAO.save(basic_breathing);
		competenceRelationshipDAO.save(basic_walking);
		competenceRelationshipDAO.save(basic_swimming);
		competenceRelationshipDAO.save(programming_javascriptProgramming);
		competenceRelationshipDAO.save(programming_phpProgramming);
		
		//Add projects
		Calendar time = new GregorianCalendar();
		
		Task waterFlowers = new Task();
		waterFlowers.setName("Water the flowers");
		waterFlowers.setDescription("All about watering the different flowers in the garden.");
		waterFlowers.setLocation("my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		waterFlowers.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		waterFlowers.setEndTime(time);
		waterFlowers.setCreator(myUser);
				
		//Add tasks
		
		Task waterRoses = new Task();
		waterRoses.setName("Water the roses");
		waterRoses.setDescription("Water the roses on the westside of the garden.");
		waterRoses.setLocation("my garden");
		time.set(2016, 9, 10, 16, 30, 00);
		waterRoses.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		waterRoses.setEndTime(time);
		waterRoses.setUrgency(5);
		waterRoses.setAmountOfVolunteers(2);		
		waterRoses.setCreator(myUser);
		waterRoses.setSuperTask(waterFlowers);
		
		Task waterLilies = new Task();
		waterLilies.setName("Water the lillies");
		waterLilies.setDescription("Water the lilies on the eastside of the garden.");
		waterLilies.setLocation("my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		waterLilies.setStartTime(time);
		time.set(2016, 9, 10, 16, 00, 00);
		waterLilies.setEndTime(time);
		waterLilies.setUrgency(2);
		waterLilies.setAmountOfVolunteers(1);
		waterLilies.setCreator(myUser);
		waterLilies.setSuperTask(waterFlowers);
		
		Task programWateringTool = new Task();
		programWateringTool.setName("Program a watering tool");
		programWateringTool.setDescription("Program a web-tool that makes watering flowers easier.");
		programWateringTool.setLocation("a desk in my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		programWateringTool.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		programWateringTool.setEndTime(time);
		programWateringTool.setUrgency(10);
		programWateringTool.setAmountOfVolunteers(1);
		
		programWateringTool.setCreator(myUser);
		programWateringTool.setSuperTask(waterFlowers);
		
		waterFlowers.setChildTasks(new HashSet<Task>());
		waterFlowers.getChildTasks().add(waterRoses);
		waterFlowers.getChildTasks().add(waterLilies);
		waterFlowers.getChildTasks().add(programWateringTool);
		
		taskDAO.save(waterFlowers);
		taskDAO.save(waterRoses);
		taskDAO.save(waterLilies);
		taskDAO.save(programWateringTool);
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, waterRoses, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, waterRoses, 10, 10, false));
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, waterLilies, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, waterLilies, 10, 10, false));
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, programWateringTool, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, programWateringTool, 100, 100, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(programming, programWateringTool, 10, 10, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(phpProgramming, programWateringTool, 10, 10, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(javascriptProgramming, programWateringTool, 10, 10, false));

		
		ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");

		eSConnTask.indexOrUpdate(""+waterFlowers.getId(),waterFlowers);
		
		for(Task t : waterFlowers.getChildTasks()){
			eSConnTask.indexOrUpdate(""+t.getId(),t);
		}

		//Add users
		
		CracUser Webmaster = new CracUser();
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		
		Webmaster.setName("Webmaster");
		Webmaster.setFirstName("Max");
		Webmaster.setLastName("Mustermann");
		Webmaster.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, breathing, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, walking, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, swimming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, programming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, phpProgramming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, javascriptProgramming, 50, 1));
		Webmaster.setPassword(bcryptEncoder.encode("noOneKnowsThisPassword!1!1"));
		//Webmaster.setRole(Role.USER);
		Webmaster.setPhone("0987656789098");
		Webmaster.setEmail("Webmaster@internet.at");
		
		CracUser AverageHuman = new CracUser();
		
		AverageHuman.setName("AverageHuman");
		AverageHuman.setFirstName("Hans");
		AverageHuman.setLastName("Musterhans");
		AverageHuman.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, breathing, 50, 1));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, walking, 50, 1));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, swimming, 50, 1));
		AverageHuman.setPassword(bcryptEncoder.encode("noOneKnowsThisPasswordAnyway!1!1"));
		//AverageHuman.setRole(Role.USER);
		AverageHuman.setPhone("35678987654");
		AverageHuman.setEmail("AverageHuman@internet.at");
		userDAO.save(Webmaster);
		userDAO.save(AverageHuman);
		
		RepetitionDate date1 = new RepetitionDate(0, 0, 0, 0, 10);
		repetitionDateDAO.save(date1);
		
		CompetenceStorage.sync(competenceDAO, competenceRelationshipDAO);
		
		return JSonResponseHelper.bootSuccess();
	}

}
