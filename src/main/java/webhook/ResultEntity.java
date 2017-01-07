package webhook;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ResultEntity {
	
	private String speech;
	private String source;
	private String displayText;
	
	public ResultEntity(String speech, String source, String displayText) {
		this.speech = speech;
		this.source = source;
		this.displayText = displayText;
	}
	
	public ResultEntity() {
	}

	public ResponseEntity<String> getResultJson(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return ResponseEntity.ok().headers(headers)
				.body("{\"speech\":\"" + speech + "\"," + 
						"\"source\":\"" + source + "\"," +
						"\"displayText\":\"" + displayText + "!\"}");
	}
	
	public String getSpeech() {
		return speech;
	}
	
	public void setSpeech(String speech) {
		this.speech = speech;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getDisplayText() {
		return displayText;
	}
	
	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	
}
