package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Comment;


/**
 * Spring Data CrudRepository for the comment entity.
 * @author David Hondl
*/
@Transactional
public interface CommentDAO extends CrudRepository<Comment, Long> {
	public Comment findByName(String name);
}
