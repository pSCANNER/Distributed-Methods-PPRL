package rosita.linkage.tools.cic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.apache.commons.collections.map.HashedMap;

import rosita.linkage.MappedPair;
import rosita.linkage.io.DatabaseConnection;
import cdc.configuration.ConfiguredSystem;
import cdc.gui.StoppableThread;
import cdc.utils.CPUInfo;
import cdc.utils.Log;
import cdc.utils.RJException;
import cdc.utils.StringUtils;

public class CICControl extends StoppableThread {
	
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
	
	private ArrayList<String> noneNullList = null;
	private ResultSet resultSet;
	private int intMissingCount;
	private DatabaseConnection dbConn;
	private ArrayList<MappedPair> mappedPairs;
	private ArrayList<MappedPair> mappedPairs_backup;
	
	private HashedMap numbers;
	
	private  CICThread[] workers;
	
	public CICControl(ConfiguredSystem system, ResultSet parResultSet, DatabaseConnection parDBConn, ArrayList<MappedPair> parMappedPairs, ArrayList<MappedPair> parMappedPairs_backup) {
		this.system = system;
		this.resultSet = parResultSet;
		this.dbConn = parDBConn;
		mappedPairs = parMappedPairs;
		mappedPairs_backup = parMappedPairs_backup;
		numbers = new HashedMap();
	}
	
	private void createWorkers() throws IOException, RJException {
		if (workers == null) {
			workers = new CICThread[CPUInfo.testNumberOfCPUs()];
		}
	}
	
	public void run() {
		n = 0;
		String[][] row;
		//JFrame frame = null;
		try {
			
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					//info.getProgressBar().setIndeterminate(!system.getJoin().isProgressSupported());
				}
			});
			
			System.gc();
			
			//system.getJoin().reset();
			//if (system.getJoin().isProgressSupported()) {
			//	new PollingThread(system).start();
			//}
			//system.getResultSaver().reset();
			
			t1 = System.currentTimeMillis();
			System.out.println("Start time: "+t1);
			
			HashedMap BackupValueA = new HashedMap();
			
			createWorkers();
			
			int intTempCount = 0;
			
			while ((row = this.dbConn.getNextResultWithColName(resultSet)) != null) {
				Boolean hasNull = false;
				int LocalMissingCountA = 0;
				for(int i=0; i<mappedPairs.size(); i++){
					//TODO: What is the real null value
					
					if(StringUtils.isNullOrEmpty(row[1][i])){
						hasNull = true;
						LocalMissingCountA++;
					}
				}	
				
				if(hasNull==true){
					ArrayList<String> noneNullList = new ArrayList<String>();
					for(int j = 0; j<mappedPairs_backup.size();j++){
						if(!StringUtils.isNullOrEmpty(row[1][mappedPairs.size()+j])){
							noneNullList.add(row[0][mappedPairs.size()+j]);
						}
					}
					
					if(LocalMissingCountA<=noneNullList.size()){
					
						Boolean isAssigned = false;
						
						
						
						while (isAssigned==false){
							HashedMap numbers;
							for (int i = 0; i < workers.length; i++) {
								if(workers[i]==null){
									intTempCount++;
									workers[i] = new CICThread(noneNullList, LocalMissingCountA);
									workers[i].start();
									isAssigned = true;
									break;
								}else if(workers[i].isFinished()){
									intTempCount++;
									numbers = workers[i].getBackupValue();
									recordNumbers(numbers);
									workers[i].stopProcessing();
									workers[i] = new CICThread(noneNullList, LocalMissingCountA);
									workers[i].start();
									isAssigned = true;
									break;
								}
							}
						}
					}
				}
			}
			
			Boolean isDone = false;
			
			while (isDone == false){
				isDone = true;
				for (int i=0;i<workers.length;i++){
					if(workers[i]!=null){
						isDone = false;
						if(workers[i].isFinished()){
							recordNumbers(workers[i].getBackupValue());
							workers[i].stopProcessing();
							workers[i] = null;
						}
					}
				}
			}
			
			System.out.println(intTempCount);
			//system.getJoin().closeListeners();
			//system.getJoin().close();
			
			t2 = System.currentTimeMillis();
			//System.out.println(system.getJoin() + ": Algorithm produced " + n + " joined tuples. Elapsed time: " + (t2 - FRILLinker.t1) + "ms.");

			//Log.log(getClass(), system.getJoin() + ": Algorithm produced " + n + " joined tuples. Elapsed time: " + (t2 - t1) + "ms.", 1);
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
		} catch (Exception e) {
			//JXErrorDialog.showDialog(MainFrame.main, "Error while joining data", e);
			closeProgress();
			e.printStackTrace();
		}
		

		
		//system.getJoin().setCancelled(false);
		//info = null;
		system = null;
	}
	
	public HashedMap getNumbers(){
		return numbers;
	}
	
	private void recordNumbers(HashedMap parNumbers){
		Object[] keys = parNumbers.keySet().toArray();
		for(int i=0;i<keys.length; i++){
			if(numbers.containsKey(keys[i])){
				int intCurrentValue = Integer.parseInt(numbers.get(keys[i]).toString())+Integer.parseInt(parNumbers.get(keys[i]).toString());
				numbers.remove(keys[i]);
				numbers.put(keys[i], intCurrentValue);
			}else{
				numbers.put(keys[i], Integer.parseInt(parNumbers.get(keys[i]).toString()));
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
