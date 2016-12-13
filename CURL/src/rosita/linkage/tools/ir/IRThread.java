


package rosita.linkage.tools.ir;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import cdc.components.AbstractDataSource;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.impl.join.nestedloop.NestedLoopJoin;
import cdc.utils.Log;
import cdc.utils.RJException;
import cdc.utils.RowUtils;

public class IRThread extends Thread {
	
	private static final int BUFFER_SIZE = 200;
	
	private AbstractDataSource sourceA;
	private AbstractDataSource sourceB;
	private ArrayBlockingQueue buffer;
	private rosita.linkage.tools.ir.IRJoin.NLJConnector connector;
	

	private volatile boolean completed = false;
	private volatile boolean stopped = false;
	private volatile RJException error;
	
	private int readA = 0;
	private int readB = 0;
	
	private int step = 1;
	
	public IRThread(AbstractDataSource sourceA, AbstractDataSource sourceB, ArrayBlockingQueue resultBuffer, rosita.linkage.tools.ir.IRJoin.NLJConnector parConnector){
		this.sourceA = sourceA;
		this.sourceB = sourceB;
		this.buffer = resultBuffer;
		this.connector = parConnector;
	}
	
	public void run() {
		DataRow rowA[];
		try {
			Log.log(getClass(), "Thread starts.", 2);
			main: while ((rowA = fillInBuffer(sourceA, BUFFER_SIZE)) != null) {
				readA += rowA.length;
				DataRow rowB[];
				Log.log(NestedLoopJoin.class, "Outer loop starts", 3);
				while ((rowB = fillInBuffer(sourceB, 1)) != null) {
					readB += rowB.length;
					for (int i = 0; i < rowA.length; i++) {
						for (int j = 0; j < rowB.length; j++) {
							calculateProgress();
							Log.log(NestedLoopJoin.class, "Inner loop starts", 4);
							IREvaluatedCondition eval;
							if ((eval = (IREvaluatedCondition) connector.getJoinCondition().conditionSatisfied(rowA[i], rowB[j])).isSatisfied()) {
								DataColumnDefinition[] tempColumnDefinition = new DataColumnDefinition[]{new DataColumnDefinition("Match", DataColumnDefinition.TYPE_STRING, "")};
								DataCell[] tempCell = new DataCell[]{new DataCell(1, eval.matchString)};
								DataRow tempRow = new DataRow(tempColumnDefinition, tempCell);
								buffer.put(tempRow);
							} else {
								step++;
								//this is only to see debug info...
								if (step % 1000 == 0 && connector.isAnyJoinListenerRegistered()) {
									step = 1;
									connector.notifyNotJoined(rowA[i], rowB[j], eval.getConfidence());
								}
							}
						}
					}
					if (connector.isCancelled() || stopped) {
						break main;
					}
				}
				
				for (int i = 0; i < rowA.length; i++) {
					if (RowUtils.shouldReportTrashingNotJoined(connector.getJoin(), rowA[i])) {
						connector.notifyTrashingNotJoined(rowA[i]);
					}
				}
				Log.log(NestedLoopJoin.class, "Inner source read fully", 3);
				sourceB.reset();
			}
			
			//System.out.println(getName() + " read " + readA + " rows.");
			
		} catch (RJException e) {
			error = e;
		} catch (IOException e) {
			error = new RJException("Error while joining data", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				sourceB.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RJException e) {
				e.printStackTrace();
			}
		}
		
		Log.log(getClass(), "Thread completed.", 2);
		
		synchronized (this) {
			completed = true;
			this.notifyAll();
		}
		
	}

	private DataRow[] fillInBuffer(AbstractDataSource source, int size) throws IOException, RJException {
		return source.getNextRows(size);
	}

	private void calculateProgress() {
		
	}
	
	public RJException getError() {
		try {
			synchronized (this) {
				while (!completed) {
					this.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return error;
	}

	public void stopProcessing() {
		stopped = true;
		//just wait for finish...
		getError();
	}

	public boolean isFinished() {
		synchronized (this) {
			return completed;
		}
	}
	
	public int getReadA() {
		return readA;
	}
	
	public int getReadB() {
		return readB;
	}
	
}
