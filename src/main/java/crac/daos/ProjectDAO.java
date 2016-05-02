package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Project;

/**
 * Spring Data CrudRepository for the task entity.
 */

@Transactional
public interface ProjectDAO extends CrudRepository<Project, Long> {
	public Project findByName(String name);
}
