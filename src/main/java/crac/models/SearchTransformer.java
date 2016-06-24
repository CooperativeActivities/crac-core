package crac.models;

import crac.elastic.ElasticPerson;
import crac.elastic.ElasticTask;

public class SearchTransformer {
	
	public ElasticTask transformTask(Task originalTask){
		return transformTaskInternDirectly(originalTask);
	}
	
	public ElasticPerson transformUser(CracUser originalUser){
		return transformUserInternDirectly(originalUser);
	}

	private ElasticPerson transformUserInternDirectly(CracUser c){
		return new ElasticPerson(c.getId(), c.getName(), /*c.getEmail(), c.getLastName(), c.getFirstName(), c.getBirthDate(), c.getStatus(), c.getPhone(),
				c.getAddress(), c.getRole(), c.getCreatedTasks(), c.getCreatedProjects(), c.getCreatedCompetences(), c.getCreatedGroups(),*/ c.getCompetences(),
				c.getLikes(), c.getDislikes()/*, c.getOpenTasks(), c.getResponsibleForTasks(), c.getFollowingTasks(), c.getGroups(), c.getUserImage()*/);
	}
	
	private ElasticTask transformTaskInternDirectly(Task t){
		return new ElasticTask(""+t.getId(), /*t.getSuperTask(), t.getChildTasks(),
				*/t.getNeededCompetences(), /*t.getSignedUsers(), t.getResponsibleUsers(), 
				t.getFollowingUsers(), */t.getName(), t.getDescription()/*, t.getLocation(), t.getStartTime(), 
				t.getEndTime(), t.getUrgency(), t.getAmountOfVolunteers(), t.getFeedback(),
				t.getCreator(), t.getAttachments(), t.getComments()*/);
	}

}
