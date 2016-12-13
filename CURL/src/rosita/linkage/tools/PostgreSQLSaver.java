package rosita.linkage.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;

import rosita.linkage.io.DatabaseConnection;
import au.com.bytecode.opencsv.CSVWriter;
import cdc.components.AbstractJoin;
import cdc.components.AbstractResultsSaver;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataRow;
import cdc.gui.GUIVisibleComponent;
import cdc.gui.components.paramspanel.FileChoosingPanelFieldCreator;
import cdc.gui.components.paramspanel.ParamsPanel;
import cdc.impl.join.strata.StrataJoinWrapper;
import cdc.utils.Log;
import cdc.utils.RJException;
import cdc.utils.Utils;

public class PostgreSQLSaver extends AbstractResultsSaver{
	
	public static final String DEFAULT_FILE = "results.csv";
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final String OUTPUT_FILE_PROPERTY = "output-file";
	public static final String OUTPUT_FILE_ENCODING = "encoding";
	public static final String SAVE_SOURCE_NAME = "save-source-name";
	public static final String SAVE_CONFIDENCE = "save-confidence";
	public static final String TARGET_TABLE_NAME = "target-table-name";
	public static final String TARGET_SCHEMA = "target-schema";
	public static final String LEFT_TABLE_NAME = "left-table-name";
	public static final String LEFT_SCHEMA = "left-schema";
	public static final String LEFT_DATABASE_NAME = "left-database-name";
	public static final String LEFT_LINK_NAME = "left-link-name";
	public static final String RIGHT_TABLE_NAME = "right-table-name";
	public static final String RIGHT_SCHEMA = "right-schema";
	public static final String RIGHT_DATABASE_NAME = "right-database-name";
	public static final String RIGHT_LINK_NAME = "right-link-name";
	
	private static class CSVFileSaverVisibleComponent extends GUIVisibleComponent {
		
		private ParamsPanel panel;
		
		public Object generateSystemComponent() throws RJException, IOException {
			Map params = panel.getParams();
			String fileName = (String) params.get(OUTPUT_FILE_PROPERTY);
			String[] name = Utils.parseFilePath(fileName);
			if (!name[0].endsWith(".csv")) {
				name[0] = name[0] + ".csv";
			}
			params.put(OUTPUT_FILE_PROPERTY, name[0]);
			if (name.length == 2) {
				params.put(OUTPUT_FILE_ENCODING, name[1]);
			}
			
			return new PostgreSQLSaver(params,null);
		}
		public JPanel getConfigurationPanel(Object[] objects, int sizeX, int sizeY) {
			String file = DEFAULT_FILE;
			String enc = getRestoredParam(OUTPUT_FILE_PROPERTY) == null ? DEFAULT_ENCODING : "US-ASCII";
			if (getRestoredParam(OUTPUT_FILE_PROPERTY) != null) {
				file = getRestoredParam(OUTPUT_FILE_PROPERTY);
			}
			if (getRestoredParam(OUTPUT_FILE_ENCODING) != null) {
				enc = getRestoredParam(OUTPUT_FILE_ENCODING);
			}
			String[] defs = new String[] {file + "#ENC=" + enc + "#"};
			Map map = new HashMap();
			map.put(OUTPUT_FILE_PROPERTY, new FileChoosingPanelFieldCreator(FileChoosingPanelFieldCreator.SAVE));
			panel = new ParamsPanel(
					new String[] {OUTPUT_FILE_PROPERTY},
					new String[] {"Output file"},
					defs,
					map
			);
			
			return panel;
		}
		public Class getProducedComponentClass() {
			return MySQLSaver.class;
		}
		public String toString() {
			return "CSV file data saver";
		}
		public boolean validate(JDialog dialog) {
			return true;
		}
	}
	
	private File file;
	private Charset encoding = Utils.DEFAULT_ENCODING.getCharset();
	private CSVWriter printer;
	private String targetTable;
	private String targetFullTable;
	private String targetSchema;
	
	private String leftTable;
	private String leftFullTable;
	private String leftDatabase;
	private String leftSchema;
	private String leftLink;
	
	private String rightTable;
	private String rightFullTable;
	private String rightDatabase;
	private String rightSchema;
	private String rightLink;
	
	private boolean saveConfidence = true;
	private boolean closed = false;
	private boolean saveSourceName = true;
	private DatabaseConnection saverConnection;
	
