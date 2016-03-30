package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;

@Transactional
public interface CompetenceDAO extends CrudRepository<Competence, Long>{

}
