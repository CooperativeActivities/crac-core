package crac.models.db.daos;


import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserTaskRelDAO extends CrudRepository<UserTaskRel, Long> {
	//public UserTaskRel findByUserAndTask(CracUser user, Task task);
	public UserTaskRel findByUserAndTaskAndParticipationType(CracUser user, Task task, TaskParticipationType participationTyp);
	public UserTaskRel findByUserAndTaskAndParticipationTypeNot(CracUser user, Task task, TaskParticipationType participationTyp);
	public Set<UserTaskRel> findByParticipationTypeAndTask(TaskParticipationType participationTyp, Task task);
	public Set<UserTaskRel> findByUser (CracUser user);
}
