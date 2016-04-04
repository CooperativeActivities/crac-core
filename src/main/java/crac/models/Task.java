package crac.models;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
@Table(name = "tasks")
public class Task {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Autowired
	@Column(name="task_id")
    private long id;
	

	@ManyToMany(mappedBy="openTasks")
    private Set<CracUser> users;
    
    @Autowired
    @NotNull
    private String name;
    
    @Autowired
    @NotNull
    private String description;

    @Autowired
    @ManyToOne
    @JoinColumn(name = "creator_id")  
    private CracUser creator;

	public Task() {
		this.name = "";
	}

	public Task(String name, String description) {
		this.name = name;
		this.description = description;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<CracUser> getUsers() {
		return users;
	}



	public void setUsers(Set<CracUser> users) {
		this.users = users;
	}



	public CracUser getCreator() {
		return creator;
	}



	public void setCreator(CracUser creator) {
		this.creator = creator;
	}
    
    

}
