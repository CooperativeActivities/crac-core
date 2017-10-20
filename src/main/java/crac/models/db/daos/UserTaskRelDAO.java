package crac.models.db.daos;


import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.ConcreteTaskState;
import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;


/**
 * Spring Data CrudRepository for the user-task-relationship entity.
 * @author David Hondl
*/
@Transactional
public interface UserTaskRelDAO extends CrudRepository<UserTaskRel, Long> {
	public Set<UserTaskRel> findByUserAndTask(CracUser user, Task task);
	public UserTaskRel findByUserAndTaskAndParticipationType(CracUser user, Task task, TaskParticipationType participationTyp);
	public Set<UserTaskRel> findByUserAndParticipationType(CracUser user, TaskParticipationType participationTyp);
	public UserTaskRel findByUserAndTaskAndParticipationTypeNot(CracUser user, Task task, TaskParticipationType participationTyp);
	public Set<UserTaskRel> findByParticipationTypeAndTask(TaskParticipationType participationTyp, Task task);
	public Set<UserTaskRel> findByUser (CracUser user);
	
	/**
	 * Custom query that queries for user-task-relationships that contain a given user and a not-filled evaluation
	 * @param c
	 * @return Set<UserTaskRel>
	 */
	@Query("select r from UserTaskRel r where r.user = :u and r.evaluation.filled = false")
	public Set<UserTaskRel> selectRelByNotFilled(@Param("u") CracUser c);
	
	/**
	 * Custom query that queries for user-task-relationships that contain a given user, a given ParticipationType and a given TaskState
	 * @param c
	 * @param pt
	 * @param ts
	 * @return Set<UserTaskRel>
	 */
	@Query("select r from UserTaskRel r where r.user = :u and r.participationType = :pt and r.task.taskState = :ts")
	public Set<UserTaskRel> selectRelByUserAndParticipationTypeAndTaskState(@Param("u") CracUser c, @Param("pt") TaskParticipationType pt, @Param("ts") ConcreteTaskState ts);
}
