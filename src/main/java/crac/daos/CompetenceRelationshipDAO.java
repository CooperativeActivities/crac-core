package crac.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.Task;
import crac.models.relation.CompetenceRelationship;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
@Component
public interface CompetenceRelationshipDAO extends CrudRepository<CompetenceRelationship, Long> {
	public List<CompetenceRelationship> findByCompetence1(Competence competence1);
	public List<CompetenceRelationship> findByCompetence2(Competence competence2);
}
