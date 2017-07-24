package crac.models.db.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;

/**
 * Spring Data CrudRepository for the task entity.
 */

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long> {
	public Task findByName(String name);

	public List<Task> findMultipleByNameLike(String name);
		
	public List<Task> findBySuperTaskNull();
	
	public List<Task> findBySuperTaskNullAndTaskStateNot(TaskState taskState);

	public List<Task> findBySuperTaskNullAndTaskState(TaskState taskState);

	public List<Task> findByTaskStateNot(TaskState taskState);
	
	public List<Task> findByTaskStateOrTaskStateAndTaskTypeOrTaskType(TaskState taskState1, TaskState taskState2, TaskType type1, TaskType type2);

	public List<Task> findByTaskState(TaskState taskState);
	
	//@Query("select t from Task t inner join t.userRelationships ur where ur.user != :u and t.taskState = :s1 or t.taskState = :s2 and t.taskType = :t1 or t.taskType = :t2")
	//@Query("select t from Task t left join t.userRelationships ur where ((t.taskState = 1 or t.taskState = 2) and not t.taskType = 0) and ((ur.user = :u and not ur.participationType = 0) or (ur.user is null))")
	@Query("select t from Task t where ((t.taskState = 1 or t.taskState = 2) and not t.taskType = 0))")
	//public List<Task> selectMatchableTasks(@Param("s1") TaskState state1, @Param("s2") TaskState state2, @Param("t1") TaskType type1, @Param("t2") TaskType type2);
	public List<Task> selectMatchableTasksSimple();
	//public List<Task> selectMatchableTasks();
	
}
