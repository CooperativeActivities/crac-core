package crac.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
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

import crac.enums.TaskState;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;

public class ElasticConnector<T> {

	private Client client;
	private ObjectMapper mapper;
	private String index;
	private String type;
	private String address;
	private int port;
	
	private static final double ES_THRESHOLD = 0.05;

	public ElasticConnector(String address, int port, String index, String type) {
		mapper = new ObjectMapper();
		this.port = port;
		this.address = address;
		this.index = index;
		this.type = type;
	}
	
	private void wake(){
		try {
			client = TransportClient.builder().build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	private void close(){
		this.client.close();
	}

	public IndexResponse indexOrUpdate(String id, T obj) {

		wake();
		
		IndexResponse response = null;
		try {
			response = this.client.prepareIndex(index, type, id).setSource(this.mapper.writeValueAsString(obj)).get();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		close();
		
		return response;

	}

	public GetResponse get(String id) {
		wake();
		GetResponse r = client.prepareGet(index, type, id).get();
		close();
		return r;
	}

	public DeleteResponse delete(String id) {
		wake();
		DeleteResponse r = client.prepareDelete(index, type, id).get();
		close();
		return r;
	}

	public ArrayList<EvaluatedTask> query(String searchText, TaskDAO taskDAO) {
		wake();
		SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
		search.setQuery(QueryBuilders.matchQuery("name", searchText));
		search.setQuery(QueryBuilders.matchQuery("description", searchText));
		SearchResponse sr = search.execute().actionGet();
		ArrayList<EvaluatedTask> foundTasks = new ArrayList<EvaluatedTask>();

		System.out.println(sr.toString());
		for (SearchHit hit : sr.getHits()) {
			Long id = Long.decode(hit.getId());
			double score = hit.getScore();
			if (score >= ES_THRESHOLD) {
				EvaluatedTask evTask = new EvaluatedTask(taskDAO.findOne(id), score);
				if (evTask.getTask().getTaskState() != TaskState.NOT_PUBLISHED) {
					foundTasks.add(evTask);
				}
			}
		}
		close();
		return foundTasks;
	}

	public DeleteIndexResponse deleteIndex() {
		wake();
		DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
		close();
		return response;
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