	public PostgreSQLSaver(Map properties, DatabaseConnection parDBConnection) throws RJException {
		super(properties);
		saverConnection = parDBConnection;
		
		if (!properties.containsKey(OUTPUT_FILE_PROPERTY)) {
			file = new File(DEFAULT_FILE);
		} else {
			file = new File((String) properties.get(OUTPUT_FILE_PROPERTY));
		}
		
		if(properties.containsKey(TARGET_SCHEMA)){
			targetSchema = String.valueOf(properties.get(TARGET_SCHEMA));
		}
		
		if(properties.containsKey(TARGET_TABLE_NAME)){
			targetTable = String.valueOf(properties.get(TARGET_TABLE_NAME));
			
			if(targetSchema!= null){
				targetFullTable = targetSchema+"."+targetTable;
			}else{
				targetFullTable = targetTable;
			}
			
			if(saverConnection.checkPostgreTableExists(targetTable, targetSchema)){
				saverConnection.executeActionQuery("Drop Table "+targetFullTable);
			}
		}
		
		if(properties.containsKey(LEFT_TABLE_NAME)){
			leftTable = String.valueOf(properties.get(LEFT_TABLE_NAME));
		}
		if(properties.containsKey(LEFT_SCHEMA)){
			leftSchema = String.valueOf(properties.get(LEFT_SCHEMA));
		}
		
		if(leftSchema!=null){
			leftFullTable = leftSchema+"."+leftTable;
		}else{
			leftFullTable = leftTable;
		}
		
		if(properties.containsKey(LEFT_DATABASE_NAME)){
			leftDatabase = String.valueOf(properties.get(LEFT_DATABASE_NAME));
		}
		if(properties.containsKey(LEFT_LINK_NAME)){
			leftLink = String.valueOf(properties.get(LEFT_LINK_NAME));
		}
		
		if(properties.containsKey(RIGHT_TABLE_NAME)){
			rightTable = String.valueOf(properties.get(RIGHT_TABLE_NAME));
		}
		if(properties.containsKey(RIGHT_SCHEMA)){
			rightSchema = String.valueOf(properties.get(RIGHT_SCHEMA));
		}
		
		if(rightSchema!=null){
			rightFullTable = rightSchema+"."+rightTable;
		}else{
			rightFullTable = rightTable;
		}
		
		if(properties.containsKey(RIGHT_DATABASE_NAME)){
			rightDatabase = String.valueOf(properties.get(RIGHT_DATABASE_NAME));
		}
		if(properties.containsKey(RIGHT_LINK_NAME)){
			rightLink = String.valueOf(properties.get(RIGHT_LINK_NAME));
		}
		
		if (properties.containsKey(SAVE_SOURCE_NAME)) {
			saveSourceName = Boolean.parseBoolean((String)properties.get(SAVE_SOURCE_NAME));
		}
		if (properties.containsKey(OUTPUT_FILE_ENCODING)) {
			encoding = Utils.getEncodingForName((String)properties.get(OUTPUT_FILE_ENCODING)).getCharset();
		}
		Log.log(getClass(), "Saver created. Encoding=" + encoding, 2);
		if (file.exists() && !file.isFile()) {
			throw new RJException("Output file cannot be directory or other special file");
		}
		if (properties.containsKey(SAVE_CONFIDENCE)) {
			saveConfidence = properties.get(SAVE_CONFIDENCE).equals("true");
		}
	}
	
