package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Task;

/**
 * Spring Data CrudRepository for the task entity.
 */

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long>{
	 public Task findByName(String name);
}
