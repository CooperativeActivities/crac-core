package crac.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.UserCompetenceRel;
import crac.models.UserTaskRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserTaskRelDAO extends CrudRepository<UserTaskRel, Long> {
	public UserTaskRel findByUserAndTask(CracUser user, Task task);
}
