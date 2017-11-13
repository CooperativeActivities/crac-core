package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.relation.MappingCompetenceAreaCompetence;

@Transactional
public interface MappingCompetenceAreaCompetenceDAO extends CrudRepository<MappingCompetenceAreaCompetence, Long> {
	
}
