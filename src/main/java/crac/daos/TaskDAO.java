package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Task;

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long>{

}
