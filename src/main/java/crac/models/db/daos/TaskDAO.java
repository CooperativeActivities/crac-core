package crac.models.db.daos;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.enums.ConcreteTaskState;
import crac.models.db.entities.Task;
import crac.module.utility.ElasticConnector;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("singleton")
public class TaskDAO {

	@Autowired
	@Getter
	@Setter
	TaskService taskService;
	
	@Autowired
	@Getter
	@Setter
	private ElasticConnector<Task> ect;

	public void sync(){
		System.out.println("called");
		ect.deleteIndex();
		taskService.findAll().forEach( entity -> ect.indexOrUpdate(entity.getId()+"", entity) );
	}
	
	public <S extends Task> Iterable<S> save(Iterable<S> entities) {
		entities.forEach( entity -> {
			taskService.save(entity);
			ect.indexOrUpdate(entity.getId()+"", entity);
		});	
		return entities;
	}

	public Task findOne(Long id) {
		return taskService.findOne(id);
	}

	public boolean exists(Long id) {
		return taskService.exists(id);
	}

	public Iterable<Task> findAll(Iterable<Long> ids) {
		return taskService.findAll(ids);
	}

	public long count() {
		return taskService.count();
	}

	public void delete(Long id) {
		ect.delete(id+"");
		taskService.delete(id);
	}

	public void delete(Iterable<? extends Task> entities) {
		entities.forEach( entity -> {
			ect.delete(entity.getId()+"");
			taskService.delete(entity);
		});
	}

	public void deleteAll() {
		taskService.deleteAll();
	}

	public <S extends Task> S save(S entity) {	
		ect.indexOrUpdate(entity.getId()+"", entity);
		return taskService.save(entity);
	}

	public Iterable<Task> findAll() {
		return taskService.findAll();
	}

	public void delete(Task entity) {
		ect.delete(entity.getId()+"");	
		taskService.delete(entity);
	}

	public Task findByName(String name) {
		return taskService.findByName(name);
	}

	public List<Task> findMultipleByNameLike(String name) {
		return taskService.findMultipleByNameLike(name);
	}

	public List<Task> findBySuperTaskNullAndTaskStateNot(ConcreteTaskState taskState) {
		return taskService.findBySuperTaskNullAndTaskStateNot(taskState);
	}

	public List<Task> findBySuperTaskNullAndTaskState(ConcreteTaskState taskState) {
		return taskService.findBySuperTaskNullAndTaskState(taskState);
	}

	public List<Task> findByTaskStateNot(ConcreteTaskState taskState) {
		return taskService.findByTaskStateNot(taskState);
	}

	public List<Task> selectNameContainingTasks(String s) {
		return taskService.selectNameContainingTasks(s);
	}

	public List<Task> selectMatchableTasksSimple() {
		return taskService.selectMatchableTasksSimple();
	}

	public List<Task> selectSearchableTasks() {
		return taskService.selectSearchableTasks();
	}
		
}
