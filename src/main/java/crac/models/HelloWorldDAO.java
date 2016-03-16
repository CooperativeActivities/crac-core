package crac.models;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HelloWorldDAO extends CrudRepository<HelloWorld, Long> {


}
