package crac.components.utility;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceRelationshipType;

public class UpdateEntitiesHelper {

	public static void checkAndUpdateUser(CracUser old, CracUser updated){
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		if (updated.getPassword() != null) {
			old.setPassword(bcryptEncoder.encode(updated.getPassword()));
		}
		
		if (updated.getName() != null) {
			old.setName(updated.getName());
		}
		if (updated.getFirstName() != null) {
			old.setFirstName(updated.getFirstName());
		}
		if (updated.getLastName() != null) {
			old.setLastName(updated.getLastName());
		}
		if (updated.getBirthDate() != null) {
			old.setBirthDate(updated.getBirthDate());
		}
		if (updated.getEmail() != null) {
			old.setEmail(updated.getEmail());
		}
		if (updated.getAddress() != null) {
			old.setAddress(updated.getAddress());
		}
		if (updated.getPhone() != null) {
			old.setPhone(updated.getPhone());
		}
		if (updated.getStatus() != null) {
			old.setStatus(updated.getStatus());
		}

	}
	/*
	public static void checkAndUpdateTask(Task old, Task updated){
		if(updated.getName() != null){
			old.setName(updated.getName());
		}
		
		if(updated.getDescription() != null){
			old.setDescription(updated.getDescription());
		}

		if(updated.getLocation() != null){
			old.setLocation(updated.getLocation());
		}

		if(updated.getStartTime() != null){
			old.setStartTime(updated.getStartTime());
		}
		
		if(updated.getEndTime() != null){
			old.setEndTime(updated.getEndTime());
		}

		if(updated.getUrgency() > 0){
			old.setUrgency(updated.getUrgency());
		}
		
		if(updated.getMaxAmountOfVolunteers() >= 0){
			old.setMaxAmountOfVolunteers(updated.getMaxAmountOfVolunteers());
		}
		
		if(updated.getMinAmountOfVolunteers() >= 0){
			old.setMinAmountOfVolunteers(updated.getMinAmountOfVolunteers());
		}
		
		if(updated.getFeedback() != null){
			old.setFeedback(updated.getFeedback());
		}
		
		if(updated.getTaskState() != null){
			old.setTaskState(updated.getTaskState());
		}
		
	}*/
	
	public static void checkAndUpdateCompetence(Competence old, Competence updated){
		if (updated.getName() != null) {
			old.setName(updated.getName());
		}
		if (updated.getDescription() != null) {
			old.setDescription(updated.getDescription());
		}
		if (updated.getPermissionType() != null) {
			old.setPermissionType(updated.getPermissionType());
		}

	}
	
	public static void checkAndUpdateMaterial(Material old, Material updated){
		if (updated.getName() != null) {
			old.setName(updated.getName());
		}
		if (updated.getDescription() != null) {
			old.setDescription(updated.getDescription());
		}
		if (updated.getQuantity() > 0) {
			old.setQuantity(updated.getQuantity());
		}

	}


	public static void checkAndUpdateCompetenceRelType(CompetenceRelationshipType old, CompetenceRelationshipType updated){
		if (updated.getName() != null) {
			old.setName(updated.getName());
		}
		if (updated.getDescription() != null) {
			old.setDescription(updated.getDescription());
		}
		if (updated.getDistanceVal() >= 0) {
			old.setDistanceVal(updated.getDistanceVal());
		}

	}
/*
	public static void checkAndUpdateEvaluation(Evaluation old, Evaluation updated){
		
		if(updated.getFeedback() != null){
			old.setFeedback(updated.getFeedback());
		}
		
		old.setLikeValOrganisation(updated.getLikeValOrganisation());
		old.setLikeValOthers(updated.getLikeValOthers());
		old.setLikeValTask(updated.getLikeValTask());
	}
*/	
}
