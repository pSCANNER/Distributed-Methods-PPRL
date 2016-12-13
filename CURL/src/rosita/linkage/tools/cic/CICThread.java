package rosita.linkage.tools.cic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.collections.map.HashedMap;

import rosita.linkage.tools.CombinationCreator;
import cdc.components.AbstractDataSource;
import cdc.datamodel.DataRow;
import cdc.utils.RJException;

public class CICThread extends Thread {
	
	private static final int BUFFER_SIZE = 200;
	
	private ArrayBlockingQueue buffer;
	

	private volatile boolean completed = false;
	private volatile boolean stopped = false;
	private volatile RJException error;
	
	private int readA = 0;
	private int readB = 0;
	
	private ArrayList<String> noneNullList; 
	private int intMissingCount;
	private int step = 1;
	
	private HashedMap BackupValue = null;
	
	public CICThread(ArrayList<String> parNoneNullList, int parMissingCount){
		intMissingCount = parMissingCount;
		noneNullList = parNoneNullList;
	}
	
	public void run() {
		CombinationCreator combinationCreator = new CombinationCreator();
		combinationCreator.generate(noneNullList, intMissingCount);
		BackupValue = new HashedMap();

		for(int j=0;j<combinationCreator.results.size(); j++){
				//Create the key
			String strKey= "";
			for(int t=0; t<combinationCreator.results.get(j).size();t++){
				strKey += combinationCreator.results.get(j).get(t)+"|";
			}
			
			strKey = strKey.substring(0, strKey.length()-1);
			
			if(BackupValue.containsKey(strKey)){
				int intCurrentValue = Integer.parseInt(BackupValue.get(strKey).toString())+1;
				BackupValue.remove(strKey);
				BackupValue.put(strKey, intCurrentValue);
			}else{
				BackupValue.put(strKey, 1);
			}
		}
		synchronized (this) {
			completed = true;
			this.notifyAll();
		}
		
	}
	
	public HashedMap getBackupValue(){
		return BackupValue;
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
