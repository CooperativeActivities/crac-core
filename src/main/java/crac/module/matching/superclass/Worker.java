package crac.module.matching.superclass;

import crac.module.factories.WorkerFactory;
import crac.module.utility.CracUtility;
import lombok.Data;

/**
 * Abstract worker class
 * Used for executing different, separated and complex processes
 * @author David Hondl
 *
 */
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
	
	/**
	 * Method that executes the worker-specific code
	 * May return the outcome of the process or null
	 * @return Object
	 */
	public abstract Object run();
	
	/**
	 * Method that allows the injection of arbitrary paramters into the specific worker
	 * @param param
	 */
    public abstract void injectParam(Object param);

}
