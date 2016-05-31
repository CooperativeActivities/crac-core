package crac.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;

@RestController
@RequestMapping("/elastic")
public class ElasticController {
	
	private ElasticConnector ESConn = new ElasticConnector("localhost", 9300);

	@RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "name") String name) throws JsonProcessingException {
		ElasticTask myTask = new ElasticTask();
		myTask.setDescription("blah");
		myTask.setName(name);
		myTask.setId(new Date().toString());

				
		if(ESConn.indexOrUpdateElasticTask(myTask).isCreated()){
			return ResponseEntity.ok().body("{\"entry\":\"true\"}");
		}else{
			return ResponseEntity.ok().body("{\"updated\":\"true\"}");
		}
		
		
	}
	
	@RequestMapping(value = "/searchES", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> searchES() throws JsonProcessingException {

		return ResponseEntity.ok().body(ESConn.queryElasticTask().toString());
		
	}

	
}
