package crac.module.matching.superclass;

import crac.module.matching.factories.NLPWorkerFactory;
import crac.module.utility.CracUtility;

public abstract class NLPWorker {

		
		private String workerId;
		
		private NLPWorkerFactory wf;
		
		public NLPWorker(){
			this.workerId = CracUtility.randomString(20);
			System.out.println("_______________________");
			System.out.println("NLPWorker with ID "+this.workerId+" running!");
			System.out.println("_______________________");
		}
		
		public abstract Object run();
		
		public String getWorkerId(){
			return this.workerId;
		}

		public void setWf(NLPWorkerFactory wf) {
			this.wf = wf;
		}

		public NLPWorkerFactory getWf() {
			return wf;
		}

}
