package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.User;

@Transactional
public interface UserDAO extends CrudRepository<User, Long>{

}
