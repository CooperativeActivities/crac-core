package crac.utility;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
		return addEntity("{\"success\":\"true\", \"action\":\"create\", \"user\":\"" + u.getId() + "\",\"name\":\"" + u.getName() + "\"}");
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
	
	private static ResponseEntity<String> addEntity(String response){
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok().headers(headers).body(response);
				
	}
	
}
