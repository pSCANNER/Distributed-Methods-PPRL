

package rosita.linkage.tools.cic;

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

public class CICJoin extends AbstractJoin {

	private static int logLevel = Log.getLogLevel(CICJoin.class);
	
	private ArrayList<String> noneNullList;
	private int intMissingCount;
	
	public class NLJConnector {
		public boolean isCancelled() {
			return CICJoin.this.isCancelled();
		}
		public int getLogLevel() {
			return logLevel;
		}
		public DataColumnDefinition[] getOutColumns() {
			return CICJoin.this.getOutColumns();
		}
		public AbstractJoinCondition getJoinCondition() {
			return CICJoin.this.getJoinCondition();
		}
		public boolean isAnyJoinListenerRegistered() {
			return CICJoin.this.isAnyJoinListenerRegistered();
		}
		public void notifyNotJoined(DataRow rowA, DataRow rowB, int conf) throws RJException {
			CICJoin.this.notifyNotJoined(rowA, rowB, conf);
		}
		public void notifyJoined(DataRow rowA, DataRow rowB, DataRow row) throws RJException {
			CICJoin.this.notifyJoined(rowA, rowB, row);
		}
		public void notifyTrashingNotJoined(DataRow dataRow) throws RJException {
			CICJoin.this.notifyTrashingNotJoined(dataRow);
		}
		public void notifyTrashingJoined(DataRow dataRow) throws RJException {
			CICJoin.this.notifyTrashingJoined(dataRow);
		}
		public AbstractJoin getJoin() {
			return CICJoin.this;
		}
	}
	
	private  CICThread[] workers;
	private ArrayBlockingQueue buffer = new ArrayBlockingQueue(Props.getInteger("intrathread-buffer"));
	private boolean closed = false;

	private int readA;
	private int readB;
	
	private NLJConnector connector = new NLJConnector();
	
	public CICJoin(AbstractDataSource sourceA, AbstractDataSource sourceB, DataColumnDefinition outFormat[], AbstractJoinCondition cond, Map props) throws RJException {
		super(sourceA, sourceB, cond, outFormat, props);
		Log.log(CICJoin.class, "Join operator created", 1);
		Log.log(CICJoin.class, "Out model: " + PrintUtils.printArray(getOutColumns()), 2);
	}
	
	public ArrayList<String> find(ArrayList<String> parNoneNullList, int parMissingCount){
		ArrayList<String> result = null;
		
		noneNullList = parNoneNullList;
		intMissingCount = parMissingCount;
		
		if (closed) {
			return null;
		}
		
		try {
			createWorkersIfNeeded();
			main: while (true) {
				ArrayList<String> numbers = (ArrayList<String>) buffer.poll(100, TimeUnit.MILLISECONDS);
				calculateProgress();
				if (numbers != null) {
					return numbers;
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RJException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return result;
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
		Log.log(CICJoin.class, "Join operator closing", 1);
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
			workers = new CICThread[CPUInfo.testNumberOfCPUs()];
			for (int i = 0; i < workers.length; i++) {
				//workers[i] = new CICThread(noneNullList, intMissingCount, buffer);
				//workers[i].start();
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
