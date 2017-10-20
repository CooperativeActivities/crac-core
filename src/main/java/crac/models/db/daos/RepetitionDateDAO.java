package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.relation.RepetitionDate;


/**
 * Spring Data CrudRepository for the repetition-date entity.
 * @author David Hondl
*/
@Transactional
public interface RepetitionDateDAO extends CrudRepository<RepetitionDate, Long> {
}
