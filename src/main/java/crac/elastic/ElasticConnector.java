package crac.elastic;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.Competence;

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

	public SearchResponse query(String matchField, Set<ElasticCompetence> competences) {
		SearchRequestBuilder search = client.prepareSearch(index).setTypes(type);
		if (competences.size() != 0) {
			System.out.println("COMPETENCES:");
			String text = "";
			for (ElasticCompetence c : competences) {
				//System.out.println(c.getName());
				text += " " + c.getName();
				//search.setQuery(QueryBuilders.matchQuery(matchField, c.getName()));
			}
			System.out.println("Query for:"+text);
			search.setQuery(QueryBuilders.multiMatchQuery(text, matchField));
		} else {
			System.out.println("COMPETENCES EMPTY!!");
			search.setQuery(QueryBuilders.matchQuery(matchField, "NOCOMPETENCE"));
			search.setQuery(QueryBuilders.matchQuery(matchField, "FALSE"));
		}
		/*
		.should(QueryBuilders.matchQuery("firstName", "Ben"))
        .should(QueryBuilders.matchQuery("lastName", "McCann"))
        .should(QueryBuilders.matchQuery("emails.canonical", "ben@ben.com"))
        */
		return search.execute().actionGet();
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
