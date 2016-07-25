package crac.daos;


import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.TaskParticipationType;
import crac.models.CracUser;
import crac.models.Task;
import crac.relationmodels.UserTaskRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserTaskRelDAO extends CrudRepository<UserTaskRel, Long> {
	public UserTaskRel findByUserAndTask(CracUser user, Task task);
	public Set<UserTaskRel> findByParticipationTypeAndTask(TaskParticipationType participationTyp, Task task);
}
