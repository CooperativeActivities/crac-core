package crac.components.matching;

import crac.components.matching.factories.WorkerFactory;
import crac.models.utility.RandomUtility;

public abstract class Worker {
	
	private String workerId;
	
	private WorkerFactory wf;
	
	public Worker(){
		this.workerId = RandomUtility.randomString(20);
		System.out.println("_______________________");
		System.out.println("Worker with ID "+this.workerId+" running!");
		System.out.println("_______________________");
	}
	
	public abstract Object run();
	
	public String getWorkerId(){
		return this.workerId;
	}

	public void setWf(WorkerFactory wf) {
		this.wf = wf;
	}

	public WorkerFactory getWf() {
		return wf;
	}

}
