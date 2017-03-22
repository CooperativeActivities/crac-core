package crac.utility;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCause;
import crac.enums.RESTAction;
import crac.enums.TaskState;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracToken;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.models.db.relation.UserTaskRel;
import crac.models.output.errors.MessageHandler;
import crac.models.output.errors.NestedMessageHandler;
import crac.models.output.errors.RESTError;
import crac.models.output.errors.RESTResponse;
import crac.models.output.errors.ResponseHandler;
import crac.notifier.Notification;

public class JSonResponseHelper {
	
	public static ResponseEntity<String> nestedResponse(boolean success, HashMap<String, HashMap<String, String>> data){
		NestedMessageHandler mh = new NestedMessageHandler(success, data);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(mh);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, true);
	}

	
	public static ResponseEntity<String> messageArraySuccess(HashMap<String, String> data){
		MessageHandler mh = new MessageHandler(true, "", data);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(mh);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, true);
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
		
		return addEntity(result, true);
	}
	
	public static <T> ResponseEntity<String> createGeneralResponse(boolean success, String cause, ErrorCause error){
		
		RESTResponse<T> r = new RESTResponse<T>(RESTAction.NOT_SET, false, null);
		r.addError(error, cause);
		
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, success);
	
	}

	public static <T> ResponseEntity<String> createMetaResponse(boolean success, String cause, ErrorCause error, HashMap<String, String> meta){
		
		RESTResponse<T> r = new RESTResponse<T>(RESTAction.NOT_SET, false, null);
		r.addError(error, cause);
		r.setMeta(meta);
		
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, success);
	
	}
	
	//REST-Change Helpers
	
	public static <T> ResponseEntity<String> successfullyCreated(T obj){	
		return constructSuccessResponse(obj, RESTAction.CREATE);
	}
	
	public static <T> ResponseEntity<String> successfullyDeleted(T obj){	
		return constructSuccessResponse(obj, RESTAction.DELETE);
	}
	
	public static <T> ResponseEntity<String> successfullyUpdated(T obj){	
		return constructSuccessResponse(obj, RESTAction.UPDATE);
	}
	
	public static <T> ResponseEntity<String> successfullyAssigned(T obj){	
		return constructSuccessResponse(obj, RESTAction.ASSIGN);
	}
	
	private static <T> ResponseEntity<String> constructSuccessResponse(T obj, RESTAction action){
		RESTResponse<T> r = new RESTResponse<T>(action, true, obj);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, true);
	}
	
	private static <T> ResponseEntity<String> constructFailureResponse(T obj, RESTAction action){
		RESTResponse<T> r = new RESTResponse<T>(action, false, obj);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		try {
			result = mapper.writeValueAsString(r);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return addEntity(result, false);
	}

	//Delete Helpers
	
	public static ResponseEntity<String> indexSuccessFullyDeleted(String index){
		return addEntity("{\"success\":\"true\", \"action\":\"delete\", \"index\":\"" + index + "\"}", true);
	}

		
	//No-Result Helpers

	public static ResponseEntity<String> emptyData(){
		return addEntity("{\"success\":\"false\", \"error\":\"no-data\", \"cause\":\"request returned empty data\"}", true);
	}

	
	//Task State-Change Helpers
	
	public static ResponseEntity<String> successTaskStateChanged(Task t, TaskState ts){
		return addEntity("{\"success\":\"true\", \"action\":\"state_change\", \"state\":\""+ts.toString()+"\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}", true);
	}
	
	public static ResponseEntity<String> stateNotAvailable(String name){
		return addEntity("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"There is no such state "+name+"\"}", false);
	}
	
	// Assign Helpers
	
	//Notification Helpers
	
	public static ResponseEntity<String> successfullFriendRequest(CracUser c){
		return addEntity("{\"success\":\"true\", \"action\":\"friend_request\", \"receiver\":\"" + c.getId() + "\",\"user_name\":\"" + c.getName() + "\"}", true);
	}
	
	public static ResponseEntity<String> successfullUnfriend(CracUser c){
		return addEntity("{\"success\":\"true\", \"action\":\"unfriend\", \"user_id\":\"" + c.getId() + "\",\"user_name\":\"" + c.getName() + "\"}", true);
	}

	
	public static ResponseEntity<String> successfullEvaluation(){
		return addEntity("{\"success\":\"true\", \"action\":\"evaluation\"}", true);
	}
	
	public static ResponseEntity<String> noSuchNotification(){
		return addEntity("{\"success\":\"false\", \"action\":\"request\", \"cause\":\"no such notification found\"}", false);
	}
	
	public static ResponseEntity<String> successfullySent(){
		return addEntity("{\"success\":\"true\", \"action\":\"send\"}", true);
	}
	
	public static ResponseEntity<String> successfullyAccepted(Notification n, String m){
		return addEntity("{\"success\":\"true\", \"action\":\"accept\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}", true);
	}
	
	public static ResponseEntity<String> successfullyDenied(Notification n, String m){
		return addEntity("{\"success\":\"true\", \"action\":\"deny\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}", true);
	}


	
	//User Check Helpers
	
	public static ResponseEntity<String> checkUserSuccess(CracUser user){
		return addEntity("{\"success\":\"true\", \"action\":\"check_for_existence\", \"user\":\"" + user.getId() + "\",\"name\":\"" + user.getName() + "\"}", true);
	}
	
	public static ResponseEntity<String> successFullAction(String msg){
		return addEntity("{\"success\":\"true\" , \"msg\":\""+msg+"\"}", true);
	}

	
	//Boot_Mode Helpers
	
	public static ResponseEntity<String> bootSuccess(){
		return addEntity("{\"success\":\"true\", \"action\":\"boot\"}", true);
	}
	
	public static ResponseEntity<String> alreadyBooted(){
		return addEntity("{\"success\":\"false\", \"action\":\"boot\", \"cause\":\"already booted\"}", true);
	}
	
	public static ResponseEntity<String> bootOff(){
		return addEntity("{\"success\":\"false\", \"action\":\"boot\", \"cause\":\"bootMode is off\"}", true);
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
				+ "\"user\":\""+user.getName()+"\", \"token\":\""+token.getCode()+"\", \"roles\":"+roles+"}", true);
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
				+ ", \"user\":\""+user.getName()+"\", \"token\":\""+token.getCode()+"\", \"roles\":"+roles+"}", true);
	}
	
	public static ResponseEntity<String> tokenDestroySuccess(CracUser user){
		return addEntity("{\"success\":\"true\", \"action\":\"destroy_token\", \"id\":\""+user.getId()+"\", "
				+ "\"user\":\""+user.getName()+"\"}", true);
	}
	
	public static ResponseEntity<String> tokenDestroyFailure(CracUser user){
		return addEntity("{\"success\":\"false\", \"action\":\"destroy_token\", \"cause\":\"no token available\"}", true);
	}
	
	public static ResponseEntity<String> print(String msg){
		return addEntity(msg, true);
	}
	
	private static ResponseEntity<String> addEntity(String response, boolean status){
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(status){
    		return ResponseEntity.ok().headers(headers).body(response);
        }else{
    		return ResponseEntity.badRequest().headers(headers).body(response);
        }
        
				
	}
		
}
