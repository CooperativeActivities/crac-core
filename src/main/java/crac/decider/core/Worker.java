package crac.decider.core;

import crac.notifier.NotificationHelper;

public abstract class Worker {
	
	private String workerId;
	
	public Worker(){
		this.workerId = NotificationHelper.randomString(20);
		System.out.println("_______________________");
		System.out.println("Worker with ID "+this.workerId+" running!");
		System.out.println("_______________________");

	}
	
	public String getWorkerId(){
		return this.workerId;
	}

}
