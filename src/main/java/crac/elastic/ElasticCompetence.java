package crac.elastic;

public class ElasticCompetence {
	
	private long id;

	private String name;
	
	private String description;
	
	private int travelled;
	
	private boolean badPath;
	
	public ElasticCompetence() {
	}

	public ElasticCompetence(long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.travelled = 0;
		this.badPath = false;
	}
	
	public ElasticCompetence(long id, String name, String description, int travelled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.travelled = travelled;
		this.badPath = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public int getTravelled() {
		return travelled;
	}

	public void setTravelled(int travelled) {
		this.travelled = travelled;
	}

	public boolean isBadPath() {
		return badPath;
	}

	public void setBadPath(boolean badPath) {
		this.badPath = badPath;
	}
	
	

}
