package crac.elastic_depricated;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.relationmodels.UserCompetenceRel;
import crac.utility.CompetenceAugmenter;
import crac.utility.SearchTransformer;

@RestController
@RequestMapping("/elastic")
public class ElasticController {

	@Value("${custom.elasticUrl}")
	private String url;

	@Value("${custom.elasticPort}")
	private int port;

	private ElasticConnector<ElasticTask> ESConnTask;
	private ElasticConnector<ElasticUser> ESConnUser;

	@Autowired
	private SearchTransformer ST;

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	@PostConstruct
	public void init() {

		System.out.println(url);
		System.out.println(port);
		ESConnTask = new ElasticConnector<ElasticTask>(url, port, "crac_core", "elastic_task");
		ESConnUser = new ElasticConnector<ElasticUser>(url, port, "crac_core", "elastic_user");

	}
	/*
	@RequestMapping(value = "/addTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addTask(@PathVariable(value = "task_id") long task_id)
			throws JsonProcessingException {

		Task originalTask = taskDAO.findOne(task_id);

		if (ESConnTask.indexOrUpdate("" + originalTask.getId(), ST.transformTask(originalTask)).isCreated()) {
			return ResponseEntity.ok().body("{\"entry\":\"true\"}");
		} else {
			return ResponseEntity.ok().body("{\"updated\":\"true\"}");
		}

	}
*/
	@RequestMapping(value = "/getTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getTask(@PathVariable(value = "task_id") String task_id)
			throws JsonProcessingException {

		GetResponse response = ESConnTask.get(task_id);

		return ResponseEntity.ok().body(response.getSourceAsString());

	}

	@RequestMapping(value = "/deleteTask/{task_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> deleteTask(@PathVariable(value = "task_id") String task_id)
			throws JsonProcessingException {

		DeleteResponse response = ESConnTask.delete(task_id);

		return ResponseEntity.ok().body("{\"id\":\"" + task_id + "\", \"deleted\": \"" + response.isFound() + "\"}");

	}

	@RequestMapping(value = "/searchES/task", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> searchESTask() throws JsonProcessingException {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());

		/*
		 * ElasticUser ep = ST.transformUser(myUser); Set<ElasticCompetence>
		 * competences = ep.getSetCompetences();
		 */
		ObjectMapper mapper = new ObjectMapper();
		ElasticUser me = null;
		try {
			me = mapper.readValue(ESConnUser.get("" + myUser.getId()).getSourceAsString(), ElasticUser.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (ElasticCompetence c : me.getRelatedCompetences()) {
			System.out.println(c.getName());
		}

		return ResponseEntity.ok()
				.body("{\"found_set\":" + ESConnTask.query("neededCompetences.name", me.getSetCompetences()).toString()
						+ ", \"found_augmented\":"
						+ ESConnTask.query("neededCompetences.name", me.getRelatedCompetences()).toString() + "}");
	}
/*
	@RequestMapping(value = "/addUser/{user_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addUser(@PathVariable(value = "user_id") long user_id)
			throws JsonProcessingException {

		CracUser originalUser = userDAO.findOne(user_id);

		if (ESConnUser.indexOrUpdate("" + originalUser.getId(), ST.transformUser(originalUser)).isCreated()) {
			return ResponseEntity.ok().body("{\"entry\":\"true\"}");
		} else {
			return ResponseEntity.ok().body("{\"updated\":\"true\"}");
		}

	}
*/
	@RequestMapping(value = "/getUser/{user_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getUser(@PathVariable(value = "user_id") String user_id)
			throws JsonProcessingException {

		GetResponse response = ESConnUser.get(user_id);

		return ResponseEntity.ok().body(response.getSourceAsString());

	}

	@RequestMapping(value = "/deleteUser/{user_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> deleteUser(@PathVariable(value = "user_id") String user_id)
			throws JsonProcessingException {

		DeleteResponse response = ESConnUser.delete(user_id);

		return ResponseEntity.ok().body("{\"id\":\"" + user_id + "\", \"deleted\": \"" + response.isFound() + "\"}");

	}
/*
	@RequestMapping(value = "/searchES/user/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> searchESUser(@PathVariable(value = "task_id") long task_id)
			throws JsonProcessingException {

		Task task = taskDAO.findOne(task_id);

		ElasticTask et = ST.transformTask(task);

		return ResponseEntity.ok().body(ESConnUser.query("competences.name", et.getNeededCompetences()).toString());

	}
	*/
/*
	@RequestMapping(value = "/testAugment/{steps}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> testAugment(@PathVariable(value = "steps") int steps) throws JsonProcessingException {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());

		// ElasticUser eu = ST.transformUser(myUser);
		/*
		 * HashSet<ElasticCompetence> cSet = new HashSet<ElasticCompetence>();
		 * 
		 * for(UserCompetenceRel r : myUser.getCompetenceRelationships()){
		 * caug.augmentWithDistance(r.getCompetence(), steps, steps, cSet); }
		 * 
		 * for(ElasticCompetence c : cSet){ System.out.println(c.getId() + " | "
		 * +c.getName()+" | distance travelled: "+c.getTravelled()+
		 * " | bad path: "+c.isBadPath()); }
		 */
/*
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(ST.transformUser(myUser)));

	}
*/
}
