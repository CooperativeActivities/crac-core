package crac.models.db.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.ConcreteTaskState;
import crac.models.db.entities.Task;

/**
 * Spring Data CrudRepository for the task entity.
 * @author David Hondl
*/

@Transactional
public interface TaskService extends CrudRepository<Task, Long> {
	public Task findByName(String name);

	public List<Task> findMultipleByNameLike(String name);
			
	public List<Task> findBySuperTaskNullAndTaskStateNot(ConcreteTaskState taskState);

	public List<Task> findBySuperTaskNullAndTaskState(ConcreteTaskState taskState);

	public List<Task> findByTaskStateNot(ConcreteTaskState taskState);
		
	/**
	 * Custom query that queries for tasks that are in task-state PUBLISHED or STARTED and contains given string in the name or description
	 * @param s
	 * @return List<Task>
	 */
	@Query("select t from Task t where (t.taskState = 1 or t.taskState = 2) and (t.name like %:s% or t.description like %:s%)")
	public List<Task> selectNameContainingTasks(@Param("s") String s);
	
	/**
	 * Custom query that queries for tasks that are in task-state PUBLISHED or STARTED and are not of task-type ORGANISATIONAL
	 * @return List<Task>
	 */
	@Query("select t from Task t where ((t.taskState = 1 or t.taskState = 2) and not t.taskType = 0)")
	public List<Task> selectMatchableTasksSimple();

	/**
	 * Custom query that queries for tasks that are in task-state PUBLISHED or STARTED
	 * @return List<Task>
	 */
	@Query("select t from Task t where (t.taskState = 1 or t.taskState = 2)")
	public List<Task> selectSearchableTasks();

}
