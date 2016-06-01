package crac.models;

import crac.elastic.ElasticTask;

public class SearchTransformer {
	
	public ElasticTask transformTask(Task originalTask){
		return transformTaskInternDirectly(originalTask);
	}
	
	private ElasticTask transformTaskInternDirectly(Task t){
		return new ElasticTask(t.getId(), t.getSuperTask(), t.getChildTasks(), t.getSuperProject(), 
				t.getNeededCompetences(), t.getSignedUsers(), t.getResponsibleUsers(), 
				t.getFollowingUsers(), t.getName(), t.getDescription(), t.getLocation(), t.getStartTime(), 
				t.getEndTime(), t.getUrgency(), t.getAmountOfVolunteers(), t.getFeedback(),
				t.getCreator(), t.getAttachments(), t.getComments());
	}

}
