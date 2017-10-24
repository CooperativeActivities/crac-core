package crac.module.factories;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.PostMatchingConfiguration;
import crac.module.matching.configuration.PreMatchingConfiguration;
import crac.module.matching.superclass.Worker;
import crac.module.storage.CompetenceStorage;
import lombok.Data;

/**
 * A factory that creates worker based on a given type
 * @author David Hondl
 *
 */
@Data
@Component
@Scope("prototype")
public class WorkerFactory {

	@Autowired
	private PreMatchingConfiguration pmc;

	@Autowired
	private MatchingConfiguration mc;

	@Autowired
	private PostMatchingConfiguration pomc;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private UserRelationshipDAO userRelalationshipDAO;

	@Autowired
	private CompetenceStorage cs;

    public <T extends Worker> Worker createWorker(Class<T> type, Object param) {        
        Worker w = BeanUtils.instantiate(type);    
        w.injectParam(param);
        w.setWf(this);
		return w;
	}


}
