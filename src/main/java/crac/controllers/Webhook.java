package crac.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.utility.JSonResponseHelper;
import crac.utility.SimpleLogger;

@RestController
@RequestMapping("/lol")
public class Webhook {

	private String euw = "https://euw.api.pvp.net/";
	private String global = "https://global.api.pvp.net/";
	private String key = "?api_key=RGAPI-9309e8ff-073d-40f7-a240-abe9f7b50cdd";

	@RequestMapping(value = { "/webhook/",
			"/webhook" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> hook(@RequestBody String json) throws ClientProtocolException, IOException {

		JsonNode rootNode = new ObjectMapper().readTree(new StringReader(json));
		JsonNode resultNode = rootNode.get("result");
		JsonNode parametersNode = resultNode.get("parameters");
		String action = resultNode.get("action").asText();

		String returns = "";
		
		if (action.equals("call-champion")) {
			
			String name = parametersNode.get("Player").asText();
			
			String call = call("api/lol/EUW/v1.4/summoner/by-name/"+name, euw);
			JsonNode playerNode = new ObjectMapper().readTree(new StringReader(call));
			JsonNode nameNode = playerNode.get(name);
			String id = nameNode.get("id").asText();
			
			call = call("championmastery/location/EUW1/player/"+id+"/topchampions", euw);
			JsonNode championsNode = new ObjectMapper().readTree(new StringReader(call));
			JsonNode championIdNode = null;
			for(JsonNode child : championsNode){
				championIdNode = child;
				break;
			}
			String mostPlayedId = championIdNode.get("championId").asText();
			
			call = call("api/lol/static-data/euw/v1.2/champion/"+mostPlayedId, global);
			JsonNode championNode = new ObjectMapper().readTree(new StringReader(call));
			JsonNode championNameNode = championNode.get("name");
			returns = championNameNode.asText();
		}


		// SimpleLogger.setString(s);
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok().headers(headers).body("{\"champion\":\""+returns+"\"}");

	}

	private String call(String url, String region) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpGet httpget = new HttpGet(region + url + key);

		HttpResponse response = httpclient.execute(httpget);

		response.getEntity().getContent();
		return IOUtils.toString(response.getEntity().getContent());
	}

	@RequestMapping(value = { "/get/", "/get" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> gethook() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return ResponseEntity.ok().headers(headers).body(SimpleLogger.getString());
	}

}
