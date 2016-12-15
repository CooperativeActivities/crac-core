package crac.models.utility;

public class SimpleQuery {
	
	private String text;

	public SimpleQuery(String text) {
		this.text = text;
	}
	
	public SimpleQuery() {
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
