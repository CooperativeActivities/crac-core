package crac.components.storage;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.storage.AugmentedSimpleCompetenceCollection;
import crac.models.storage.SimpleCompetence;
import crac.models.storage.SimpleCompetenceRelation;

@Component
@Scope("singleton")
public class CompetenceStorage {
	
	@Autowired
	private CompetenceDAO competenceDAO;
	
	@Autowired
	private CompetenceRelationshipDAO compRelDAO;

	private boolean synced = false;
	private boolean cached = false;

	private HashMap<Long, SimpleCompetence> competences = new HashMap<Long, SimpleCompetence>();
	private ArrayList<AugmentedSimpleCompetenceCollection> cache = new ArrayList<>();

	public boolean copy() {
		
		for (Competence c : competenceDAO.findAll()) {
			this.competences.put(c.getId(), new SimpleCompetence(c));
		}

		for (CompetenceRelationship cr : compRelDAO.findAll()) {
			SimpleCompetence c1 = this.competences.get(cr.getCompetence1().getId());
			SimpleCompetence c2 = this.competences.get(cr.getCompetence2().getId());
			if (cr.getType() != null) {
				c1.addRelation(new SimpleCompetenceRelation(c2, cr.getType().getDistanceVal()));
				c2.addRelation(new SimpleCompetenceRelation(c1, cr.getType().getDistanceVal()));
			}
		}

		this.synced = true;
		this.cached = false;

		// this.cache(competenceDAO);

		return true;
	}

	public boolean clearCache() {
		return this.clear();

	}

	public boolean synchronize() {
		copy();
		cache();
		return true;
	}

	public boolean cache() {

		AugmenterUnit au = new AugmenterUnit(this);

		for (SimpleCompetence asc : this.competences.values()) {
			this.cache.add(au.augment(asc));
		}

		this.cached = true;

		return true;
	}

	private CompetenceStorage() {
	}

	public HashMap<Long, SimpleCompetence> getCompetences() {
		if (this.synced) {
			return this.competences;
		} else {
			return null;
		}
	}

	public SimpleCompetence getCompetence(Long key) {
		if (this.synced) {
			return this.competences.get(key);
		} else {
			return null;
		}
	}

	public double getCompetenceSimilarity(Competence assigned, Competence target) {
		if (!this.synced || !this.cached) {
			return 0;
		} else {
			return this.getCollectionIntern(assigned.getId()).compare(this.getCollectionIntern(target.getId()));
		}
	}

	public AugmentedSimpleCompetenceCollection getCollection(Competence c) {
		return this.getCollectionIntern(c.getId());
	}

	public AugmentedSimpleCompetenceCollection getCollection(Long id) {
		return this.getCollectionIntern(id);
	}

	public ArrayList<AugmentedSimpleCompetenceCollection> getCollections(Task t) {
		ArrayList<AugmentedSimpleCompetenceCollection> collections = new ArrayList<AugmentedSimpleCompetenceCollection>();
		for (CompetenceTaskRel ctr : t.getMappedCompetences()) {
			collections.add(this.getCollectionIntern(ctr.getCompetence().getId()));
		}
		return collections;
	}

	public ArrayList<AugmentedSimpleCompetenceCollection> getCollections(CracUser u) {
		ArrayList<AugmentedSimpleCompetenceCollection> collections = new ArrayList<AugmentedSimpleCompetenceCollection>();
		for (UserCompetenceRel ucr : u.getCompetenceRelationships()) {
			collections.add(this.getCollectionIntern(ucr.getCompetence().getId()));
		}
		return collections;
	}

	public boolean isSynced() {
		return this.synced;
	}

	public boolean isCached() {
		return this.cached;
	}

	private AugmentedSimpleCompetenceCollection getCollectionIntern(Long id) {
		if (this.cached) {
			for (AugmentedSimpleCompetenceCollection single : this.cache) {
				if (single.getMain().getConcreteComp().getId() == id) {
					return single;
				}
			}
		}
		return null;
	}

	private boolean clear() {
		competences = new HashMap<Long, SimpleCompetence>();
		cached = false;
		return true;
	}

	public CompetenceDAO getCompetenceDAO() {
		return competenceDAO;
	}

	public void setCompetenceDAO(CompetenceDAO competenceDAO) {
		this.competenceDAO = competenceDAO;
	}
	
}
