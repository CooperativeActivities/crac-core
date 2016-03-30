package crac.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.persistence.JoinColumn;


import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "users")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Autowired
	@Column(name="user_id")
    private long id;
	
	@NotNull
	@Autowired
	private String name;
	
	@NotNull
	@Autowired
	private String password;
	
	@Autowired
	@OneToMany(mappedBy="creator", cascade=CascadeType.ALL)  
    private Set<Task> created_tasks; 
	
	@Autowired
	@OneToMany(mappedBy="creator", cascade=CascadeType.ALL)  
    private Set<Competence> created_competences; 
	
	@Autowired
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "user_competencies", joinColumns = { @JoinColumn(name = "user_id") },
    inverseJoinColumns = { @JoinColumn(name = "competence_id") })
    private Set<Competence> competencies;
	
	@Autowired
	@ManyToMany(cascade = { CascadeType.ALL })
	@JoinTable(name = "user_tasks", joinColumns = { @JoinColumn(name = "user_id") },
    inverseJoinColumns = { @JoinColumn(name = "task_id") })
    private Set<Task> openTasks;

	public User(String name, String password) {
		this.name = name;
		this.password = password;
		this.competencies = null;
		this.openTasks = null;
	}

	public User() {
		this.name = "";
		this.password = "";
		this.competencies = null;
		this.openTasks = null;
	}

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Competence> getCompetencies() {
		return competencies;
	}

	public void setCompetencies(Set<Competence> competencies) {
		this.competencies = competencies;
	}

	public Set<Task> getOpenTasks() {
		return openTasks;
	}

	public void setOpenTasks(Set<Task> openTasks) {
		this.openTasks = openTasks;
	}

	public Set<Task> getCreated_tasks() {
		return created_tasks;
	}

	public void setCreated_tasks(Set<Task> created_tasks) {
		this.created_tasks = created_tasks;
	}

	public Set<Competence> getCreated_competences() {
		return created_competences;
	}

	public void setCreated_competences(Set<Competence> created_competences) {
		this.created_competences = created_competences;
	}
	
	

}
