package crac.models.relation;

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

import crac.models.Competence;
import crac.models.CracUser;

@Entity
@Table(name = "user_competence_relationship")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserCompetenceRel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private long id;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user_id")
	private CracUser user;
	
	@ManyToOne
	@JoinColumn(name = "competence_id")
	private Competence competence;
	
	//-100 - 100
	private int likeValue;
	
	//0 - 100
	private int proficiencyValue;
	
	private boolean selfAssigned;

	public UserCompetenceRel() {
		this.likeValue = 1;
	}
	
	public UserCompetenceRel(CracUser user, Competence competence, int proficiencyValue, int likeValue) {
		this.user = user;
		this.competence = competence;
		this.proficiencyValue = proficiencyValue;
		this.likeValue = likeValue;
		this.selfAssigned = true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CracUser getUser() {
		return user;
	}

	public void setUser(CracUser user) {
		this.user = user;
	}

	public Competence getCompetence() {
		return competence;
	}

	public void setCompetence(Competence competence) {
		this.competence = competence;
	}

	public int getLikeValue() {
		return likeValue;
	}

	public void setLikeValue(int likeValue) {
		this.likeValue = likeValue;
	}

	public int getProficiencyValue() {
		return proficiencyValue;
	}

	public void setProficiencyValue(int proficiencyValue) {
		this.proficiencyValue = proficiencyValue;
	}

	public boolean isSelfAssigned() {
		return selfAssigned;
	}

	public void setSelfAssigned(boolean selfAssigned) {
		this.selfAssigned = selfAssigned;
	}

}
