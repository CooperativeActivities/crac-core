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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
import crac.decider.core.Decider;
import crac.decider.core.UserFilterParameters;
import crac.decider.filter.ImportancyLevelFilter;
import crac.decider.filter.LikeLevelFilter;
import crac.decider.filter.ProficiencyLevelFilter;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.TaskMatchingWorker;
import crac.decider.workers.config.GlobalMatrixFilterConfig;
import crac.enums.TaskState;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Role;
import crac.models.Task;
import crac.models.relation.CompetencePermissionType;
import crac.models.relation.CompetenceRelationship;
import crac.models.relation.CompetenceRelationshipType;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserCompetenceRel;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.storage.SimpleCompetence;
import crac.models.storage.SimpleCompetenceRelation;
import crac.models.utility.RepetitionDate;
import crac.models.utility.TravelledCompetence;
import crac.storage.AugmenterUnit;
import crac.storage.CompetenceStorage;
import crac.utility.ElasticConnector;
import crac.utility.JSonResponseHelper;

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
	
	@Value("${crac.elastic.url}")
    private String url;
	
	@Value("${crac.elastic.port}")
    private int port;
	
	@Value("${crac.boot.enable}")
    private boolean bootEnabled;
		
	@RequestMapping("/test/{competence_id}")
	@ResponseBody
	public ResponseEntity<String> test(@PathVariable(value = "competence_id") Long competenceId) {
		
		CompetenceStorage.getCollection(competenceId).print();
		
		return JSonResponseHelper.successFullAction("called");
	}
	
	@RequestMapping("/filters")
	@ResponseBody
	public ResponseEntity<String> filters() {
		
		CompetenceStorage.synchronize(competenceDAO, competenceRelationshipDAO);

		
		System.out.println("-----------------------");
		System.out.println("Adding Filters!");
		GlobalMatrixFilterConfig.clearFilters();
		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
		System.out.println("-----------------------");
		
		return JSonResponseHelper.successFullAction("standard filters added!");
		
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
		t1.setDescription("Competences are synonym to each other");
		t1.setDistanceVal(1.0);
		t1.setName("synonym");
		
		competenceRelationshipTypeDAO.save(t1);
		
		CompetenceRelationshipType t2 = new CompetenceRelationshipType(); 
		t2.setDescription("Competences are similar to each other");
		t2.setDistanceVal(0.8);
		t2.setName("similar");
		
		competenceRelationshipTypeDAO.save(t2);
		
		CompetenceRelationshipType t3 = new CompetenceRelationshipType(); 
		t3.setDescription("Competences are closely related to each other");
		t3.setDistanceVal(0.6);
		t3.setName("closeRelated");
		
		competenceRelationshipTypeDAO.save(t3);
		
		CompetenceRelationshipType t4 = new CompetenceRelationshipType(); 
		t4.setDescription("Competences are related to each other");
		t4.setDistanceVal(0.4);
		t4.setName("related");
		
		competenceRelationshipTypeDAO.save(t4);

		CompetenceRelationshipType t5 = new CompetenceRelationshipType(); 
		t5.setDescription("Competences are far related to each other");
		t5.setDistanceVal(0.2);
		t5.setName("farRelated");
		
		competenceRelationshipTypeDAO.save(t5);

		
		//Add competences
		
		CompetencePermissionType cPermType1 = new CompetencePermissionType();
		cPermType1.setDescription("Can be added by oneself, free of restrictions.");
		cPermType1.setName("SELF");
		cPermType1.setSelf(true);
		
		CompetencePermissionType cPermType2 = new CompetencePermissionType();
		cPermType2.setDescription("Can be added only by a user with the permission to add it.");
		cPermType2.setName("EXTERNAL");
		cPermType2.setSelf(false);
		
		CompetencePermissionType cPermType3 = new CompetencePermissionType();
		cPermType3.setDescription("Can be acqired by solving tasks containing related competences as a reward.");
		cPermType3.setName("ACQUIRED");
		cPermType3.setSelf(false);
		
		CompetencePermissionType cPermType4 = new CompetencePermissionType();
		cPermType4.setDescription("Are automatically acquired, when enough tasks with this self-acquirably competence are done.");
		cPermType4.setName("ACQUIRED");
		cPermType4.setSelf(true);
		
		competencePermissionTypeDAO.save(cPermType1);
		competencePermissionTypeDAO.save(cPermType2);
		competencePermissionTypeDAO.save(cPermType3);
		competencePermissionTypeDAO.save(cPermType4);
		
		Competence basicHumanSkills = new Competence();
		basicHumanSkills.setCreator(myUser);
		basicHumanSkills.setDescription("The majority of people is able to do these things.");
		basicHumanSkills.setName("basic human skills");
		basicHumanSkills.setPermissionType(cPermType1);
		
		Competence breathing = new Competence();
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");
		breathing.setPermissionType(cPermType1);

		CompetenceRelationship basic_breathing = new CompetenceRelationship();
		basic_breathing.setCompetence1(basicHumanSkills);
		basic_breathing.setCompetence2(breathing);
		basic_breathing.setType(competenceRelationshipTypeDAO.findOne((long) 1));

		Competence walking = new Competence();
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");
		walking.setPermissionType(cPermType1);

		CompetenceRelationship basic_walking = new CompetenceRelationship();
		basic_walking.setCompetence1(basicHumanSkills);
		basic_walking.setCompetence2(walking);
		basic_walking.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence swimming = new Competence();
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");
		swimming.setPermissionType(cPermType1);

		CompetenceRelationship basic_swimming = new CompetenceRelationship();
		basic_swimming.setCompetence1(basicHumanSkills);
		basic_swimming.setCompetence2(swimming);
		basic_swimming.setType(competenceRelationshipTypeDAO.findOne((long) 3));
		
		Competence programming = new Competence();
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");
		programming.setPermissionType(cPermType1);

		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");
		javascriptProgramming.setPermissionType(cPermType1);

		CompetenceRelationship programming_javascriptProgramming = new CompetenceRelationship();
		programming_javascriptProgramming.setCompetence1(programming);
		programming_javascriptProgramming.setCompetence2(javascriptProgramming);
		programming_javascriptProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence phpProgramming = new Competence();
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");
		phpProgramming.setPermissionType(cPermType1);

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
		
		Task waterFlowers = new Task();
		waterFlowers.setName("Water the flowers");
		waterFlowers.setDescription("All about watering the different flowers in the garden.");
		waterFlowers.setLocation("my garden");
		waterFlowers.setMaxAmountOfVolunteers(0);
		waterFlowers.setMinAmountOfVolunteers(10);
		waterFlowers.setCreator(myUser);
		waterFlowers.setTaskState(TaskState.PUBLISHED);
				
		//Add tasks
		
		Task waterRoses = new Task();
		waterRoses.setName("Water the roses");
		waterRoses.setDescription("Water the roses on the westside of the garden.");
		waterRoses.setLocation("my garden");
		waterRoses.setUrgency(5);
		waterRoses.setMaxAmountOfVolunteers(0);		
		waterRoses.setMinAmountOfVolunteers(3);		
		waterRoses.setCreator(myUser);
		waterRoses.setSuperTask(waterFlowers);
		waterRoses.setTaskState(TaskState.PUBLISHED);

		Task waterLilies = new Task();
		waterLilies.setName("Water the lillies");
		waterLilies.setDescription("Water the lilies on the eastside of the garden.");
		waterLilies.setLocation("my garden");
		waterLilies.setUrgency(2);
		waterLilies.setMaxAmountOfVolunteers(0);
		waterLilies.setMinAmountOfVolunteers(2);
		waterLilies.setCreator(myUser);
		waterLilies.setSuperTask(waterFlowers);
		waterLilies.setTaskState(TaskState.PUBLISHED);

		Task programWateringTool = new Task();
		programWateringTool.setName("Program a watering tool");
		programWateringTool.setDescription("Program a web-tool that makes watering flowers easier.");
		programWateringTool.setLocation("a desk in my garden");
		programWateringTool.setUrgency(10);
		programWateringTool.setMaxAmountOfVolunteers(0);
		programWateringTool.setMinAmountOfVolunteers(5);
		programWateringTool.setTaskState(TaskState.PUBLISHED);

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
		
		CracUser frontend = userDAO.findOne((long)1);
		
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, breathing, 50, 1));
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, walking, 50, 1));
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, swimming, 50, 1));
		userDAO.save(frontend);

		
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
		userDAO.save(Webmaster);
	
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
		userDAO.save(AverageHuman);
		
		addActualUsers();
		
		RepetitionDate date1 = new RepetitionDate(0, 0, 0, 0, 10);
		repetitionDateDAO.save(date1);
		
		CompetenceStorage.synchronize(competenceDAO, competenceRelationshipDAO);
		
		System.out.println("-----------------------");
		System.out.println("Adding Filters!");
		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new UserRelationFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
		System.out.println("-----------------------");

		return JSonResponseHelper.bootSuccess();
	}
	
	public void addActualUsers(){
		CracUser SchönböckADMIN = new CracUser();
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		SchönböckADMIN.setName("SchönböckADMIN");
		SchönböckADMIN.setFirstName("Johannes");
		SchönböckADMIN.setLastName("Schönböck");
		SchönböckADMIN.setPassword(bcryptEncoder.encode("default"));
		SchönböckADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchönböckADMIN.setPhone("35678987654");
		SchönböckADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckADMIN);
		
		CracUser SchönböckUSER = new CracUser();

		SchönböckUSER.setName("SchönböckUSER");
		SchönböckUSER.setFirstName("Johannes");
		SchönböckUSER.setLastName("Schönböck");
		SchönböckUSER.setPassword(bcryptEncoder.encode("default"));
		SchönböckUSER.addRole(roleDAO.findByName("USER"));
		SchönböckUSER.setPhone("35678987654");
		SchönböckUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckUSER);

		CracUser PröllADMIN = new CracUser();

		PröllADMIN.setName("PröllADMIN");
		PröllADMIN.setFirstName("Birigt");
		PröllADMIN.setLastName("Pröll");
		PröllADMIN.setPassword(bcryptEncoder.encode("default"));
		PröllADMIN.addRole(roleDAO.findByName("ADMIN"));
		PröllADMIN.setPhone("35678987654");
		PröllADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(PröllADMIN);
		
		CracUser PröllUSER = new CracUser();

		PröllUSER.setName("PröllUSER");
		PröllUSER.setFirstName("Birigt");
		PröllUSER.setLastName("Pröll");
		PröllUSER.setPassword(bcryptEncoder.encode("default"));
		PröllUSER.addRole(roleDAO.findByName("USER"));
		PröllUSER.setPhone("35678987654");
		PröllUSER.setEmail("Mustermail@internet.at");
		userDAO.save(PröllUSER);
		
		CracUser EibnerADMIN = new CracUser();

		EibnerADMIN.setName("EibnerADMIN");
		EibnerADMIN.setFirstName("Wolfgang");
		EibnerADMIN.setLastName("Eibner");
		EibnerADMIN.setPassword(bcryptEncoder.encode("default"));
		EibnerADMIN.addRole(roleDAO.findByName("ADMIN"));
		EibnerADMIN.setPhone("35678987654");
		EibnerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(EibnerADMIN);
		
		CracUser EibnerUSER = new CracUser();

		EibnerUSER.setName("EibnerUSER");
		EibnerUSER.setFirstName("Wolfgang");
		EibnerUSER.setLastName("Eibner");
		EibnerUSER.setPassword(bcryptEncoder.encode("default"));
		EibnerUSER.addRole(roleDAO.findByName("USER"));
		EibnerUSER.setPhone("35678987654");
		EibnerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(EibnerUSER);
		
	}

}
