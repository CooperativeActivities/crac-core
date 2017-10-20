package crac.models.db.daos;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CracUser;

/**
 * Spring Data CrudRepository for the cracUser entity.
 * @author David Hondl
*/

@Transactional
public interface CracUserDAO extends CrudRepository<CracUser, Long>{
	 public CracUser findByName(String name);
	 public CracUser findByIdAndName(long id, String name);
	 public CracUser findByNameAndPassword(String name, String password);
	 public List<CracUser> findByLastName(String lastName);
	 public List<CracUser> findByFirstName(String firstName);
	 public List<CracUser> findByFirstNameAndLastName(String firstName, String lastName);
	 
	 /**
	  * This custom-query queries for users that either have a matching username or matching firstname + lastname
	  * @param name
	  * @param firstName
	  * @param lastName
	  * @return List<CracUser>
	  */
	 @Query("select u from CracUser u where u.name = :n or (u.firstName = :fn and u.lastName = :ln)")
	 public List<CracUser> queryByNameOrFullname(@Param("n") String name, @Param("fn") String firstName, @Param("ln") String lastName);
}
