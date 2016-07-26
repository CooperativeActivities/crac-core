package crac.daos;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.Task;

/**
 * Spring Data CrudRepository for the task entity.
 */

@Transactional
public interface TaskDAO extends CrudRepository<Task, Long> {
	public Task findByName(String name);

	public List<Task> findMultipleByNameLike(String name);
	
	public List<Task> findByNeededCompetencesIn(Set<Competence> userCompetences);
	
	public List<Task> findBySuperTaskNull();
}
