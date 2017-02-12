package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Comment;
import crac.models.Material;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface MaterialDAO extends CrudRepository<Material, Long> {
	public Material findByName(String name);
}
