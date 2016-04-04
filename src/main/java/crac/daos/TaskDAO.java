package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.CracUser;
import crac.models.Task;

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long>{
	 public Task findByName(String name);
}
