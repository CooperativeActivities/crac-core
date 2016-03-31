package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.CracUser;

@Transactional
public interface CracUserDAO extends CrudRepository<CracUser, Long>{

	 public CracUser findByName(String name);
	
}
