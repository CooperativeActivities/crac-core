package crac.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.models.Competence;
import crac.models.Task;
import crac.utilityModels.EvaluatedTask;

public class ElasticConnector<T> {

	private Client client;
	private ObjectMapper mapper;
	private String index;
	private String type;

	public ElasticConnector(String address, int port, String index, String type) {
		mapper = new ObjectMapper();
		try {
			client = TransportClient.builder().build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.index = index;
		this.type = type;
	}

	public IndexResponse indexOrUpdate(String id, T obj) {

		IndexResponse response = null;

		try {
			response = this.client.prepareIndex(index, type, id).setSource(this.mapper.writeValueAsString(obj)).get();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return response;

	}

	public GetResponse get(String id) {
		return client.prepareGet(index, type, id).get();
	}

	public DeleteResponse delete(String id) {
		return client.prepareDelete(index, type, id).get();
	}

	public ArrayList<EvaluatedTask> query(String searchText, TaskDAO taskDAO) {
		SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
		search.setQuery(QueryBuilders.matchQuery("name", searchText));
		search.setQuery(QueryBuilders.matchQuery("description", searchText));
		SearchResponse sr = search.execute().actionGet();
		ArrayList<EvaluatedTask> foundTasks = new ArrayList<EvaluatedTask>();
		
		System.out.println(sr.toString());
		for (SearchHit hit : sr.getHits()) {
			Long id =  Long.decode(hit.getId());
			System.out.println(id);
			EvaluatedTask evTask = new EvaluatedTask(taskDAO.findOne(id), hit.getScore());
	        foundTasks.add(evTask);
	    }
		return foundTasks;
	}
	
	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
