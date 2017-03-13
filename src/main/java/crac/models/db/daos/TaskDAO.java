package crac.models.db.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.enums.TaskState;
import crac.models.db.entities.Competence;
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
	
	public List<Task> findByTaskStateNot(TaskState taskState);
	
}
