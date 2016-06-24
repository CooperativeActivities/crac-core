package crac.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CompetenceRelationship;
import crac.models.Task;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CompetenceRelationshipDAO extends CrudRepository<CompetenceRelationship, Long> {
	public List<CompetenceRelationship> findByCompetence1In(Competence competence1);
	public List<CompetenceRelationship> findByCompetence2In(Competence competence2);
}
