package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Comment;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CommentDAO extends CrudRepository<Comment, Long> {
	public Comment findByName(String name);
}
