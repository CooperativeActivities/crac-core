package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Material;


/**
 * Spring Data CrudRepository for the material entity.
 * @author David Hondl
*/
@Transactional
public interface MaterialDAO extends CrudRepository<Material, Long> {
	public Material findByName(String name);
}
