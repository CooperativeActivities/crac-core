package crac.models.output;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.relation.UserTaskRel;
import lombok.Data;

/**
 * Helper class for output, that contains detailed information about the relation between two users
 * Needed for the TaskDetails-Class
 * @author David Hondl
 *
 */
@Data
public class UserFriendDetails {

	private long id;
	private String name;
	private boolean friend;
	private boolean self;
	private TaskParticipationType participationType;
	private boolean completed;
	
	public UserFriendDetails(CracUser u, boolean friend, UserTaskRel rel) {
		this.id = u.getId();
		this.name = u.getName();
		this.friend = friend;
		this.self = rel.getUser().equals(u);
		this.participationType = rel.getParticipationType();
		this.completed = rel.isCompleted();
	}

}
