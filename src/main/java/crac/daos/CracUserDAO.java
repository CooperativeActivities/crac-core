package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.CracUser;

/**
 * Spring Data CrudRepository for the cracUser entity.
 */

@Transactional
public interface CracUserDAO extends CrudRepository<CracUser, Long>{
	 public CracUser findByName(String name);
	 public CracUser findByNameAndPassword(String name, String password);
}
