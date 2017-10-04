package crac.module.matching.superclass;

import crac.module.matching.factories.NLPWorkerFactory;
import crac.module.storage.CompetenceStorage;
import crac.module.utility.RandomUtility;

public abstract class NLPWorker {

		
		private String workerId;
		
		private NLPWorkerFactory wf;
		
		public NLPWorker(){
			this.workerId = RandomUtility.randomString(20);
			System.out.println("_______________________");
			System.out.println("NLPWorker with ID "+this.workerId+" running!");
			System.out.println("_______________________");
		}
		
		public abstract Object run();
		
		public String getWorkerId(){
			return this.workerId;
		}

		public void setWf(NLPWorkerFactory wf) {
			System.out.println("NLP Worker Factory set in NLPWorker!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			this.wf = wf;
		}

		public NLPWorkerFactory getWf() {
			System.out.println("NLP Worker: get WorkerFactory!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return wf;
		}

}
