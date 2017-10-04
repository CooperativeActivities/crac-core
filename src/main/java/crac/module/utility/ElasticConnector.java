package crac.module.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ConcreteTaskState;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;
import crac.module.matching.helpers.EvaluatedTask;
import lombok.Getter;

@Component
@Scope("singleton")
public class ElasticConnector<T> {

	@Autowired
	private TaskDAO taskDAO;

	@Getter
	@Value("${crac.elastic.url}")
	private String address;

	@Getter
	@Value("${crac.elastic.port}")
	private int port;

	@Getter
	@Value("${crac.elastic.index}")
	private String index;

	@Getter
	@Value("${crac.elastic.threshold}")
	private double threshold;

	@Autowired
	private ObjectMapper mapper;

	private TransportClient client;
	
	@Getter
	private String type;

	public ElasticConnector() {
	}

	@PostConstruct
	private void wake() {
		System.out.println("called on creation");
		try {
			//client = TransportClient.builder().build();
			client = new PreBuiltTransportClient(Settings.EMPTY);
			client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		type = "task";
	}

	@PreDestroy
	private void close() {
		this.client.close();
	}

	@SuppressWarnings("deprecation")
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
		GetResponse r = client.prepareGet(index, type, id).get();
		return r;
	}

	public DeleteResponse delete(String id) {
		DeleteResponse r = client.prepareDelete(index, type, id).get();
		return r;
	}

	public ArrayList<EvaluatedTask> query(String searchText) {
		System.out.println("index: " + index + ", type: " + type);
		SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
		search.setQuery(QueryBuilders.matchQuery("name", searchText));
		search.setQuery(QueryBuilders.matchQuery("description", searchText));
		SearchResponse sr = search.get();
		ArrayList<EvaluatedTask> foundTasks = new ArrayList<EvaluatedTask>();

		System.out.println(sr.toString());
		for (SearchHit hit : sr.getHits()) {
			Long id = Long.decode(hit.getId());
			double score = hit.getScore();
			if (score >= threshold) {
				EvaluatedTask evTask = new EvaluatedTask(taskDAO.findOne(id), score);
				if (evTask.getTask().getTaskState() != ConcreteTaskState.NOT_PUBLISHED) {
					foundTasks.add(evTask);
				}
			}
		}
		return foundTasks;
	}

	public List<Task> queryForTasks(String searchText) {
		System.out.println("index: " + index + ", type: " + type + ", searchText: " + searchText);

		SearchResponse sr = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.multiMatchQuery(searchText, "name", "description")).get();

		List<Long> ids = new ArrayList<>(); 
		
		sr.getHits().forEach( hit -> {
			if (hit.getScore() >= threshold) {
				ids.add(Long.decode(hit.getId()));
				//foundTasks.add(taskDAO.findOne(Long.decode(hit.getId())));
			}
		});
		
		List<Task> tasks = new ArrayList<>();
		
		taskDAO.findAll(ids).forEach(tasks::add);
				
		return tasks;
	}

	public DeleteIndexResponse deleteIndex() {
		DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();
		return response;
	}

}
