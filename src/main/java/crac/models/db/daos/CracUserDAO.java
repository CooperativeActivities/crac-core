package crac.models.db.daos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CracUser;

/**
 * Spring Data CrudRepository for the cracUser entity.
 */

@Transactional
public interface CracUserDAO extends CrudRepository<CracUser, Long>{
	 public CracUser findByName(String name);
	 public CracUser findByIdAndName(long id, String name);
	 public CracUser findByNameAndPassword(String name, String password);
	 public List<CracUser> findByLastName(String lastName);
	 public List<CracUser> findByFirstName(String firstName);
	 public List<CracUser> findByFirstNameAndLastName(String firstName, String lastName);
}
