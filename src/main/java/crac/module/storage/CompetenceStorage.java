package crac.module.storage;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import crac.module.matching.helpers.AugmentedSimpleCompetenceCollection;
import crac.module.matching.helpers.SimpleCompetence;
import crac.module.matching.helpers.SimpleCompetenceRelation;
import lombok.Getter;

/**
 * The storage-component, which caches the SimpleCompetences and SimpleCompetenceRelationships, as well as the results of the augmentation-process
 * @author David Hondl
 *
 */
@Component
@Scope("singleton")
public class CompetenceStorage {

	@Getter
	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private CompetenceRelationshipDAO compRelDAO;

	@Getter
	@Value("${crac.print.synchronization.competences}")
	private boolean print;

	@Getter
	@Value("${crac.traversal.steps}")
	private int maxSteps;

	@Getter
	@Value("${crac.traversal.minSimilarity}")
	private double minSimilarity;

	private boolean synced = false;
	private boolean cached = false;

	private HashMap<Long, SimpleCompetence> competences = new HashMap<Long, SimpleCompetence>();
	private ArrayList<AugmentedSimpleCompetenceCollection> cache = new ArrayList<>();

	/**
	 * Copies the competences of the database into the storage and returns, if it was successfull
	 * @return boolean
	 */
	public boolean copy() {

		for (Competence c : competenceDAO.findAll()) {
			this.competences.put(c.getId(), new SimpleCompetence(c));
		}

		for (CompetenceRelationship cr : compRelDAO.findAll()) {
			if (cr.getCompetence1() != null && cr.getCompetence2() != null) {
				SimpleCompetence c1 = this.competences.get(cr.getCompetence1().getId());
				SimpleCompetence c2 = this.competences.get(cr.getCompetence2().getId());
				if (cr.getType() != null) {
					c1.addRelation(new SimpleCompetenceRelation(c2, cr.getType().getDistanceVal()));
					c2.addRelation(new SimpleCompetenceRelation(c1, cr.getType().getDistanceVal()));
				}
			}
		}

		this.synced = true;
		this.cached = false;

		// this.cache(competenceDAO);

		return true;
	}

	/**
	 * Calls the AugmenterUnit and storages the results of the augmentation of all competences
	 * @return boolean
	 */
	public boolean cache() {

		AugmenterUnit au = new AugmenterUnit(this);

		for (SimpleCompetence asc : this.competences.values()) {
			this.cache.add(au.augment(asc));
		}

		this.cached = true;

		return true;
	}

	/**
	 * Synchronizes the storage, using both the copy()- and cache()-method
	 * @return boolean
	 */
	public boolean synchronize() {
		copy();
		cache();
		return true;
	}

	/**
	 * Clears the storage
	 * @return boolean
	 */
	public boolean clearCache() {
		return this.clear();

	}

	private CompetenceStorage() {
	}

	/**
	 * Returns all stored competences
	 * @return HashMap<Long, SimpleCompetence>
	 */
	public HashMap<Long, SimpleCompetence> getCompetences() {
		if (this.synced) {
			return this.competences;
		} else {
			return null;
		}
	}

	/**
	 * Returns target stored competence
	 * @param key
	 * @return SimpleCompetence
	 */
	public SimpleCompetence getCompetence(Long key) {
		if (this.synced) {
			return this.competences.get(key);
		} else {
			return null;
		}
	}

	/**
	 * Returns the similarity between two given competences
	 * @param assigned
	 * @param target
	 * @return double
	 */
	public double getCompetenceSimilarity(Competence assigned, Competence target) {
		if (!this.synced || !this.cached) {
			return 0;
		} else {
			return this.getCollectionIntern(assigned.getId()).compare(this.getCollectionIntern(target.getId()));
		}
	}

	/**
	 * Returns a AugmentedSimpleCompetenceCollection for given competence
	 * @param c
	 * @return AugmentedSimpleCompetenceCollection
	 */
	public AugmentedSimpleCompetenceCollection getCollection(Competence c) {
		return this.getCollectionIntern(c.getId());
	}

	/**
	 * Returns a AugmentedSimpleCompetenceCollection for given competence-id
	 * @param id
	 * @return AugmentedSimpleCompetenceCollection
	 */
	public AugmentedSimpleCompetenceCollection getCollection(Long id) {
		return this.getCollectionIntern(id);
	}

	/**
	 * Returns all collected for the competences of target task
	 * @param t
	 * @return ArrayList<AugmentedSimpleCompetenceCollection>
	 */
	public ArrayList<AugmentedSimpleCompetenceCollection> getCollections(Task t) {
		ArrayList<AugmentedSimpleCompetenceCollection> collections = new ArrayList<AugmentedSimpleCompetenceCollection>();
		for (CompetenceTaskRel ctr : t.getMappedCompetences()) {
			collections.add(this.getCollectionIntern(ctr.getCompetence().getId()));
		}
		return collections;
	}

	/**
	 * Returns all collected for the competences of target user
	 * @param u
	 * @return ArrayList<AugmentedSimpleCompetenceCollection>
	 */
	public ArrayList<AugmentedSimpleCompetenceCollection> getCollections(CracUser u) {
		ArrayList<AugmentedSimpleCompetenceCollection> collections = new ArrayList<AugmentedSimpleCompetenceCollection>();
		for (UserCompetenceRel ucr : u.getCompetenceRelationships()) {
			collections.add(this.getCollectionIntern(ucr.getCompetence().getId()));
		}
		return collections;
	}

	/**
	 * Shows if storage is synced
	 * @return boolean
	 */
	public boolean isSynced() {
		return this.synced;
	}

	/**
	 * Shows if storage is cached
	 * @return boolean
	 */
	public boolean isCached() {
		return this.cached;
	}

	/**
	 * Returns a AugmentedSimpleCompetenceCollection by given id from the cache
	 * @param id
	 * @return AugmentedSimpleCompetenceCollection
	 */
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

	/**
	 * Clears the cache
	 * @return boolean
	 */
	private boolean clear() {
		competences = new HashMap<Long, SimpleCompetence>();
		cached = false;
		return true;
	}

}
