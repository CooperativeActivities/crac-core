package crac.models.komet.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import crac.exception.KometMappingException;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.relation.MappingCompetenceAreaCompetence;
import crac.module.matching.interfaces.SyncableCrac;
import crac.module.matching.interfaces.SyncableKomet;

import org.springframework.data.repository.CrudRepository;


/**
 * The persistent class for the tx_exabiscompetences_descriptors_topicid_mm database table.
 * @author David Hondl, Claudia Vojinovic-Peer
 */
@Entity
@Table(name="tx_exabiscompetences_descriptors_topicid_mm")
@IdClass(TxExabiscompetencesDescriptorsTopicidMm.class)
public class TxExabiscompetencesDescriptorsTopicidMm implements SyncableKomet, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="uid_foreign")
	private int uidForeign;

	@Id
	@Column(name="uid_local")
	private int uidLocal;

	public TxExabiscompetencesDescriptorsTopicidMm() {
	}

	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map) throws KometMappingException{
		CompetenceAreaDAO competenceAreaDAO = (CompetenceAreaDAO) map.get(CompetenceAreaDAO.class);
		CompetenceDAO competenceDAO = (CompetenceDAO) map.get(CompetenceDAO.class);
		
		MappingCompetenceAreaCompetence mac = new MappingCompetenceAreaCompetence();
		
	    CompetenceArea ca = competenceAreaDAO.findOne((long) this.uidForeign);
	    Competence c = competenceDAO.findOne((long) this.uidLocal);
		
	    if (ca == null || c == null) 
	    	throw new KometMappingException();
	    
	    if (ca.getMappedCompetences() == null)
	    	ca.setMappedCompetences(new HashSet<Competence>());
	    
	    Set<Competence> mappedCompetences = (Set<Competence>) ca.getMappedCompetences();
	    mappedCompetences.add(c);
		
	    mac.setCompetenceArea(ca);
	    mac.setCompetence(c);
	    
		return mac;
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

	@Override
	public boolean isValid() {
		return this.uidForeign > 0 && this.uidLocal > 0;

	}
	
	@Override
	public int getUid() {
		return 0;
	}

}