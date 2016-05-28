package crac.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

	@RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "name") String name) throws JsonProcessingException {
		ElasticTask myTask = new ElasticTask();
		myTask.setDescription("blah");
		myTask.setName(name);
		ObjectMapper mapper = new ObjectMapper();
		
		Client client = null;
		try {
			client = TransportClient.builder().build()
			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		IndexResponse response = client.prepareIndex("twitter", "tweet")
		        .setSource(mapper.writeValueAsString(myTask))
		        .get();

		
		if(response.isCreated()){
			return ResponseEntity.ok().body("new entry");
		}else{
			return ResponseEntity.ok().body("updated");
		}
		
		
	}

	
}
