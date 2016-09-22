package crac.relationmodels;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.CracUser;

@Entity
@Table(name = "user_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserRelationship {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user1")
	private CracUser c1;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user2")
	private CracUser c2;
	
	private double likeValue;
	
	private boolean friends;

	
	
	public UserRelationship() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CracUser getC1() {
		return c1;
	}

	public void setC1(CracUser c1) {
		this.c1 = c1;
	}

	public CracUser getC2() {
		return c2;
	}

	public void setC2(CracUser c2) {
		this.c2 = c2;
	}

	public double getLikeValue() {
		return likeValue;
	}

	public void setLikeValue(double likeValue) {
		this.likeValue = likeValue;
	}

	public boolean isFriends() {
		return friends;
	}

	public void setFriends(boolean friends) {
		this.friends = friends;
	}
	
	
	
}
