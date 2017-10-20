package crac.models.db.daos;


import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CompetenceArea;


/**
 * Spring Data CrudRepository for the competence-area entity.
 * @author David Hondl
*/

@Transactional
public interface CompetenceAreaDAO extends CrudRepository<CompetenceArea, Long> {
	public List<CompetenceArea> findByDeprecatedNot(boolean deprecated);
}