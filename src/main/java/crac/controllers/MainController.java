package crac.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetencePermissionTypeDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.RepetitionDateDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsTopicidMmDAO;
import crac.models.komet.daos.TxExabiscompetencesTopicDAO;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.notifier.Notification;
import crac.module.notifier.factory.NotificationFactory;
import crac.module.notifier.notifications.FriendRequest;
import crac.module.notifier.notifications.TaskInvitation;
import crac.module.storage.CompetenceStorage;
import crac.module.utility.ElasticConnector;
import crac.module.utility.JSONResponseHelper;

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
	private UserRelationshipDAO userRelationshipDAO;
	
	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;
	
	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;
	
	@Autowired
	private TxExabiscompetencesTopicDAO txExabiscompetencesTopicDAO;
	
	@Autowired
	private TxExabiscompetencesDescriptorsTopicidMmDAO txExabiscompetencesDescriptorsTopicidMmDAO;

	@Autowired
	private MatchingConfiguration matchingConfig;
	
	@Autowired
	private NotificationFactory nf;
	
	@Autowired
	private CompetenceStorage cs;
		
	@Autowired
	private ElasticConnector<Task> ect;
	
	@Autowired
	public void configureES(ElasticConnector<Task> ect, @Value("task") String type){
		ect.setType(type);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/test")
	@ResponseBody
	public ResponseEntity<String> test() {
		
		System.out.println("--------------------------------");
		System.out.println("ES-Config");
		System.out.println("--------------------------------");
		System.out.println(ect.getIndex()+" --> Index");
		System.out.println(ect.getType()+" --> Type");
		System.out.println(ect.getAddress()+" --> Address");
		System.out.println(ect.getPort()+" --> Port");
		System.out.println(ect.getThreshold()+" --> Threshold");
		System.out.println("--------------------------------");
		
		return JSONResponseHelper.createResponse("done", true);
		
	}

		
		
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/include")
	@ResponseBody
	public ResponseEntity<String> include() {
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		CracUser BradenADMIN = new CracUser();

		BradenADMIN.setName("BradenADMIN");
		BradenADMIN.setFirstName("Daniel");
		BradenADMIN.setLastName("Braden");
		BradenADMIN.setPassword(bcryptEncoder.encode("default"));
		BradenADMIN.addRole(roleDAO.findByName("ADMIN"));
		BradenADMIN.setPhone("35678987654");
		BradenADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(BradenADMIN);

		CracUser BradenUSER = new CracUser();

		BradenUSER.setName("BradenUSER");
		BradenUSER.setFirstName("Daniel");
		BradenUSER.setLastName("Braden");
		BradenUSER.setPassword(bcryptEncoder.encode("default"));
		BradenUSER.addRole(roleDAO.findByName("USER"));
		BradenUSER.setPhone("35678987654");
		BradenUSER.setEmail("Mustermail@internet.at");
		userDAO.save(BradenUSER);

		CracUser StillerADMIN = new CracUser();

		StillerADMIN.setName("StillerADMIN");
		StillerADMIN.setFirstName("Susanne");
		StillerADMIN.setLastName("Stiller");
		StillerADMIN.setPassword(bcryptEncoder.encode("default"));
		StillerADMIN.addRole(roleDAO.findByName("ADMIN"));
		StillerADMIN.setPhone("35678987654");
		StillerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(StillerADMIN);

		CracUser StillerUSER = new CracUser();

		StillerUSER.setName("StillerUSER");
		StillerUSER.setFirstName("Susanne");
		StillerUSER.setLastName("Stiller");
		StillerUSER.setPassword(bcryptEncoder.encode("default"));
		StillerUSER.addRole(roleDAO.findByName("USER"));
		StillerUSER.setPhone("35678987654");
		StillerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(StillerUSER);
		
		CracUser VasicADMIN = new CracUser();

		VasicADMIN.setName("VasicADMIN");
		VasicADMIN.setFirstName("Mili");
		VasicADMIN.setLastName("Vasic");
		VasicADMIN.setPassword(bcryptEncoder.encode("default"));
		VasicADMIN.addRole(roleDAO.findByName("ADMIN"));
		VasicADMIN.setPhone("35678987654");
		VasicADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VasicADMIN);

		CracUser VasicUSER = new CracUser();

		VasicUSER.setName("VasicUSER");
		VasicUSER.setFirstName("Mili");
		VasicUSER.setLastName("Vasic");
		VasicUSER.setPassword(bcryptEncoder.encode("default"));
		VasicUSER.addRole(roleDAO.findByName("USER"));
		VasicUSER.setPhone("35678987654");
		VasicUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VasicUSER);

		CracUser VitekaADMIN = new CracUser();

		VitekaADMIN.setName("VitekaADMIN");
		VitekaADMIN.setFirstName("Anita");
		VitekaADMIN.setLastName("Viteka");
		VitekaADMIN.setPassword(bcryptEncoder.encode("default"));
		VitekaADMIN.addRole(roleDAO.findByName("ADMIN"));
		VitekaADMIN.setPhone("35678987654");
		VitekaADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VitekaADMIN);

		CracUser VitekaUSER = new CracUser();

		VitekaUSER.setName("VitekaUSER");
		VitekaUSER.setFirstName("Anita");
		VitekaUSER.setLastName("Viteka");
		VitekaUSER.setPassword(bcryptEncoder.encode("default"));
		VitekaUSER.addRole(roleDAO.findByName("USER"));
		VitekaUSER.setPhone("35678987654");
		VitekaUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VitekaUSER);

		CracUser VeriADMIN = new CracUser();

		VeriADMIN.setName("VeriADMIN");
		VeriADMIN.setFirstName("Veri");
		VeriADMIN.setLastName("Unknown");
		VeriADMIN.setPassword(bcryptEncoder.encode("default"));
		VeriADMIN.addRole(roleDAO.findByName("ADMIN"));
		VeriADMIN.setPhone("35678987654");
		VeriADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VeriADMIN);

		CracUser VeriUSER = new CracUser();

		VeriUSER.setName("VeriUSER");
		VeriUSER.setFirstName("Veri");
		VeriUSER.setLastName("Unknown");
		VeriUSER.setPassword(bcryptEncoder.encode("default"));
		VeriUSER.addRole(roleDAO.findByName("USER"));
		VeriUSER.setPhone("35678987654");
		VeriUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VeriUSER);
		
		CracUser AlexADMIN = new CracUser();

		VitekaADMIN.setName("AlexADMIN");
		AlexADMIN.setFirstName("Alex");
		AlexADMIN.setLastName("Unknown");
		AlexADMIN.setPassword(bcryptEncoder.encode("default"));
		AlexADMIN.addRole(roleDAO.findByName("ADMIN"));
		AlexADMIN.setPhone("35678987654");
		AlexADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(AlexADMIN);

		CracUser AlexUSER = new CracUser();

		AlexUSER.setName("AlexUSER");
		AlexUSER.setFirstName("Alex");
		AlexUSER.setLastName("Unknown");
		AlexUSER.setPassword(bcryptEncoder.encode("default"));
		AlexUSER.addRole(roleDAO.findByName("USER"));
		AlexUSER.setPhone("35678987654");
		AlexUSER.setEmail("Mustermail@internet.at");
		userDAO.save(AlexUSER);

		return JSONResponseHelper.createResponse("included", true);
	}
	
	@RequestMapping("/filters")
	@ResponseBody
	public ResponseEntity<String> filters() {
		
		cs.synchronize();

		
		System.out.println("-----------------------");
		System.out.println("Adding Filters!");
		matchingConfig.clearFilters();
		matchingConfig.addFilter(new ProficiencyLevelFilter());
		matchingConfig.addFilter(new LikeLevelFilter());
		matchingConfig.addFilter(new ImportancyLevelFilter());
		System.out.println("-----------------------");
		
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filters", "ADDED");
		return JSONResponseHelper.createResponse(true, meta);
		
	}
	
	/*
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
		/*
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
	}*/
	
	public void addActualUsers(){
		CracUser SchönböckADMIN = new CracUser();
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		SchönböckADMIN.setName("SchoenboeckADMIN");
		SchönböckADMIN.setFirstName("Johannes");
		SchönböckADMIN.setLastName("Schönböck");
		SchönböckADMIN.setPassword(bcryptEncoder.encode("default"));
		SchönböckADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchönböckADMIN.setPhone("35678987654");
		SchönböckADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckADMIN);
		
		CracUser SchönböckUSER = new CracUser();

		SchönböckUSER.setName("SchoenboeckUSER");
		SchönböckUSER.setFirstName("Johannes");
		SchönböckUSER.setLastName("Schönböck");
		SchönböckUSER.setPassword(bcryptEncoder.encode("default"));
		SchönböckUSER.addRole(roleDAO.findByName("USER"));
		SchönböckUSER.setPhone("35678987654");
		SchönböckUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckUSER);

		CracUser PröllADMIN = new CracUser();

		PröllADMIN.setName("ProellADMIN");
		PröllADMIN.setFirstName("Birigt");
		PröllADMIN.setLastName("Pröll");
		PröllADMIN.setPassword(bcryptEncoder.encode("default"));
		PröllADMIN.addRole(roleDAO.findByName("ADMIN"));
		PröllADMIN.setPhone("35678987654");
		PröllADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(PröllADMIN);
		
		CracUser PröllUSER = new CracUser();

		PröllUSER.setName("ProellUSER");
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

		CracUser KapsammerADMIN = new CracUser();

		KapsammerADMIN.setName("KapsammerADMIN");
		KapsammerADMIN.setFirstName("Elisabeth");
		KapsammerADMIN.setLastName("Kapsammer");
		KapsammerADMIN.setPassword(bcryptEncoder.encode("default"));
		KapsammerADMIN.addRole(roleDAO.findByName("ADMIN"));
		KapsammerADMIN.setPhone("35678987654");
		KapsammerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(KapsammerADMIN);
		
		CracUser KapsammerUSER = new CracUser();

		KapsammerUSER.setName("KapsammerUSER");
		KapsammerUSER.setFirstName("Elisabeth");
		KapsammerUSER.setLastName("Kapsammer");
		KapsammerUSER.setPassword(bcryptEncoder.encode("default"));
		KapsammerUSER.addRole(roleDAO.findByName("USER"));
		KapsammerUSER.setPhone("35678987654");
		KapsammerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(KapsammerUSER);

		CracUser SchwingerADMIN = new CracUser();

		SchwingerADMIN.setName("SchwingerADMIN");
		SchwingerADMIN.setFirstName("Wieland");
		SchwingerADMIN.setLastName("Schwinger");
		SchwingerADMIN.setPassword(bcryptEncoder.encode("default"));
		SchwingerADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchwingerADMIN.setPhone("35678987654");
		SchwingerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchwingerADMIN);
		
		CracUser SchwingerUSER = new CracUser();

		SchwingerUSER.setName("SchwingerUSER");
		SchwingerUSER.setFirstName("Wieland");
		SchwingerUSER.setLastName("Schwinger");
		SchwingerUSER.setPassword(bcryptEncoder.encode("default"));
		SchwingerUSER.addRole(roleDAO.findByName("USER"));
		SchwingerUSER.setPhone("35678987654");
		SchwingerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchwingerUSER);

		
		//Users for Birgit
		
		CracUser bpAdmin = new CracUser();

		bpAdmin.setName("bp-admin");
		bpAdmin.setFirstName("Test");
		bpAdmin.setLastName("Admin");
		bpAdmin.setPassword(bcryptEncoder.encode("default"));
		bpAdmin.addRole(roleDAO.findByName("ADMIN"));
		bpAdmin.setPhone("35678987654");
		bpAdmin.setEmail("Mustermail@internet.at");
		userDAO.save(bpAdmin);

		CracUser bpVol1 = new CracUser();

		bpVol1.setName("bp-vol1");
		bpVol1.setFirstName("Test");
		bpVol1.setLastName("User1");
		bpVol1.setPassword(bcryptEncoder.encode("default"));
		bpVol1.addRole(roleDAO.findByName("USER"));
		bpVol1.setPhone("35678987654");
		bpVol1.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol1);

		CracUser bpVol2 = new CracUser();

		bpVol2.setName("bp-vol2");
		bpVol2.setFirstName("Test");
		bpVol2.setLastName("User2");
		bpVol2.setPassword(bcryptEncoder.encode("default"));
		bpVol2.addRole(roleDAO.findByName("USER"));
		bpVol2.setPhone("35678987654");
		bpVol2.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol2);

		CracUser bpVol3 = new CracUser();

		bpVol3.setName("bp-vol3");
		bpVol3.setFirstName("Test");
		bpVol3.setLastName("User3");
		bpVol3.setPassword(bcryptEncoder.encode("default"));
		bpVol3.addRole(roleDAO.findByName("USER"));
		bpVol3.setPhone("35678987654");
		bpVol3.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol3);
		
		//Users for Claudia
		
		CracUser cvadmin1 = new CracUser();

		cvadmin1.setName("cvadmin1");
		cvadmin1.setFirstName("Test");
		cvadmin1.setLastName("Admin");
		cvadmin1.setPassword(bcryptEncoder.encode("default"));
		cvadmin1.addRole(roleDAO.findByName("ADMIN"));
		cvadmin1.setPhone("35678987654");
		cvadmin1.setEmail("Mustermail@internet.at");
		userDAO.save(cvadmin1);

		CracUser cvvol1 = new CracUser();

		cvvol1.setName("cvvol1");
		cvvol1.setFirstName("Test");
		cvvol1.setLastName("User1");
		cvvol1.setPassword(bcryptEncoder.encode("default"));
		cvvol1.addRole(roleDAO.findByName("USER"));
		cvvol1.setPhone("35678987654");
		cvvol1.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol1);

		CracUser cvvol2 = new CracUser();

		cvvol2.setName("cvvol2");
		cvvol2.setFirstName("Test");
		cvvol2.setLastName("User2");
		cvvol2.setPassword(bcryptEncoder.encode("default"));
		cvvol2.addRole(roleDAO.findByName("USER"));
		cvvol2.setPhone("35678987654");
		cvvol2.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol2);

		CracUser cvvol3 = new CracUser();

		cvvol3.setName("cvvol3");
		cvvol3.setFirstName("Test");
		cvvol3.setLastName("User3");
		cvvol3.setPassword(bcryptEncoder.encode("default"));
		cvvol3.addRole(roleDAO.findByName("USER"));
		cvvol3.setPhone("35678987654");
		cvvol3.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol3);

		
	}

}