	public void saveRow(DataRow row) throws RJException, IOException {	
		String stratum = row.getProperty(StrataJoinWrapper.PROPERTY_STRATUM_NAME);
		String strCmd= "";
		//Check target table existence
		
		if(!saverConnection.checkPostgreTableExists(targetTable, targetSchema)){
			//Connect to the database and retrieve the data type of the linking field
			
			//Changed to PostgreSQL structure
			
			//strCmd = "Select COLUMN_TYPE From INFORMATION_SCHEMA.COLUMNS Where TABLE_SCHEMA='"+leftDatabase+"' AND TABLE_NAME='"+leftTable+"' AND COLUMN_NAME='"+leftLink+"'";
			
			strCmd = "Select DATA_TYPE From INFORMATION_SCHEMA.COLUMNS Where TABLE_SCHEMA='"+leftSchema+"' AND TABLE_NAME='"+leftTable+"' AND COLUMN_NAME='"+leftLink+"'";
    			String LeftDataType = saverConnection.getSingleValueSQL(strCmd).toString();
			
			//strCmd = "Select COLUMN_TYPE From INFORMATION_SCHEMA.COLUMNS Where TABLE_SCHEMA='"+rightDatabase+"' AND TABLE_NAME='"+rightTable+"' AND COLUMN_NAME='"+rightLink+"'";
			strCmd = "Select DATA_TYPE From INFORMATION_SCHEMA.COLUMNS Where TABLE_SCHEMA='"+rightSchema+"' AND TABLE_NAME='"+rightTable+"' AND COLUMN_NAME='"+rightLink+"'";
			String RightDataType = saverConnection.getSingleValueSQL(strCmd).toString();
			
			strCmd = "Create table "+targetFullTable+" ( left_"+leftLink +" "+LeftDataType+", right_"+rightLink+" "+RightDataType+", Confidence int)";
			saverConnection.executeActionQuery(strCmd);
		}
		
		//Build the header
		/*String[] header = new String[row.getData().length + (saveConfidence ? 1 : 0) + (stratum != null?1:0)];
		for (int i = 0; i < header.length - (stratum != null?1:0) - (saveConfidence ? 1 : 0); i++) {
			if (saveSourceName) {
				header[i] = row.getRowModel()[i].toString();
			} else {
				header[i] = row.getRowModel()[i].getColumnName();
			}
		}
		if (stratum != null) {
			header[header.length - (saveConfidence ? 2 : 1)] = "Stratum name";
		}
		if (saveConfidence) {
			header[header.length - 1] = "Confidence";
		}
		
		strCmd = "INSERT INTO "+targetTable+"(";
		for(int i=0; i<header.length;i++){
			strCmd += header[i]+",";		
		}*/
		
		strCmd = "INSERT INTO "+targetFullTable+"(left_"+leftLink+", right_"+rightLink+", Confidence) VALUES(";
		
		//strCmd = strCmd.substring(0,strCmd.length()-1)+") VALUES('";
		
		DataCell[] cells = row.getData();
		String[] strRow = new String[cells.length + (saveConfidence ? 1 : 0) + (stratum != null ? 1 : 0)];
		for (int i = 0; i < strRow.length - (stratum != null ? 1 : 0) - (saveConfidence ? 1 : 0); i++) {
			strRow[i] = cells[i].getValue().toString();
		}
		if (stratum != null) {
			strRow[strRow.length - (saveConfidence ? 2 : 1)] = stratum;
		}
		if (saveConfidence) {
			strRow[strRow.length - 1] = row.getProperty(AbstractJoin.PROPERTY_CONFIDNCE);
		}

		//create Insert statement
		for(int i=0; i<strRow.length-1;i++){
			strCmd += "'"+strRow[i]+"',";		
		}
		strCmd = strCmd+strRow[strRow.length-1]+")";
		
		saverConnection.executeActionQuery(strCmd);
	}
	
	public void flush() throws IOException {
		if (printer != null) {
			printer.flush();
		}
	}

	public void close() throws IOException {
		//Calculate performance info
		
		PostgreSQLResultReporter resultReporter = new PostgreSQLResultReporter();
		int intTP = resultReporter.calculateTP(targetFullTable, leftLink, rightLink, saverConnection);
		int intFP = resultReporter.calculateFP(targetFullTable, leftLink, rightLink, saverConnection);
		int intTN = resultReporter.calculateTN(targetFullTable, leftFullTable, rightFullTable, leftLink, rightLink, saverConnection, targetSchema);
		int intFN = resultReporter.calculateFN(targetFullTable, leftFullTable, rightFullTable, leftLink, rightLink, saverConnection, targetSchema);
		
		double dblSensitivity = ((double)intTP)/(intTP+intFN);
		double dblSpecificity = ((double)intTN)/(intTN+intFP);
		
		System.out.println("TP: "+intTP);
		System.out.println("FP: "+intFP);
		System.out.println("TN: "+intTN);
		System.out.println("FN: "+intFN);
		
		System.out.println("Sensitivity: "+dblSensitivity);
		System.out.println("Specificity: "+dblSpecificity);
		
		Log.log(getClass(), "Close in MySQL saver.");
		if (closed) {
			return;
		}
		closed = true;
		if (printer != null) {
			printer.flush();
			printer.close();
			printer = null;
		}
	}
	
	public void reset() throws IOException {
		if (printer != null) {
			printer.close();
			printer = null;
		}
		closed = false;
	}

	public static GUIVisibleComponent getGUIVisibleComponent() {
		return new CSVFileSaverVisibleComponent();
	}
	
	public String toString() {
		return "CSV file saver";
	}
	
	public String toHTMLString() {
		return "CSV result saver (file=" + file.getName() + ")";
	}

	public String getActiveDirectory() {
		return new File(file.getAbsolutePath()).getParent();
	}

	public boolean isClosed() {
		return closed;
	}
}
