package crac.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticConnector {

	private Client client;
	private ObjectMapper mapper;

	public ElasticConnector(String address, int port) {
		mapper = new ObjectMapper();
		try {
			client = TransportClient.builder().build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(address), port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public IndexResponse indexOrUpdateElasticTask(ElasticTask task) {

		IndexResponse response = null;

		try {
			response = this.client.prepareIndex("crac_core", "elastic_task", task.getId())
					.setSource(this.mapper.writeValueAsString(task)).get();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return response;

	}

	public GetResponse getElasticTask(String id) {
		return client.prepareGet("crac_core", "elastic_task", id).get();
	}
	
	public DeleteResponse deleteElasticTask(String id){
		return client.prepareDelete("crac_core", "elastic_task", id).get();
	}
	
	public SearchResponse queryElasticTask() {
		SearchResponse response = client.prepareSearch("crac_core").setTypes("elastic_task")
				// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				// .setQuery(QueryBuilders.termQuery("multi", "test")) // Query
				// .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))
				// // Filter
				// .setFrom(0).setSize(60).setExplain(true)
				.execute().actionGet();
		return response;
	}

}
