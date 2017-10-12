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
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
public class WorkerFactory {

	@Autowired
	@Getter
	@Setter
	private PreMatchingConfiguration pmc;

	@Autowired
	@Getter
	@Setter
	private MatchingConfiguration mc;

	@Autowired
	@Getter
	@Setter
	private PostMatchingConfiguration pomc;

	@Autowired
	@Getter
	@Setter
	private TaskDAO taskDAO;

	@Autowired
	@Getter
	@Setter
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	@Getter
	@Setter
	private CracUserDAO userDAO;

	@Autowired
	@Getter
	@Setter
	private UserRelationshipDAO userRelalationshipDAO;

	@Autowired
	@Getter
	@Setter
	private CompetenceStorage cs;

	public <T extends Worker> Worker createWorker(Class<T> type, Object param) {		
		Worker w = BeanUtils.instantiate(type);	
		w.injectParam(param);
		w.setWf(this);
		return w;
	}


}
