package crac.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.get.GetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.TaskDAO;
import crac.models.CracUser;
import crac.models.SearchTransformer;
import crac.models.Task;

@RestController
@RequestMapping("/elastic")
public class ElasticController {
	
	private ElasticConnector ESConn = new ElasticConnector("localhost", 9300);
	private SearchTransformer ST = new SearchTransformer();
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private TaskDAO taskDAO;

	@RequestMapping(value = "/addTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addTask(@PathVariable(value = "task_id") long task_id) throws JsonProcessingException {

		Task originalTask = taskDAO.findOne(task_id);
		
		if(ESConn.indexOrUpdateElasticTask(ST.transformTask(originalTask)).isCreated()){
			return ResponseEntity.ok().body("{\"entry\":\"true\"}");
		}else{
			return ResponseEntity.ok().body("{\"updated\":\"true\"}");
		}
		
	}
	
	@RequestMapping(value = "/getTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getTask(@PathVariable(value = "task_id") String task_id) throws JsonProcessingException {
		
		GetResponse response = ESConn.getElasticTask(task_id);

		return ResponseEntity.ok().body(response.getSourceAsString());
		
	}
	
	@RequestMapping(value = "/searchES", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> searchES() throws JsonProcessingException {

		return ResponseEntity.ok().body(ESConn.queryElasticTask().toString());
		
	}

	
}
