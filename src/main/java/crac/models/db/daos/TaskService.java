package crac.models.db.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.ConcreteTaskState;
import crac.enums.TaskType;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;

/**
 * Spring Data CrudRepository for the task entity.
 */

@Transactional
public interface TaskService extends CrudRepository<Task, Long> {
	public Task findByName(String name);

	public List<Task> findMultipleByNameLike(String name);
			
	public List<Task> findBySuperTaskNullAndTaskStateNot(ConcreteTaskState taskState);

	public List<Task> findBySuperTaskNullAndTaskState(ConcreteTaskState taskState);

	public List<Task> findByTaskStateNot(ConcreteTaskState taskState);
		
	@Query("select t from Task t where (t.taskState = 1 or t.taskState = 2) and (t.name like %:s% or t.description like %:s%)")
	public List<Task> selectNameContainingTasks(@Param("s") String s);
	
	@Query("select t from Task t where ((t.taskState = 1 or t.taskState = 2) and not t.taskType = 0)")
	public List<Task> selectMatchableTasksSimple();

	@Query("select t from Task t where (t.taskState = 1 or t.taskState = 2)")
	public List<Task> selectSearchableTasks();

}
