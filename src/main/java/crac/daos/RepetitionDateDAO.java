package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.utility.RepetitionDate;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface RepetitionDateDAO extends CrudRepository<RepetitionDate, Long> {
}
