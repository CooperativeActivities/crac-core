package crac.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Table(name = "helloworlds")
public class HelloWorld {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Autowired
    private long id;
	  
	@NotNull
	@Autowired
    private String hello;
	  
	@NotNull
	@Autowired
    private String world;
	
	public HelloWorld() { }

	public HelloWorld(long id) { 
	  this.id = id;
	}

    public HelloWorld(String hello, String world) {
        this.hello = hello;
        this.world = world;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getHello() {
		return hello;
	}

	public void setHello(String hello) {
		this.hello = hello;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

    
    
}
