package rosita.linkage.tools;

import cdc.gui.StoppableThread;
import cdc.impl.em.EMEstimator;

public class ImputationRuleThread extends StoppableThread{

	private volatile boolean stopScheduled = false;
	
	public void run(){
		try{
			
			
		}catch(Exception ex){
			
		}
	}
	
	public void scheduleStop() {
		// TODO Auto-generated method stub
		stopScheduled = true;
		EMEstimator.cancel = true;
		System.out.println("Cancelled...");
	}
	

}
