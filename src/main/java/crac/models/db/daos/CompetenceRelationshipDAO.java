package crac.models.db.daos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.relation.CompetenceRelationship;


/**
 * Spring Data CrudRepository for the competence-relationship entity.
 * @author David Hondl
*/
@Transactional
@Component
public interface CompetenceRelationshipDAO extends CrudRepository<CompetenceRelationship, Long> {
	public List<CompetenceRelationship> findByCompetence1(Competence competence1);
	public List<CompetenceRelationship> findByCompetence2(Competence competence2);
}
