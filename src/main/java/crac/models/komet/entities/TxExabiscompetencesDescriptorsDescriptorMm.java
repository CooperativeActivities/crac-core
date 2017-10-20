package crac.models.komet.entities;

import java.util.Map;

import javax.persistence.*;

import org.springframework.data.repository.CrudRepository;

import crac.exception.KometMappingException;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.entities.Competence;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.module.matching.interfaces.SyncableCrac;
import crac.module.matching.interfaces.SyncableKomet;


/**
 * The persistent class for the tx_exabiscompetences_descriptors_descriptor_mm database table.
 * @author David Hondl
 */
@Entity
@Table(name="tx_exabiscompetences_descriptors_descriptor_mm")
public class TxExabiscompetencesDescriptorsDescriptorMm implements SyncableKomet {

	@Id
	private int uid;

	private int sorting;

	private int strength;

	private String tablenames;

	@Column(name="type_id")
	private int typeId;

	@Column(name="uid_foreign")
	private int uidForeign;

	@Column(name="uid_local")
	private int uidLocal;

	@Column(name="uni_direction")
	private byte uniDirection;

	public TxExabiscompetencesDescriptorsDescriptorMm() {
	}
	
	public CompetenceRelationship mapToCompetence(CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO, CompetenceDAO competenceDAO){
		CompetenceRelationship c = new CompetenceRelationship();
		c.setId(this.uid);
		c.setType(competenceRelationshipTypeDAO.findOne((long) this.typeId));
		c.setCompetence1(competenceDAO.findOne((long) this.uidForeign));
		c.setCompetence2(competenceDAO.findOne((long) this.uidLocal));
		c.setUniDirection(false);
		return c;
	}
	
	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map) throws KometMappingException {
		CompetenceDAO competenceDAO = (CompetenceDAO) map.get(CompetenceDAO.class);
		CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO = (CompetenceRelationshipTypeDAO) map.get(CompetenceRelationshipTypeDAO.class);
		CompetenceRelationship c = new CompetenceRelationship();
		c.setId(this.uid);
		
		CompetenceRelationshipType crt = competenceRelationshipTypeDAO.findOne((long) this.typeId);
		Competence c1 = competenceDAO.findOne((long) this.uidForeign);
		Competence c2 = competenceDAO.findOne((long) this.uidLocal);
		
		if(crt == null || c1 == null || c2 == null){
			throw new KometMappingException();
		}
		
		c.setType(crt);
		c.setCompetence1(c1);
		c.setCompetence2(c2);
		c.setUniDirection(false);
		
		return c;
	}

	public boolean isValid(){
		return this.uidForeign > 0 && this.uidLocal > 0;
	}

	
	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getSorting() {
		return this.sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public int getStrength() {
		return this.strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public String getTablenames() {
		return this.tablenames;
	}

	public void setTablenames(String tablenames) {
		this.tablenames = tablenames;
	}

	public int getTypeId() {
		return this.typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getUidForeign() {
		return this.uidForeign;
	}

	public void setUidForeign(int uidForeign) {
		this.uidForeign = uidForeign;
	}

	public int getUidLocal() {
		return this.uidLocal;
	}

	public void setUidLocal(int uidLocal) {
		this.uidLocal = uidLocal;
	}

	public byte getUniDirection() {
		return this.uniDirection;
	}

	public void setUniDirection(byte uniDirection) {
		this.uniDirection = uniDirection;
	}

}