package crac.models;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long>{

}
