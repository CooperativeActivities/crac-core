package crac.utility;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.TaskState;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Role;
import crac.models.Task;
import crac.models.output.errors.MessageHandler;
import crac.models.output.errors.ResponseHandler;
import crac.models.relation.CompetenceRelationshipType;
import crac.models.relation.UserTaskRel;
import crac.models.CracToken;
import crac.notifier.Notification;

public class JSonResponseHelper {
	
	public static ResponseEntity<String> messageArraySuccess(HashMap<String, String> data){
		MessageHandler mh = new MessageHandler(true, "", data);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(mh);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result);
	}
	
	public static ResponseEntity<String> messageArray(HashMap<String, String> data){
		MessageHandler mh = new MessageHandler(false, "bad_request", data);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(mh);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result);
	}
	
	public static ResponseEntity<String> createResponse(boolean success, String error, String cause){
		ResponseHandler rh = new ResponseHandler(success, error, cause);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(rh);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result);
	}
	
	//Creation Helpers
		
	public static ResponseEntity<String> successFullyCreated(CracUser u){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}	
	
	public static ResponseEntity<String> successFullyCreated(Role r){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"user\":\"" + r.getId() + "\",\"name\":\"" + r.getName() + "\"}");
	}

	
	public static ResponseEntity<String> successFullyCreated(Competence c){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyCreated(Task t){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyCreated(CompetenceRelationshipType crt){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}

	public static ResponseEntity<String> successFullyCreated(Evaluation e){
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"evaluation\":\"" + e.getId() + "\"}");
	}

	//Delete Helpers
	
	public static ResponseEntity<String> successFullyDeleted(CracUser u){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(Role r){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"user\":\"" + r.getId() + "\",\"name\":\"" + r.getName() + "\"}");
	}

	public static ResponseEntity<String> successFullyDeleted(Competence c){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(Task t){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(CompetenceRelationshipType crt){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}

	public static ResponseEntity<String> indexSuccessFullyDeleted(String index){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"index\":\"" + index + "\"}");
	}

	
	//Update Helpers
	
	public static ResponseEntity<String> successFullyUpdated(CracUser u){
		return addEntity("{\"success\":\"true\", \"action\":\"update\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(Role r){
		return addEntity("{\"success\":\"true\", \"action\":\"update\", \"user\":\"" + r.getId() + "\",\"name\":\"" + r.getName() + "\"}");
	}

	public static ResponseEntity<String> successFullyUpdated(Competence c){
		return addEntity("{\"success\":\"true\", \"action\":\"update\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(Task t){
		return addEntity("{\"success\":\"true\", \"action\":\"update\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(CompetenceRelationshipType crt){
		return addEntity("{\"success\":\"true\", \"action\":\"update\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}


	//Error Helpers

	public static ResponseEntity<String> idNotFound(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"id does not exist\"}");
	}
	
	public static ResponseEntity<String> alreadyExists(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_post_request\", \"cause\":\"dataset already exists\"}");
	}
	
	public static ResponseEntity<String> jsonReadError(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_post_put_request\", \"cause\":\"json-file contains error(s)\"}");
	}
	
	public static ResponseEntity<String> jsonMapError(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_post_put_request\", \"cause\":\"can not map json to object\"}");
	}
	
	public static ResponseEntity<String> jsonWriteError(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"can not write generated json\"}");
	}
	
	public static ResponseEntity<String> ressourceUnchangeable(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"this ressource is in an unchangeable state\"}");
	}
	
	public static ResponseEntity<String> actionNotPossible(String msg){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\""+msg+"\"}");
	}

	
	//No-Result Helpers

	public static ResponseEntity<String> emptyData(){
		return addEntity("{\"success\":\"false\", \"error\":\"no-data\", \"cause\":\"request returned empty data\"}");
	}

	
	//Task State-Change Helpers
	
	public static ResponseEntity<String> successTaskStateChanged(Task t, TaskState ts){
		return addEntity("{\"success\":\"true\", \"action\":\"state_change\", \"state\":\""+ts.toString()+"\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> stateNotAvailable(String name){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"There is no such state "+name+"\"}");
	}
	
	// Assign Helpers
	
	public static ResponseEntity<String> successFullyAssigned(CracUser u){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(Role r){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"role\":\"" + r.getId() + "\",\"name\":\"" + r.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(Competence c){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(Task t){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(CompetenceRelationshipType crt){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(UserTaskRel utr){
		return addEntity("{\"success\":\"true\", \"action\":\"assign\", \"user_task_relationship\":\"" + utr.getId() + "\",\"user\":\"" + utr.getUser().getName() + "\",\"task\":\"" + utr.getTask().getName() + "\"}");
	}

	//Notification Helpers
	
	public static ResponseEntity<String> successfullFriendRequest(CracUser c){
		return addEntity("{\"success\":\"true\", \"action\":\"friend_request\", \"receiver\":\"" + c.getId() + "\",\"user_name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successfullUnfriend(CracUser c){
		return addEntity("{\"success\":\"true\", \"action\":\"unfriend\", \"user_id\":\"" + c.getId() + "\",\"user_name\":\"" + c.getName() + "\"}");
	}

	
	public static ResponseEntity<String> successfullEvaluation(){
		return addEntity("{\"success\":\"true\", \"action\":\"evaluation\"}");
	}
	
	public static ResponseEntity<String> noSuchNotification(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"action\":\"request\", \"cause\":\"no such notification found\"}");
	}
	
	public static ResponseEntity<String> successfullySent(){
		return addEntity("{\"success\":\"true\", \"action\":\"send\"}");
	}
	
	public static ResponseEntity<String> successfullyAccepted(Notification n, String m){
		return addEntity("{\"success\":\"true\", \"action\":\"accept\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}");
	}
	
	public static ResponseEntity<String> successfullyDenied(Notification n, String m){
		return addEntity("{\"success\":\"true\", \"action\":\"deny\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}");
	}


	
	//User Check Helpers
	
	public static ResponseEntity<String> checkUserSuccess(CracUser user){
		return addEntity("{\"success\":\"true\", \"action\":\"check_for_existence\", \"user\":\"" + user.getId() + "\",\"name\":\"" + user.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullAction(String msg){
		return addEntity("{\"success\":\"true\" , \"msg\":\""+msg+"\"}");
	}

	
	//Boot_Mode Helpers
	
	public static ResponseEntity<String> bootSuccess(){
		return addEntity("{\"success\":\"true\", \"action\":\"boot\"}");
	}
	
	public static ResponseEntity<String> alreadyBooted(){
		return addEntity("{\"success\":\"false\", \"action\":\"boot\", \"cause\":\"already booted\"}");
	}
	
	public static ResponseEntity<String> bootOff(){
		return addEntity("{\"success\":\"false\", \"action\":\"boot\", \"cause\":\"bootMode is off\"}");
	}
	
	//Token Helpers
	
	public static ResponseEntity<String> tokenSuccess(CracUser user, CracToken token){
		ObjectMapper mapper = new ObjectMapper();
		String roles = "";
		try {
			roles = mapper.writeValueAsString(user.getRoles());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return addEntity("{\"success\":\"true\", \"action\":\"create_token\", \"id\":\""+user.getId()+"\", "
				+ "\"user\":\""+user.getName()+"\", \"token\":\""+token.getCode()+"\", \"roles\":"+roles+"}");
	}
	
	public static ResponseEntity<String> tokenFailure(CracUser user, CracToken token){
		ObjectMapper mapper = new ObjectMapper();
		String roles = "";
		try {
			roles = mapper.writeValueAsString(user.getRoles());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return addEntity("{\"success\":\"false\", \"action\":\"create_token\", \"cause\":\"token already created\", \"id\":\""+user.getId()+"\""
				+ ", \"user\":\""+user.getName()+"\", \"token\":\""+token.getCode()+"\", \"roles\":"+roles+"}");
	}
	
	public static ResponseEntity<String> tokenDestroySuccess(CracUser user){
		return addEntity("{\"success\":\"true\", \"action\":\"destroy_token\", \"id\":\""+user.getId()+"\", "
				+ "\"user\":\""+user.getName()+"\"}");
	}
	
	public static ResponseEntity<String> tokenDestroyFailure(CracUser user){
		return addEntity("{\"success\":\"false\", \"action\":\"destroy_token\", \"cause\":\"no token available\"}");
	}
	
	public static ResponseEntity<String> print(String msg){
		return addEntity(msg);
	}
	
	private static ResponseEntity<String> addEntity(String response){
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok().headers(headers).body(response);
				
	}
		
}
