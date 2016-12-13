package rosita.linkage.tools.ir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import rosita.linkage.FRILLinker;
import rosita.linkage.tools.ImputationRule;
import cdc.configuration.ConfiguredSystem;
import cdc.datamodel.DataRow;
import cdc.gui.StoppableThread;
import cdc.utils.Log;
import cdc.utils.RJException;

public class IRControl extends StoppableThread {
	
	private class PollingThread extends Thread {
		private ConfiguredSystem system;
		public PollingThread(ConfiguredSystem system) {
			this.system = system;
		}
		public void run() {
			
			this.system = null;
		}
	}
	
	//private Animation animation;
	//private volatile JoinInfoPanel info;
	private ConfiguredSystem system;
	private volatile boolean stopped = false;
	private long t1;
	private long t2;
	private int n;
	
	private ArrayList<ImputationRule> ImputationRuleSet;
	
	public IRControl(ConfiguredSystem system, ArrayList<ImputationRule> parImputationRuleSet ) {
		this.system = system;
		ImputationRuleSet = parImputationRuleSet;
	}
	
	public ArrayList<ImputationRule> getImputationRuleSet(){
		return ImputationRuleSet;
	}
	
	
	public void run() {
		n = 0;
		DataRow row;
		//JFrame frame = null;
		try {
			
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					//info.getProgressBar().setIndeterminate(!system.getJoin().isProgressSupported());
				}
			});
			
			System.gc();
			
			system.getJoin().reset();
			if (system.getJoin().isProgressSupported()) {
				new PollingThread(system).start();
			}
			//system.getResultSaver().reset();
			
			t1 = System.currentTimeMillis();
			System.out.println("Start time: "+t1);
			while ((row = system.getJoin().joinNext()) != null) {
				n++;
				recordARule(row.getData()[0].toString());
				//system.getResultSaver().saveRow(row);
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						//info.incrementJoined();
					}
				});
				if (stopped) {
					break;
				}
				
			}
			//system.getResultSaver().flush();
			//System.out.println("End time: "+t2);
			//system.getResultSaver().close();
			system.getJoin().closeListeners();
			//system.getJoin().close();
			
			t2 = System.currentTimeMillis();
			System.out.println(system.getJoin() + ": Algorithm produced " + n + " joined tuples. Elapsed time: " + (t2 - FRILLinker.t1) + "ms.");

			Log.log(getClass(), system.getJoin() + ": Algorithm produced " + n + " joined tuples. Elapsed time: " + (t2 - t1) + "ms.", 1);
			closeProgress();
			//animation.stopAnimation();
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					if (stopped) {
						Log.log(getClass(), "Linkage was cancelled", 1);
					}
					//MainFrame.main.setCompletedLinkageSummary(system, stopped, t2 - t1, n);
					//JOptionPane.showMessageDialog(MainFrame.main, Utils.getSummaryMessage(system, stopped, t2 - t1, n));
				}

			});
			
		} catch (RJException e) {
			//JXErrorDialog.showDialog(MainFrame.main, "Error while joining data", e);
			closeProgress();
		} catch (IOException e) {
			//JXErrorDialog.showDialog(MainFrame.main, "Error while joining data", e);
			closeProgress();
		} catch (Exception e) {
			//JXErrorDialog.showDialog(MainFrame.main, "Error while joining data", e);
			closeProgress();
			e.printStackTrace();
		}
		

		
		system.getJoin().setCancelled(false);
		//info = null;
		system = null;
	}
	
	private void recordARule(String parMatch){
		for (int t=0;t<ImputationRuleSet.size();t++){
			if(ImputationRuleSet.get(t).ImputedValue==-1){
				boolean isQualified = true;
				for(int w=0; w<ImputationRuleSet.get(t).FieldsWithData.size();w++){
					if(!parMatch.contains(ImputationRuleSet.get(t).FieldsWithData.get(w)+"|")){
						isQualified = false;
					}
				}
				
				if(isQualified==true){
					if(parMatch.contains(ImputationRuleSet.get(t).FieldWithMissingData+"|")){
						ImputationRuleSet.get(t).intUp++;
					}
					ImputationRuleSet.get(t).intDown++;
				}
			}
		}
	}

	private void closeProgress() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					//info.joinCompleted();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void scheduleStop() {
		this.stopped = true;
		this.system.getJoin().setCancelled(true);
		//this.interrupt();
	}
	
}
