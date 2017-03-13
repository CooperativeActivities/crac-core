package crac.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.decider.filter.ImportancyLevelFilter;
import crac.decider.filter.LikeLevelFilter;
import crac.decider.filter.ProficiencyLevelFilter;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.config.GlobalMatrixFilterConfig;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetencePermissionTypeDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.relation.CompetencePermissionType;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.models.komet.daos.TxExabiscompetencesDescriptorDAO;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsDescriptorMmDAO;
import crac.models.komet.entities.TxExabiscompetencesDescriptor;
import crac.models.komet.entities.TxExabiscompetencesDescriptorsDescriptorMm;
import crac.storage.CompetenceStorage;
import crac.utility.JSonResponseHelper;

@RestController
@RequestMapping("/synchronzation")
public class SynchronizationController {
	
	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private GroupDAO groupDAO;

	
	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;
	
	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;

	@Autowired
	private TxExabiscompetencesDescriptorDAO txExabiscompetencesDescriptorDAO;

	@Autowired
	private TxExabiscompetencesDescriptorsDescriptorMmDAO txExabiscompetencesDescriptorsDescriptorMmDAO;


	/**
	 * Synchronizes the competences of the DB into the CompetenceStorage of the
	 * application and caches the relations
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/intern")
	@ResponseBody
	public ResponseEntity<String> internsync() {
		CompetenceStorage.synchronize(competenceDAO, competenceRelationshipDAO);
		System.out.println("-------------------------------");
		System.out.println("||||INTERN COMPETENCES SYNCED||||");
		System.out.println("-------------------------------");
		return JSonResponseHelper.successFullAction("Competences have been synchronized");
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/full")
	@ResponseBody
	public ResponseEntity<String> fullsync() {
		this.datasync();
		this.dbsync();
		this.filtersync();
		this.internsync();
		return JSonResponseHelper.successFullAction("Everything has been synced");
	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/database")
	@ResponseBody
	public ResponseEntity<String> dbsync() {
		HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m = new HashMap<>();

		handleCompetences(m);
		handleRelationships(m);
		
		System.out.println("-------------------------------");
		System.out.println("||||DATABASE SYNCED||||");
		System.out.println("-------------------------------");

		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().headers(headers).body(mapper.writeValueAsString(m));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}

	}

	private void handleRelationships(HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		Iterable<TxExabiscompetencesDescriptorsDescriptorMm> kometRelationshipList = txExabiscompetencesDescriptorsDescriptorMmDAO
				.findAll();
		Iterable<CompetenceRelationship> cracRelationshipList = competenceRelationshipDAO.findAll();
		HashMap<Long, CompetenceRelationship> cracRelationshipMap = new HashMap<>();

		for (CompetenceRelationship c : cracRelationshipList) {
			cracRelationshipMap.put(c.getId(), c);
		}

		ArrayList<CompetenceRelationship> newc = new ArrayList<>();
		ArrayList<CompetenceRelationship> updatec = new ArrayList<>();
		ArrayList<CompetenceRelationship> deletec = new ArrayList<>();

		for (TxExabiscompetencesDescriptorsDescriptorMm single : kometRelationshipList) {

			if (competenceDAO.findOne((long) single.getUidForeign()) == null
					|| competenceDAO.findOne((long) single.getUidForeign()) == null) {

				deletec.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
				cracRelationshipMap.remove((long) single.getUid());

			} else if (!cracRelationshipMap.containsKey((long) single.getUid())) {
				newc.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
			} else {
				updatec.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
				cracRelationshipMap.remove((long) single.getUid());
			}
		}

		for (Map.Entry<Long, CompetenceRelationship> set : cracRelationshipMap.entrySet()) {
			deletec.add(set.getValue());
		}

		handleNewCompetenceRelationships(newc, m);
		handleUpdatedCompetenceRelationships(updatec, m);
		handleDeletedCompetenceRelationships(deletec);

	}

	private void handleNewCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		for (CompetenceRelationship c : competencerels) {
			competenceRelationshipDAO.save(c);

			String comp1id = c.getCompetence1().getId() + "";
			String comp2id = c.getCompetence2().getId() + "";

			HashMap<String, String> comp1 = new HashMap<>();
			HashMap<String, String> comp2 = new HashMap<>();

			if (m.get("created").containsKey(comp1id)) {
				comp1 = m.get("created").get(comp1id).get("relations");
			} else if (m.get("updated").containsKey(comp1id)) {
				comp1 = m.get("updated").get(comp1id).get("relations");
			}

			if (m.get("created").containsKey(comp2id)) {
				comp2 = m.get("created").get(comp2id).get("relations");
			} else if (m.get("updated").containsKey(comp2id)) {
				comp2 = m.get("updated").get(comp2id).get("relations");
			}

			if (comp1 != null && comp2 != null) {
				comp1.put(comp2id + "", "CREATE");
				comp2.put(comp1id + "", "CREATE");
			}

		}
	}

	private void handleUpdatedCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		for (CompetenceRelationship c : competencerels) {
			competenceRelationshipDAO.save(c);
			String comp1id = c.getCompetence1().getId() + "";
			String comp2id = c.getCompetence2().getId() + "";

			HashMap<String, String> comp1 = new HashMap<>();
			HashMap<String, String> comp2 = new HashMap<>();

			if (m.get("created").containsKey(comp1id)) {
				comp1 = m.get("created").get(comp1id).get("relations");
			} else if (m.get("updated").containsKey(comp1id)) {
				comp1 = m.get("updated").get(comp1id).get("relations");
			}

			if (m.get("created").containsKey(comp2id)) {
				comp2 = m.get("created").get(comp2id).get("relations");
			} else if (m.get("updated").containsKey(comp2id)) {
				comp2 = m.get("updated").get(comp2id).get("relations");
			}

			if (comp1 != null && comp2 != null) {
				comp1.put(comp2id + "", "UPDATE");
				comp2.put(comp1id + "", "UPDATE");
			}
		}
	}

	private void handleDeletedCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels) {
		for (CompetenceRelationship c : competencerels) {
			competenceRelationshipDAO.delete(c);
		}
	}

	private void handleCompetences(HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {

		Iterable<TxExabiscompetencesDescriptor> kometCompetenceList = txExabiscompetencesDescriptorDAO.findAll();
		Iterable<Competence> cracCompetenceList = competenceDAO.findAll();
		HashMap<Long, Competence> cracCompetenceMap = new HashMap<>();

		for (Competence c : cracCompetenceList) {
			cracCompetenceMap.put(c.getId(), c);
		}

		ArrayList<Competence> newc = new ArrayList<>();
		ArrayList<Competence> updatec = new ArrayList<>();
		ArrayList<Competence> deletec = new ArrayList<>();

		for (TxExabiscompetencesDescriptor single : kometCompetenceList) {
			if (!cracCompetenceMap.containsKey((long) single.getUid())) {
				newc.add(single.mapToCompetence());
			} else {
				updatec.add(single.mapToCompetence());
				cracCompetenceMap.remove((long) single.getUid());
			}
		}

		for (Map.Entry<Long, Competence> set : cracCompetenceMap.entrySet()) {
			deletec.add(set.getValue());
		}

		HashMap<String, HashMap<String, HashMap<String, String>>> meta = new HashMap<String, HashMap<String, HashMap<String, String>>>();

		HashMap<String, HashMap<String, String>> details = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> numbercreate = new HashMap<String, String>();
		numbercreate.put("number", newc.size() + "");
		HashMap<String, String> numberupdate = new HashMap<String, String>();
		numberupdate.put("number", updatec.size() + "");
		HashMap<String, String> numberdelete = new HashMap<String, String>();
		numberdelete.put("number", deletec.size() + "");

		details.put("CREATE", numbercreate);
		details.put("UPDATE", numberupdate);
		details.put("DELETE", numberdelete);
		meta.put("details", details);
		m.put("meta", meta);

		m.put("created", handleNewCompetences(newc));
		m.put("updated", handleUpdatedCompetences(updatec));
		m.put("deleted", handleDeletedCompetences(deletec));

	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleNewCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "CREATE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());
			competenceDAO.save(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleUpdatedCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "UPDATE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());
			competenceDAO.save(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleDeletedCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "DELETE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());
			competenceDAO.delete(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/filter")
	@ResponseBody
	public ResponseEntity<String> filtersync() {
		System.out.println("-------------------------------");
		System.out.println("||||FILTERS SYNCED||||");
		System.out.println("-------------------------------");

		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new UserRelationFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
		return JSonResponseHelper.successFullAction("Filters have been synced");
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/data")
	@ResponseBody
	public ResponseEntity<String> datasync() {
		System.out.println("-------------------------------");
		System.out.println("||||DATA SYNCED||||");
		System.out.println("-------------------------------");

		addRoles();
		addCompetenceRelationshipTypes();
		addCompetencePermissionType();
		addUsers();
		return JSonResponseHelper.successFullAction("Data has been synced");
	}
	
	private void addRoles(){
		System.out.println("-------------------------------");
		System.out.println("--roles synced--");
		System.out.println("-------------------------------");

		Role userRole = new Role();
		userRole.setName("USER");
		roleDAO.save(userRole);
		
		Role editorRole = new Role();
		editorRole.setName("EDITOR");
		roleDAO.save(editorRole);
	}

	private void addCompetenceRelationshipTypes(){
		System.out.println("-------------------------------");
		System.out.println("--relationship-types synced--");
		System.out.println("-------------------------------");

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

	}
	
	private void addCompetencePermissionType(){
		System.out.println("-------------------------------");
		System.out.println("--permission-types synced--");
		System.out.println("-------------------------------");

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
	}

	private void addUsers(){
		System.out.println("-------------------------------");
		System.out.println("--users synced--");
		System.out.println("-------------------------------");

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
