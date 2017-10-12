package crac.models.db.daos;


import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.ConcreteTaskState;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserTaskRelDAO extends CrudRepository<UserTaskRel, Long> {
	public Set<UserTaskRel> findByUserAndTask(CracUser user, Task task);
	public UserTaskRel findByUserAndTaskAndParticipationType(CracUser user, Task task, TaskParticipationType participationTyp);
	public Set<UserTaskRel> findByUserAndParticipationType(CracUser user, TaskParticipationType participationTyp);
	public UserTaskRel findByUserAndTaskAndParticipationTypeNot(CracUser user, Task task, TaskParticipationType participationTyp);
	public Set<UserTaskRel> findByParticipationTypeAndTask(TaskParticipationType participationTyp, Task task);
	public Set<UserTaskRel> findByUser (CracUser user);
	
	@Query("select r from UserTaskRel r where r.user = :u and r.evaluation.filled = false")
	public Set<UserTaskRel> selectRelByNotFilled(@Param("u") CracUser c);
	
	@Query("select r from UserTaskRel r where r.user = :u and r.participationType = :pt and r.task.taskState = :ts")
	public Set<UserTaskRel> selectRelByUserAndParticipationTypeAndTaskState(@Param("u") CracUser c, @Param("pt") TaskParticipationType pt, @Param("ts") ConcreteTaskState ts);
}
