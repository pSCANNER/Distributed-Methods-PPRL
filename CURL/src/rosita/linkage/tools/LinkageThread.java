package rosita.linkage.tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import rosita.linkage.FRILLinker;
import cdc.configuration.ConfiguredSystem;
import cdc.datamodel.DataRow;
import cdc.gui.StoppableThread;
import cdc.utils.Log;
import cdc.utils.RJException;

public class LinkageThread extends StoppableThread {
	
	private class PollingThread extends Thread {
		private ConfiguredSystem system;
		public PollingThread(ConfiguredSystem system) {
			this.system = system;
		}
		public void run() {
			/*while (LinkageThread.this.info != null) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							//panel.getProgressBar().setValue(system.getJoin().getProgress());
						}
					});
					sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}*/
			//this.panel = null;
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
	
	public LinkageThread(ConfiguredSystem system) {
		this.system = system;
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
			system.getResultSaver().reset();
			
			t1 = System.currentTimeMillis();
			System.out.println("Start time: "+t1);
			while ((row = system.getJoin().joinNext()) != null) {
				n++;
				//if(row.getData()[0].getValue().toString().equals("3344") && row.getData()[1].getValue().toString().equals("2556")){
				//	int k = 0;
				//	k++;
				//}
					
				system.getResultSaver().saveRow(row);
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						//info.incrementJoined();
					}
				});
				if (stopped) {
					break;
				}
				
			}
			system.getResultSaver().flush();
			//System.out.println("End time: "+t2);
			system.getResultSaver().close();
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
			e.printStackTrace();
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
