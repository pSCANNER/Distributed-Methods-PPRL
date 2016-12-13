package rosita.linkage.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import cdc.components.AbstractDataSource;
import cdc.components.AbstractJoinCondition;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.gui.StoppableThread;
import cdc.gui.external.JXErrorDialog;
import cdc.impl.datasource.sampling.DataSampleInterface;
import cdc.impl.datasource.sampling.FullSampler;
import cdc.impl.datasource.sampling.RandomSampler;
import cdc.impl.em.EMEstimator;
import cdc.impl.em.actions.ConfigureBlockingMethod;
import cdc.impl.em.actions.ConfigureSearchMethodAction;
import cdc.impl.em.actions.ConfigureSourcesAction;
import cdc.impl.join.blocking.BlockingFunction;
import cdc.impl.join.blocking.BucketManager;
import cdc.impl.join.blocking.EqualityBlockingFunction;
import cdc.utils.RJException;

public class S_EMThread extends StoppableThread{
	private volatile boolean stopScheduled = false;
	private S_EMResultsReporter wizard;
	private ConfigureSourcesAction sources;
	private ConfigureSearchMethodAction search;
	private ConfigureBlockingMethod blocking;
	//private EMRunnerAction runnerGUI;
	private AbstractJoinCondition condition;
	private int[] weights;
	private S_EMEstimator estimator;
	private int intBlockPairIndex;
	private int intRandomSampleSize = 100;
	private AbstractDataSource mySourceA;
	private AbstractDataSource mySourceB;
	
	public S_EMThread(AbstractDataSource SourceA, AbstractDataSource SourceB, AbstractJoinCondition condition, int parBlockPairIndex, int parRandomSampleSize) {
			try {
				sources = new ConfigureSourcesAction(SourceA, SourceB);
				search = new ConfigureSearchMethodAction();
				blocking = new ConfigureBlockingMethod(condition);
				
				mySourceA = SourceA;
				mySourceB = SourceB;
				
				intBlockPairIndex = parBlockPairIndex;
				intRandomSampleSize = parRandomSampleSize;
				
				this.condition = condition;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RJException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

	public void run() {
		try {
			estimator = new S_EMEstimator();
			EMEstimator.cancel = false;
			//DataSampleInterface leftSource = sources.getSamplingConfigurationForSourceA();
			//DataSampleInterface leftSource = sources.getSamplingConfigurationForSourceA();
			//DataSampleInterface rightSource = sources.getSamplingConfigurationForSourceB();
			DataSampleInterface leftSource = new RandomSampler(mySourceA, intRandomSampleSize);
			DataSampleInterface rightSource = new RandomSampler(mySourceB, intRandomSampleSize);
			BlockingFunction function = null;
			int hashingAttribute = -1; 
			DataColumnDefinition[][] columns = null;
			
			if (intBlockPairIndex>0) {
				//hashingAttribute = blocking.getBlockingAttribute();
				hashingAttribute = intBlockPairIndex;
				columns = new DataColumnDefinition[][]{{condition.getLeftJoinColumns()[hashingAttribute], 
					  condition.getRightJoinColumns()[hashingAttribute]}};
				//Directly use Equality Blocking Function 
				function = new EqualityBlockingFunction(columns);
			}
			
			DataRow[][] rowsA;
			DataRow[][] rowsB;
			if (function != null) {
				BucketManager manager = new BucketManager(function);
				DataRow row;
				int n = 0;
				if (stopped()) {doStop(); return;}
				while ((row = leftSource.getNextRow()) != null) {
					manager.addToBucketLeftSource(row);
					n++;
				}
				if (stopped()) {doStop(); return;}
				n = 0;
				while ((row = rightSource.getNextRow()) != null) {
					manager.addToBucketRightSource(row);
					n++;
					if ((n % 1000) == 0 && n != 0) {
					}
				}
				manager.addingCompleted();
				if (stopped()) {doStop(); return;}
				if (leftSource instanceof FullSampler) {
					DataRow[][] activeBucket;
					ArrayList list1 = new ArrayList();
					ArrayList list2 = new ArrayList();
					read: for (int i = 0; i < ((FullSampler)leftSource).getNumberOfBlocks(); i++) {
						do {
							activeBucket = manager.getBucket();
							if (activeBucket == null) {
								break read;
							}
						} while (activeBucket[0].length == 0 || activeBucket[1].length == 0);
						//System.out.println("Buckets: " + activeBucket[0].length + " <--> " + activeBucket[1].length);
						list1.add(activeBucket[0]);
						list2.add(activeBucket[1]);
						if (stopped()) {doStop(); return;}
					}
					rowsA = (DataRow[][]) list1.toArray(new DataRow[][] {});
					rowsB = (DataRow[][]) list2.toArray(new DataRow[][] {});
				} else {
					DataRow[][] activeBucket;
					ArrayList list1 = new ArrayList();
					ArrayList list2 = new ArrayList();
					if (stopped()) {doStop(); return;}
					while ((activeBucket = manager.getBucket()) != null) {
						list1.add(activeBucket[0]);
						list2.add(activeBucket[1]);
					}
					if (stopped()) {doStop(); return;}
					rowsA = (DataRow[][]) list1.toArray(new DataRow[][] {});
					rowsB = (DataRow[][]) list2.toArray(new DataRow[][] {});
				}
				if (search.isAllToAll()) {
					ArrayList list1 = new ArrayList();
					ArrayList list2 = new ArrayList();
					if (stopped()) {doStop(); return;}
					for (int i = 0; i < rowsA.length; i++) {
						list1.addAll(Arrays.asList(rowsA[i]));
					}
					if (stopped()) {doStop(); return;}
					for (int i = 0; i < rowsB.length; i++) {
						list2.addAll(Arrays.asList(rowsB[i]));
					}
					if (stopped()) {doStop(); return;}
					rowsA = new DataRow[][] {(DataRow[])list1.toArray(new DataRow[] {})};
					rowsB = new DataRow[][] {(DataRow[])list2.toArray(new DataRow[] {})};
				}
			} else {
				ArrayList list1 = new ArrayList();
				ArrayList list2 = new ArrayList();
				DataRow row;
				if (stopped()) {doStop(); return;}
				while ((row = leftSource.getNextRow()) != null) {
					list1.add(row);
				}
				if (stopped()) {doStop(); return;}
				while ((row = rightSource.getNextRow()) != null) {
					list2.add(row);
				}
				if (stopped()) {doStop(); return;}
				rowsA = new DataRow[][] {(DataRow[]) list1.toArray(new DataRow[] {})};
				rowsB = new DataRow[][] {(DataRow[]) list2.toArray(new DataRow[] {})};
			}
			
			//now - rowsA and rowsB have what should be compared
			weights = estimator.weightsTo0_100(estimator.runEMMethodBlocking(rowsA, rowsB, condition, 0.02));
			//weights = estimator.weightsTo0_100(estimator.runEMMethodAllToAll(rowsA[0], rowsB[0], condition, 0.02));
						
			doStop();
		} catch (RJException e) {
			JXErrorDialog.showDialog(wizard, "Error running EM method", e);
		} catch (IOException e) {
			JXErrorDialog.showDialog(wizard, "Error running EM method", e);
		}
	}
	
	private void doStop() {
		EMEstimator.cancel = false;
		//wizard.finished(weights != null);
	}

	public boolean stopped() {
		return stopScheduled;
	}
	
	public void scheduleStop() {
		stopScheduled = true;
		EMEstimator.cancel = true;
		System.out.println("Cancelled...");
	}

	public int[] getFinalWeights() {
		return weights;
	}
}
