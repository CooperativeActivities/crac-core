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
			response = this.client.prepareIndex(index, type, id)
					.setSource(this.mapper.writeValueAsString(obj)).get();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return response;

	}

	public GetResponse get(String id) {
		return client.prepareGet(index, type, id).get();
	}
	
	public DeleteResponse delete(String id){
		return client.prepareDelete(index, type, id).get();
	}
	
	public SearchResponse query() {
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				// .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				   .setQuery(QueryBuilders.matchQuery( "neededCompetences.name", "programming")) // Query
				   .setQuery(QueryBuilders.matchQuery( "neededCompetences.name", "breathing")) // Query
				// .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))
				// // Filter
				// .setFrom(0).setSize(60).setExplain(true)
				.execute().actionGet();
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
