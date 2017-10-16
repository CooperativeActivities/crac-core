package crac.module.matching.superclass;

import crac.module.factories.WorkerFactory;
import crac.module.utility.CracUtility;
import lombok.Data;

@Data
public abstract class Worker {
	
	private String workerId;
	
	private WorkerFactory wf;
	
	public Worker(){
		this.workerId = CracUtility.randomString(20);
		System.out.println("_______________________");
		System.out.println("Worker with ID "+this.workerId+" running!");
		System.out.println("_______________________");
	}
	
	public abstract Object run();
	
    public abstract void injectParam(Object param);

}
