package crac.models.db.relation;

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

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import lombok.Data;

@Data
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
	@Column(name = "like_value")
	private int likeValue;
	
	//0 - 100
	@Column(name = "proficiency_value")
	private int proficiencyValue;
	
	@Column(name = "self_assigned")
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

}
