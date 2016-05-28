package crac.elastic;

import org.springframework.data.annotation.Id;
public class ElasticTask {

	private String name;
	
	private String description;

	public ElasticTask() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
