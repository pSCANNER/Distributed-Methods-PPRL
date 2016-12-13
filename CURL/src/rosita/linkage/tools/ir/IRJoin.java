

package rosita.linkage.tools.ir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import cdc.components.AbstractDataSource;
import cdc.components.AbstractJoin;
import cdc.components.AbstractJoinCondition;
import cdc.components.LinkageSummary;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.DataRow;
import cdc.gui.GUIVisibleComponent;
import cdc.utils.CPUInfo;
import cdc.utils.HTMLUtils;
import cdc.utils.Log;
import cdc.utils.PrintUtils;
import cdc.utils.Props;
import cdc.utils.RJException;

public class IRJoin extends AbstractJoin {

	private static int logLevel = Log.getLogLevel(IRJoin.class);
	
	
	public class NLJConnector {
		public boolean isCancelled() {
			return IRJoin.this.isCancelled();
		}
		public int getLogLevel() {
			return logLevel;
		}
		public DataColumnDefinition[] getOutColumns() {
			return IRJoin.this.getOutColumns();
		}
		public AbstractJoinCondition getJoinCondition() {
			return IRJoin.this.getJoinCondition();
		}
		public boolean isAnyJoinListenerRegistered() {
			return IRJoin.this.isAnyJoinListenerRegistered();
		}
		public void notifyNotJoined(DataRow rowA, DataRow rowB, int conf) throws RJException {
			IRJoin.this.notifyNotJoined(rowA, rowB, conf);
		}
		public void notifyJoined(DataRow rowA, DataRow rowB, DataRow row) throws RJException {
			IRJoin.this.notifyJoined(rowA, rowB, row);
		}
		public void notifyTrashingNotJoined(DataRow dataRow) throws RJException {
			IRJoin.this.notifyTrashingNotJoined(dataRow);
		}
		public void notifyTrashingJoined(DataRow dataRow) throws RJException {
			IRJoin.this.notifyTrashingJoined(dataRow);
		}
		public AbstractJoin getJoin() {
			return IRJoin.this;
		}
	}
	
	private IRThread[] workers;
	private ArrayBlockingQueue buffer = new ArrayBlockingQueue(Props.getInteger("intrathread-buffer"));
	private boolean closed = false;

	private int readA;
	private int readB;
	
	private NLJConnector connector = new NLJConnector();
	
	public IRJoin(AbstractDataSource sourceA, AbstractDataSource sourceB, DataColumnDefinition outFormat[], AbstractJoinCondition cond, Map props) throws RJException {
		super(sourceA, sourceB, cond, outFormat, props);
		Log.log(IRJoin.class, "Join operator created", 1);
		Log.log(IRJoin.class, "Out model: " + PrintUtils.printArray(getOutColumns()), 2);
	}

	protected DataRow doJoinNext() throws IOException, RJException {
		
		if (closed) {
			return null;
		}
		
		createWorkersIfNeeded();
		
		try {
			main: while (true) {
				DataRow row = (DataRow) buffer.poll(100, TimeUnit.MILLISECONDS);
				calculateProgress();
				if (row != null) {
					return row;
				} else if (isCancelled()) {
					updateSrcStats();
					return null;
				} else {
					for (int i = 0; i < workers.length; i++) {
						if (!workers[i].isFinished()) {
							continue main;
						}
					}
					updateSrcStats();
					return null;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void updateSrcStats() {
		for (int i = 0; i < workers.length; i++) {
			readA += workers[i].getReadA();
		}
		readB = workers[0].getReadB();
	}

	protected DataRow[] doJoinNext(int size) throws IOException, RJException {
		List joinResult = new ArrayList();
		DataRow result;
		while (((result = joinNext()) != null) && joinResult.size() < size) {
			joinResult.add(result);
		}
		return (DataRow[]) joinResult.toArray(new DataRow[] {});
	}

	protected void doClose() throws IOException, RJException {
		Log.log(IRJoin.class, "Join operator closing", 1);
		if (!closed) {
			getSourceA().close();
			getSourceB().close();
			closed = true;
		}
		if (workers != null) {
			for (int i = 0; i < workers.length; i++) {
				workers[i].stopProcessing();
			}
			workers = null;
		}
		buffer = null;
	}

	public String toString() {
		return "NestedLoopJoin";
	}

	public static GUIVisibleComponent getGUIVisibleComponent() {
		//return new NLJGUIVisibleComponent();
		return null;
	}

	protected void doReset(boolean deep) throws IOException, RJException {
		getSourceA().reset();
		getSourceB().reset();
		if (workers != null) {
			for (int i = 0; i < workers.length; i++) {
				workers[i].stopProcessing();
			}
			workers = null;
		}
		readA = 0;
		readB = 0;
		createWorkersIfNeeded();
	}
	
	private void createWorkersIfNeeded() throws IOException, RJException {
		if (workers == null) {
			workers = new IRThread[CPUInfo.testNumberOfCPUs()];
			for (int i = 0; i < workers.length; i++) {
				workers[i] = new IRThread(getSourceA(), getSourceB().copy(), buffer, connector);
				workers[i].start();
			}
		}
	}

	protected void finalize() throws Throwable {
		//System.out.println(getClass() + " finalize");
		//close();
	}
	
	public String toHTMLString() {
		StringBuilder builder = new StringBuilder();
		builder.append(HTMLUtils.getHTMLHeader());
		builder.append(HTMLUtils.encodeTable(new String[][] {
				{"Search method:", "Nested loop join (NLJ)"}, 
			}));
		builder.append("Attributes mapping and distance function selection:<br>");
		builder.append(HTMLUtils.encodeJoinCondition(getJoinCondition()));
		builder.append("</html>");
		return builder.toString();
	}
	
	private void calculateProgress() {
		try {
			int progress = (int) Math.round(getSourceA().position() / (double)getSourceA().size() * 100);
			setProgress(progress);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isProgressSupported() {
		return true;
	}
	
	public LinkageSummary getLinkageSummary() {
		return new LinkageSummary(readA, readB, getLinkedCnt());
	}
	
}
