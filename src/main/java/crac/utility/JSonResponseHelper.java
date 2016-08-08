package crac.utility;

import org.springframework.http.ResponseEntity;

import crac.enums.TaskState;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.notifier.Notification;
import crac.relationmodels.CompetenceRelationshipType;
import crac.relationmodels.UserTaskRel;

public class JSonResponseHelper {
	
	//Creation Helpers
		
	public static ResponseEntity<String> successFullyCreated(CracUser u){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"create\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyCreated(Competence c){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"create\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyCreated(Task t){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"create\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyCreated(CompetenceRelationshipType crt){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"create\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}

	public static ResponseEntity<String> successFullyCreated(Evaluation e){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"create\", \"evaluation\":\"" + e.getId() + "\"}");
	}

	//Delete Helpers
	
	public static ResponseEntity<String> successFullyDeleted(CracUser u){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"delete\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(Competence c){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"delete\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(Task t){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"delete\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyDeleted(CompetenceRelationshipType crt){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"delete\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}

	
	//Update Helpers
	
	public static ResponseEntity<String> successFullyUpdated(CracUser u){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"update\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(Competence c){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"update\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(Task t){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"update\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyUpdated(CompetenceRelationshipType crt){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"update\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
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
	
	//No-Result Helpers

	public static ResponseEntity<String> emptyData(){
		return ResponseEntity.ok().body("{\"success\":\"false\", \"error\":\"no-data\", \"cause\":\"request returned empty data\"}");
	}

	
	//Task State-Change Helpers
	
	public static ResponseEntity<String> successTaskStateChanged(Task t, TaskState ts){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"state_change\", \"state\":\""+ts.toString()+"\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> stateNotAvailable(String name){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"error\":\"bad_request\", \"cause\":\"There is no such state "+name+"\"}");
	}
	
	// Assign Helpers
	
	public static ResponseEntity<String> successFullyAssigned(CracUser u){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"assign\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(Competence c){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"assign\", \"competence\":\"" + c.getId() + "\",\"name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(Task t){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"assign\", \"task\":\"" + t.getId() + "\",\"name\":\"" + t.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(CompetenceRelationshipType crt){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"assign\", \"competence_relationship_type\":\"" + crt.getId() + "\",\"name\":\"" + crt.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successFullyAssigned(UserTaskRel utr){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"assign\", \"user_task_relationship\":\"" + utr.getId() + "\",\"user\":\"" + utr.getUser().getName() + "\",\"task\":\"" + utr.getTask().getName() + "\"}");
	}

	//Notification Helpers
	
	public static ResponseEntity<String> successfullFriendRequest(CracUser c){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"friend_request\", \"receiver\":\"" + c.getId() + "\",\"user_name\":\"" + c.getName() + "\"}");
	}
	
	public static ResponseEntity<String> successfullEvaluation(){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"evaluation\"}");
	}
	
	public static ResponseEntity<String> noSuchNotification(){
		return ResponseEntity.badRequest().body("{\"success\":\"false\", \"action\":\"request\", \"cause\":\"no such notification found\"}");
	}
	
	public static ResponseEntity<String> successfullySent(){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"send\"}");
	}
	
	public static ResponseEntity<String> successfullyAccepted(Notification n, String m){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"accept\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}");
	}
	
	public static ResponseEntity<String> successfullyDenied(Notification n, String m){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"deny\", \"notification\":"+n.toJSon()+", \"message\":\""+m+"\"}");
	}


	
	//User Check Helpers
	
	public static ResponseEntity<String> checkUserSuccess(CracUser user){
		return ResponseEntity.ok().body("{\"success\":\"true\", \"action\":\"check_for_existence\", \"user\":\"" + user.getId() + "\",\"name\":\"" + user.getName() + "\"}");
	}
	
}
