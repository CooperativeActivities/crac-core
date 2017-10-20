package crac.models.db.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * The attachment-entity
 * @author David Hondl
*/

@Entity
@Table(name = "comments")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Comment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private long id;

	@NotNull
	private String name;

	@NotNull
	private String content;
	
	/**
	 * defines a one to many relation with the task-entity
	 */
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "task_id")
	private Task task;
	
	/**
	 * constructors
	 */
	
	public Comment(String name, String content) {
		this.name = name;
		this.content = content;
	}
	
	public Comment(String name) {
		this.name = name;
		this.content = "";
	}
	
	public Comment() {
		this.name = "";
		this.content = "";
	}
	
	/**
	 * getters and setters
	 */

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

}
