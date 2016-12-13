package rosita.linkage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import rosita.linkage.analysis.Algorithm;
import rosita.linkage.analysis.DBMS;
import rosita.linkage.analysis.NONEDistance;
import rosita.linkage.analysis.PPRLDistance;
import rosita.linkage.analysis.SamplingMethod;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.tools.ImputationRule;
import rosita.linkage.tools.JoinMethod;
import rosita.linkage.tools.MySQLSaver;
import rosita.linkage.tools.PostgreSQLSaver;
import rosita.linkage.tools.S_EMResultsReporter;
import rosita.linkage.tools.S_EMThread;
import rosita.linkage.tools.S_JDBCDataSource;
import rosita.linkage.tools.S_Postgre_JDBCDataSource;
import rosita.linkage.tools.S_WeightedJoinCondition;
import rosita.linkage.tools.WeightIdentifier;
import rosita.linkage.tools.cic.BackupField;
import rosita.linkage.tools.cic.CICGenerator;
import rosita.linkage.tools.ir.IRCondition;
import rosita.linkage.tools.ir.IRGenerator;
import rosita.linkage.tools.ir.IRJoin;
import rosita.linkage.util.MyUtilities;
import cdc.components.AbstractDataSource;
import cdc.components.AbstractDistance;
import cdc.components.AbstractJoin;
import cdc.components.AbstractResultsSaver;
import cdc.configuration.ConfiguredSystem;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataColumnDefinition;
import cdc.datamodel.converters.ConverterColumnWrapper;
import cdc.datamodel.converters.ModelGenerator;
import cdc.impl.conditions.WeightedJoinCondition;
import cdc.impl.datasource.jdbc.JDBCDataColumnDefinition;
import cdc.impl.datasource.jdbc.JDBCDataSource;
import cdc.impl.datasource.sampling.DataSampleInterface;
import cdc.impl.datasource.sampling.FirstNSampler;
import cdc.impl.datasource.sampling.RandomSampler;
import cdc.impl.distance.AddressDistance;
import cdc.impl.distance.DateDistance;
import cdc.impl.distance.EditDistance;
import cdc.impl.distance.EqualFieldsDistance;
import cdc.impl.distance.JaroWinkler;
import cdc.impl.distance.QGramDistance;
import cdc.impl.distance.SoundexDistance;
import cdc.impl.join.blocking.BlockingJoin;
import cdc.impl.join.nestedloop.NestedLoopJoin;
import cdc.impl.join.snm.SNMJoin_v1;
import cdc.impl.resultsavers.CSVFileSaver;
import cdc.utils.RJException;

public class FRILLinker {

	
	
	/**
	 * 
	 * @param parResultDBC - The database connect to store the result
	 * @param parResultTable - Name of the table used to store the result (temp)
	 * @param parMappedPair - Mapped columns
	 *	 
	 **/
	
	
	public static long t1;
	
	private String strConfigFilePath;
	
	private WeightIdentifier weightIdentifer;
	private JoinMethod joinMethod;
	private String acceptanceLevel;
	
	private String[] strSNMOrder;
	private int intSNMWindowSize;
	
	private String strMaxCPU = "999";
	
	private String SOURCENAME_A = "SourceA";
	private String SOURCENAME_B = "SourceB";
	
	public static final int ATTRIBUTE_DEDUCTION = -1;
	public static final int DISTANCE_IMPUTATION = -2;
	public static final int BACKUP_FULL = -3;
	public static final int BACKUP_OPTIMAL = -4;
	
	//Data Sampling
	private SamplingMethod samplingMethod;
	DataSampleInterface SourceInterfaceA;
	DataSampleInterface SourceInterfaceB;
	
	private String MySQL_JDBCDriver = "com.mysql.jdbc.Driver";
	
	private DBMS dbms;
	
	private XML_Reader xmlr;
	private ArrayList<MappedPair> mappedPairs;
	private ArrayList<MappedPair> mappedPairs_backup;
	private ArrayList<MappedPair> mappedPairs_select;
	
	private Map A_params;
	private Map B_params;
	private AbstractDataSource A_DataSource;
	private AbstractDataSource B_DataSource;
	
	private AbstractDataSource A_DataSource_Backup;
	private AbstractDataSource B_DataSource_Backup;
	
	private AbstractDataSource A_EMDataSource;
	private AbstractDataSource A_EMDataSource_Backup;
	private AbstractDataSource B_EMDataSource;
	private AbstractDataSource B_EMDataSource_Backup;
	
	private ConverterColumnWrapper[] outFormat;
	private int intOutFormatCount;
	
	private JDBCDataColumnDefinition[] A_outputColumnDefinitions;
	private JDBCDataColumnDefinition[] A_EMoutputColumnDefinitions;
	private JDBCDataColumnDefinition[] A_outputColumnDefinitions_Backup;
	private JDBCDataColumnDefinition[] B_outputColumnDefinitions;
	private JDBCDataColumnDefinition[] B_EMoutputColumnDefinitions;
	private JDBCDataColumnDefinition[] B_outputColumnDefinitions_Backup;
	
	private ConverterColumnWrapper[] A_outputWrapperColumnDefinitions;
	private ConverterColumnWrapper[] A_outputWrapperColumnDefinitions_Backup;
	private ConverterColumnWrapper[] B_outputWrapperColumnDefinitions;
	private ConverterColumnWrapper[] B_outputWrapperColumnDefinitions_Backup;
	
	private ModelGenerator A_ModelGenerator;
	private ModelGenerator A_EMModelGenerator;
	private ModelGenerator A_ModelGenerator_Backup;
	private ModelGenerator B_ModelGenerator;
	private ModelGenerator B_EMModelGenerator;
	private ModelGenerator B_ModelGenerator_Backup;
	
	private ArrayList<ImputationRule> ImputationRuleSet =new ArrayList<ImputationRule>();
	
	private AbstractDistance[] distances;
	private AbstractDistance[] distances_backup;
	
	private double[] weights;
	private double[] weights_backup;
	
	private double dblPPRLThreshold = 0;
	private int intBloomFilterSize = 100;
	
	private int intRandomSampleSize = 100;
	
	double EMWeightsDouble[] = null;
	double EMWeightsDouble_Backup[] = null;

	
	private double[] emptyValues;
	private S_WeightedJoinCondition condition;
	private S_WeightedJoinCondition condition_Backup;
	private AbstractJoin join;
	
	private AbstractResultsSaver saver;
	
	private ConfiguredSystem system;
	
	private S_EMThread EMWeightGenerator;
	private S_EMThread EMWeightGenerator_Backup;
	
	private MappedPair blockPair;
	private MappedPair linkPair;
	private MappedPair EMblockPair;
	private int intBlockPairIndex;
	private int intEMBlockPairIndex;
	
	private int intMissingDataValue;
	
	private S_EMResultsReporter EMreporter;
	
	public FRILLinker(String parConfigFilePath){

		strConfigFilePath = parConfigFilePath;
		
	}
	
	boolean tryParseInt(String value)   
	{   
	     try   
	     {   
	         Integer.parseInt(value);   
	         return true;   
	      } catch(NumberFormatException nfe)   
	      {   
	          return false;   
	      }   
	} 

	boolean tryParseDouble(String value)   
	{   
	     try   
	     {   
	         Double.parseDouble(value);   
	         return true;   
	      } catch(NumberFormatException nfe)   
	      {   
	          return false;   
	      }   
	}
	public void link(){

		//Read the data about the data sources and linkage information from XML
		readDataFromXML();
		
		//Process sampling data sources
		if(samplingMethod.Method==SamplingMethod.TOP){
			
		}
		
		if(intMissingDataValue == this.BACKUP_FULL || intMissingDataValue == this.BACKUP_OPTIMAL)
		{
			preprocess();
		}
		
		//setup the data sources from the info read from the XML file
		setupDatasources();
		
		//Setup the linkage information
		setupLikageConfig();
		
		if(intMissingDataValue == this.DISTANCE_IMPUTATION || intMissingDataValue == this.ATTRIBUTE_DEDUCTION)
		{
			preprocess();
		}
		
		//Setup result saver
		setupResultSaver();
		
		//final setup
		finalLinkageSetup();
		
		//Start the linkage process
		start();
	}
	
	//Read the database information from the XML file
	
	private void readDataFromXML(){
		
		xmlr = new XML_Reader(strConfigFilePath);
		
		weightIdentifer = xmlr.getWeightIdentifier();
		joinMethod = xmlr.getJoinMethod();
		
		strMaxCPU = xmlr.getMaxCPU();
		
		acceptanceLevel = xmlr.getAcceptanceLevel();
		
		//Check acceptanceLevel
		try{
			double dblAcceptanceLevel = Double.parseDouble(acceptanceLevel);
			if(dblAcceptanceLevel<0 || dblAcceptanceLevel>100){
				MyUtilities.end("Acceptance level must be between 0 and 100.");
			}
		}catch(NumberFormatException nfe){
			MyUtilities.end("Acceptance level is not in correct format. Acceptance level should be a double number");
		}
		
		//Check weight identifiers
		if(weightIdentifer!=null){
			//if(weightIdentifer!=WeightIdentifier.MANUAL && weightIdentifer!=WeightIdentifier.EM){
			if(!WeightIdentifier.isMember(weightIdentifer)){
				MyUtilities.end("Weight identifier is not supported.");
			}
		}else{
			MyUtilities.end("Weight identifier is missing.");
		}
		
		//Check join method
		if(joinMethod!=null){
			if(!JoinMethod.isMember(joinMethod)){
				MyUtilities.end("The join method is not supported.");
			}
		}else{
			MyUtilities.end("Join method is missing.");
		}
		
		dbms = xmlr.getDBMS();
		
		if(dbms==null){
			MyUtilities.end("DBMS is not provided or not supported.");
		}

		//This section sets up the datasource definition for the linkage
		mappedPairs = xmlr.getMappedPairs();
		mappedPairs_backup = xmlr.getBackupMappedPairs();
		
		//getMissingDataValue
		intMissingDataValue = 0;
		String tempMissing = xmlr.getMissingValue();
		if(tempMissing!=null){
			if(tryParseInt(tempMissing)){
				intMissingDataValue = Integer.parseInt(tempMissing);
				if(intMissingDataValue<0){
					intMissingDataValue = 0;
				}else if(intMissingDataValue>100){
					intMissingDataValue = 100;
				}
			}else{
				if(tempMissing.toUpperCase().equals("ATTRIBUTE_DEDUCTION")){
					intMissingDataValue = this.ATTRIBUTE_DEDUCTION;
				}else if(tempMissing.toUpperCase().equals("DISTANCE_IMPUTATION")){
					intMissingDataValue = this.DISTANCE_IMPUTATION;
				}else if(tempMissing.toUpperCase().equals("BACKUP_OPTIMAL")){
					intMissingDataValue = this.BACKUP_OPTIMAL;
				}else if(tempMissing.toUpperCase().equals("BACKUP_FULL")){
					intMissingDataValue = this.BACKUP_FULL;
				}else{
					MyUtilities.end("Missing value handling method is not supported");
				}
			}
		}
		
		//get Sampling method
		samplingMethod = xmlr.getSamplingMethod();
		
		//get blocking attribute
		blockPair = xmlr.getBlockingPair();
		
		//get lingking attribute
		linkPair = xmlr.getLinkingPair();

		//Identify the index of the blocking pair
		intBlockPairIndex = -1;
		if(blockPair!=null){
			for(int i=0; i<mappedPairs.size();i++){
				if(mappedPairs.get(i).getColA().equals(blockPair.getColA()) && mappedPairs.get(i).getColB().equals(blockPair.getColB())){
					intBlockPairIndex = i;
					break;
				}
			}
		}
		
		//get PPRL Distance threshold
		String strTempPPRLThreshold = xmlr.getPPRLThreshold();
		if(strTempPPRLThreshold != null){
			if(tryParseDouble(strTempPPRLThreshold)){
				dblPPRLThreshold = Double.parseDouble(strTempPPRLThreshold);
			}
		}
		
		//get Random Sample Size		
		String strTempRandomSampleSize = xmlr.getRandomSampleSize();
		if(strTempRandomSampleSize != null){
			if(tryParseInt(strTempRandomSampleSize)){
				intRandomSampleSize = Integer.parseInt(strTempRandomSampleSize);
			}
		}
		
		//get Bloom filter size
		String strTempBloomSize = xmlr.getBloomFilterSize();
		if(strTempBloomSize != null){
			if(tryParseInt(strTempBloomSize)){
				intBloomFilterSize = Integer.parseInt(strTempBloomSize);
			}
		}
		
		
		//get blocking attribute
		EMblockPair = xmlr.getEMBlockingPair();
		
		//Identify the index of the EM blocking pair
		intEMBlockPairIndex = -1;
		if(EMblockPair!=null){
			for(int i=0; i<mappedPairs.size();i++){
				if(mappedPairs.get(i).getColA().equals(EMblockPair.getColA()) && mappedPairs.get(i).getColB().equals(EMblockPair.getColB())){
					intEMBlockPairIndex = i;
					break;
				}
			}
		}
		
		String A_databaseString = xmlr.getDatabaseName(XML_Reader.SOURCEA);
		
		// set parameters for source A
		A_params = new HashedMap();
		
		if(dbms.equals(DBMS.SQLServer)){
			A_params.put("source-name", SOURCENAME_A);
			A_params.put("url", xmlr.getURL(XML_Reader.SOURCEA));
			A_params.put("database", A_databaseString);
			A_params.put("driver", MySQL_JDBCDriver);
			A_params.put("table", xmlr.getTableName(XML_Reader.SOURCEA));
			
			
			//Create Test Select string
			A_params.put("user", xmlr.getUser(XML_Reader.SOURCEA));
			A_params.put("password", xmlr.getPassword(XML_Reader.SOURCEA));
			
			//Add link field for source A
			//if(linkPair!=null){
			//	A_params.put(rosita.linkage.tools.JDBCDataSource.LINK_FIELD, linkPair.getColA());
			//}
			
			
			// set parameters for source B
			B_params = new HashedMap();
			B_params.put("source-name", SOURCENAME_B);
			
			String B_databaseString = xmlr.getDatabaseName(XML_Reader.SOURCEB);
			
			B_params.put("url", xmlr.getURL(XML_Reader.SOURCEB));
			B_params.put("database", B_databaseString);
			B_params.put("driver", MySQL_JDBCDriver);
			B_params.put("table", xmlr.getTableName(XML_Reader.SOURCEB));
			
			
			//Create Test Select string
			B_params.put("user", xmlr.getUser(XML_Reader.SOURCEB));
			B_params.put("password", xmlr.getPassword(XML_Reader.SOURCEB));
		}else{
			A_params.put("source-name", SOURCENAME_A);
			A_params.put("url", xmlr.getURL(XML_Reader.SOURCEA)+A_databaseString);
			A_params.put("driver", MySQL_JDBCDriver);
			A_params.put("table", xmlr.getTableName(XML_Reader.SOURCEA));
			
			
			//Create Test Select string
			A_params.put("user", xmlr.getUser(XML_Reader.SOURCEA));
			A_params.put("password", xmlr.getPassword(XML_Reader.SOURCEA));
			
			//Add link field for source A
			//if(linkPair!=null){
			//	A_params.put(rosita.linkage.tools.JDBCDataSource.LINK_FIELD, linkPair.getColA());
			//}
			
			
			// set parameters for source B
			B_params = new HashedMap();
			B_params.put("source-name", SOURCENAME_B);
			
			String B_databaseString = xmlr.getDatabaseName(XML_Reader.SOURCEB);
			
			B_params.put("url", xmlr.getURL(XML_Reader.SOURCEB)+B_databaseString);
			B_params.put("driver", MySQL_JDBCDriver);
			B_params.put("table", xmlr.getTableName(XML_Reader.SOURCEB));
			
			
			//Create Test Select string
			B_params.put("user", xmlr.getUser(XML_Reader.SOURCEB));
			B_params.put("password", xmlr.getPassword(XML_Reader.SOURCEB));
		}
		
		// What kind of DBMS the system is trying to connect to?
		if(dbms==DBMS.MySQL){
			String A_testSelectString = "SELECT * FROM "+xmlr.getTableName(XML_Reader.SOURCEA);
			A_params.put("columns-select", A_testSelectString);

			String B_testSelectString = "SELECT * FROM "+xmlr.getTableName(XML_Reader.SOURCEB);
			B_params.put("columns-select", B_testSelectString);

		}else if(dbms==DBMS.PostgreSQL || dbms==DBMS.SQLServer){
			String A_testSelectString = "SELECT * FROM "+xmlr.getSchema(XML_Reader.SOURCEA)+"."+xmlr.getTableName(XML_Reader.SOURCEA);
			A_params.put("columns-select", A_testSelectString);

			String B_testSelectString = "SELECT * FROM "+xmlr.getSchema(XML_Reader.SOURCEB)+"."+xmlr.getTableName(XML_Reader.SOURCEB);
			B_params.put("columns-select", B_testSelectString);

			
			A_params.put("schema", xmlr.getSchema(XML_Reader.SOURCEA));
			B_params.put("schema", xmlr.getSchema(XML_Reader.SOURCEB));
		}else{
			//If the DBMS is not supported the system will exit
			MyUtilities.end("The databased management system indicated is currently not supported. Only MySQL and PostgreSQL are currently supported.");
		}
		
		//Add link field for source B
		//if(linkPair!=null){
		//	B_params.put(rosita.linkage.tools.JDBCDataSource.LINK_FIELD, linkPair.getColB());
		//}
		
	}
	
	//Process Sample
	private void processSamples(){
		
		//SourceInterfaceA = getSampler(source, n)
	}
	
	private DataSampleInterface getSampler(AbstractDataSource source, int n) throws IOException, RJException {
		if (samplingMethod.Method== SamplingMethod.TOP) {
			return new FirstNSampler(source, n);
		} else if(samplingMethod.Method== SamplingMethod.RANDOM){
			return new RandomSampler(source, n);
		}else{
			return null;
		}
	}
	
	
	//Set up the datasources
	
	private void setupDatasources(){
		
		try{
			
			//Setup A data source
			//Different classes for different types of DBMS
			
			if(dbms==DBMS.MySQL){
				A_DataSource = new JDBCDataSource(SOURCENAME_A, A_params);
				A_EMDataSource = new S_JDBCDataSource(SOURCENAME_A, A_params);
			}else{
				A_DataSource = new S_Postgre_JDBCDataSource(SOURCENAME_A, A_params);
				
				//This is not correct 
				//TODO: construct new class of JDBC for PostgreSQL
				A_EMDataSource = new S_JDBCDataSource(SOURCENAME_A, A_params);
			}
			
			A_outputColumnDefinitions = new JDBCDataColumnDefinition[mappedPairs.size()];
			A_outputWrapperColumnDefinitions = new ConverterColumnWrapper[mappedPairs.size()];


			B_outputColumnDefinitions = new JDBCDataColumnDefinition[mappedPairs.size()];
			B_outputWrapperColumnDefinitions = new ConverterColumnWrapper[mappedPairs.size()];
			
			
			
			if(linkPair==null){
				outFormat = new ConverterColumnWrapper[mappedPairs.size()*2];
			}
			else{
				outFormat = new ConverterColumnWrapper[2];
				
				A_outputColumnDefinitions = new JDBCDataColumnDefinition[mappedPairs.size() + (mappedPairs_backup==null?0:mappedPairs_backup.size()) + 1];
				A_outputWrapperColumnDefinitions = new ConverterColumnWrapper[mappedPairs.size()];

				B_outputColumnDefinitions = new JDBCDataColumnDefinition[mappedPairs.size() + (mappedPairs_backup==null?0:mappedPairs_backup.size()) + 1];
				B_outputWrapperColumnDefinitions = new ConverterColumnWrapper[mappedPairs.size()];
			}
			intOutFormatCount = 0;
			
			//Define output columns of the data source

			//Add link field to the output 
			for(int i=0; i<mappedPairs.size();i++){
				A_outputColumnDefinitions[i] = new JDBCDataColumnDefinition(mappedPairs.get(i).getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, mappedPairs.get(i).getColA());
				A_outputWrapperColumnDefinitions[i] = new ConverterColumnWrapper(A_outputColumnDefinitions[i]);
				
				if(linkPair==null)
				{
					outFormat[intOutFormatCount] = new ConverterColumnWrapper(new JDBCDataColumnDefinition(mappedPairs.get(i).getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, mappedPairs.get(i).getColA()));
					intOutFormatCount++;
				}
			}
			
			//Add backup fields
			if(mappedPairs_backup!=null){
				for(int i=0; i<mappedPairs_backup.size();i++){
					A_outputColumnDefinitions[mappedPairs.size()+i] = new JDBCDataColumnDefinition(mappedPairs_backup.get(i).getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, mappedPairs_backup.get(i).getColA());
					//A_outputWrapperColumnDefinitions[mappedPairs.size()+i] = new ConverterColumnWrapper(A_outputColumnDefinitions[mappedPairs.size()+i]);
				}
			}
			

			if(linkPair!=null){
				A_outputColumnDefinitions[A_outputColumnDefinitions.length-1] = new JDBCDataColumnDefinition(linkPair.getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, linkPair.getColA());
				//A_outputWrapperColumnDefinitions[mappedPairs.size()] = new ConverterColumnWrapper(A_outputColumnDefinitions[mappedPairs.size()]);

				outFormat[intOutFormatCount] = new ConverterColumnWrapper(new JDBCDataColumnDefinition(linkPair.getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, linkPair.getColA()));
				intOutFormatCount++;
			}
			
			//set up EM column definitions
			if(linkPair!=null){
				A_EMoutputColumnDefinitions = new JDBCDataColumnDefinition[A_outputColumnDefinitions.length-1];
				for (int k=0;k<A_outputColumnDefinitions.length-1;k++){
					A_EMoutputColumnDefinitions[k] = A_outputColumnDefinitions[k];
				}
			}else{
				A_EMoutputColumnDefinitions = A_outputColumnDefinitions;
			}
			
			A_ModelGenerator= new ModelGenerator(A_outputColumnDefinitions);
			A_EMModelGenerator = new ModelGenerator(A_EMoutputColumnDefinitions);
			A_DataSource.setModel(A_ModelGenerator);
			A_EMDataSource.setModel(A_EMModelGenerator);
			
			//Setup B data source
			if(dbms == DBMS.MySQL){
				B_DataSource = new JDBCDataSource(SOURCENAME_B, B_params);
				B_EMDataSource = new S_JDBCDataSource(SOURCENAME_B, B_params);
			}else{
				//PostgreSQL
				B_DataSource = new S_Postgre_JDBCDataSource(SOURCENAME_B, B_params);
				
				//This is not correct 
				//TODO: construct new class of JDBC for PostgreSQL
				B_EMDataSource = new S_JDBCDataSource(SOURCENAME_B, B_params);
			}
			//Define output columns of the data source			
			
			
			for(int i=0; i<mappedPairs.size();i++){
				B_outputColumnDefinitions[i] = new JDBCDataColumnDefinition(mappedPairs.get(i).getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, mappedPairs.get(i).getColB());
				B_outputWrapperColumnDefinitions[i] = new ConverterColumnWrapper(B_outputColumnDefinitions[i]);
				
				if(linkPair==null){
					outFormat[intOutFormatCount] = new ConverterColumnWrapper(new JDBCDataColumnDefinition(mappedPairs.get(i).getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, mappedPairs.get(i).getColB()));
					intOutFormatCount++;
				}
			}
			
			//Add backup fields
			if(mappedPairs_backup!=null){
				for(int i=0; i<mappedPairs_backup.size();i++){
					B_outputColumnDefinitions[mappedPairs.size()+i] = new JDBCDataColumnDefinition(mappedPairs_backup.get(i).getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, mappedPairs_backup.get(i).getColB());
					//B_outputWrapperColumnDefinitions[mappedPairs.size()+i] = new ConverterColumnWrapper(B_outputColumnDefinitions[mappedPairs.size()+i]);
				}
			}
			
			if(linkPair!=null){
				B_outputColumnDefinitions[B_outputColumnDefinitions.length-1] = new JDBCDataColumnDefinition(linkPair.getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, linkPair.getColB());
				//B_outputWrapperColumnDefinitions[mappedPairs.size()] = new ConverterColumnWrapper(B_outputColumnDefinitions[mappedPairs.size()]);
				
				outFormat[intOutFormatCount] = new ConverterColumnWrapper(new JDBCDataColumnDefinition(linkPair.getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, linkPair.getColB()));
				intOutFormatCount++;
			}
			
			//set up EM column definitions
			if(linkPair!=null){
				B_EMoutputColumnDefinitions = new JDBCDataColumnDefinition[B_outputColumnDefinitions.length-1];
				for (int k=0;k<B_outputColumnDefinitions.length-1;k++){
					B_EMoutputColumnDefinitions[k] = B_outputColumnDefinitions[k];
				}
			}else{
				B_EMoutputColumnDefinitions = B_outputColumnDefinitions;
			}
			
			B_ModelGenerator= new ModelGenerator(B_outputColumnDefinitions);
			B_EMModelGenerator= new ModelGenerator(B_EMoutputColumnDefinitions);
			B_DataSource.setModel(B_ModelGenerator);
			B_EMDataSource.setModel(B_EMModelGenerator);
			
			//Prepare backup field
			if(mappedPairs_select!=null){
				
				A_EMDataSource_Backup = new S_JDBCDataSource(SOURCENAME_A, A_params);
				B_EMDataSource_Backup = new S_JDBCDataSource(SOURCENAME_B, B_params);

				A_outputColumnDefinitions_Backup = new JDBCDataColumnDefinition[mappedPairs_select.size()];
				A_outputWrapperColumnDefinitions_Backup = new ConverterColumnWrapper[mappedPairs_select.size()];

				B_outputColumnDefinitions_Backup = new JDBCDataColumnDefinition[mappedPairs_select.size()];
				B_outputWrapperColumnDefinitions_Backup = new ConverterColumnWrapper[mappedPairs_select.size()];

				for(int i=0; i<mappedPairs_select.size();i++){
					A_outputColumnDefinitions_Backup[i] = new JDBCDataColumnDefinition(mappedPairs_select.get(i).getColA(), DataColumnDefinition.TYPE_STRING, SOURCENAME_A, mappedPairs_select.get(i).getColA());
					A_outputWrapperColumnDefinitions_Backup[i] = new ConverterColumnWrapper(A_outputColumnDefinitions_Backup[i]);
					
					B_outputColumnDefinitions_Backup[i] = new JDBCDataColumnDefinition(mappedPairs_select.get(i).getColB(), DataColumnDefinition.TYPE_STRING, SOURCENAME_B, mappedPairs_select.get(i).getColB());
					B_outputWrapperColumnDefinitions_Backup[i] = new ConverterColumnWrapper(B_outputColumnDefinitions_Backup[i]);
				}
				
				A_ModelGenerator_Backup = new ModelGenerator(A_outputColumnDefinitions_Backup);
				A_EMDataSource_Backup.setModel(A_ModelGenerator_Backup);
				
				B_ModelGenerator_Backup = new ModelGenerator(B_outputColumnDefinitions_Backup);
				B_EMDataSource_Backup.setModel(B_ModelGenerator_Backup);

				//A_ModelGenerator_Backup = new ModelGenerator(A_outputColumnDefinitions_Backup);
				//A_DataSource_Backup.setModel(A_ModelGenerator_Backup);
				
				//B_ModelGenerator_Backup = new ModelGenerator(B_outputColumnDefinitions_Backup);
				//B_DataSource_Backup.setModel(B_ModelGenerator_Backup);

			}
			
		}catch(RJException myException){
			myException.printStackTrace();
		}
	}
	
	// Define how the data will be linked
	
	private void setupLikageConfig(){
		
		
		//Set up the linkage condition
		distances = new AbstractDistance[mappedPairs.size()];
		weights = new double[mappedPairs.size()];
		emptyValues = new double[mappedPairs.size()];
		double ManualSumWeight = 0;
		
		for(int i=0; i<mappedPairs.size();i++){
			distances[i] = generateDistance(mappedPairs.get(i).getAlgorithm(), mappedPairs.get(i));
			weights[i] = mappedPairs.get(i).getWeight();
			emptyValues[i] = intMissingDataValue;
			ManualSumWeight+=weights[i];
		}
		
		if(xmlr.getWeightIdentifier()==WeightIdentifier.MANUAL){
			if(ManualSumWeight!=1){
				//MyUtilities.end("The sum of manual weights must be equal to 1.");
			}
		}
		
		if(mappedPairs_backup.size()>0){
			distances_backup = new AbstractDistance[mappedPairs_select.size()];
			weights_backup = new double[mappedPairs_select.size()];
			
			for(int i=0; i<mappedPairs_select.size();i++){
				distances_backup[i] = generateDistance(mappedPairs_select.get(i).getAlgorithm(), mappedPairs_select.get(i));
				weights_backup[i] = mappedPairs_select.get(i).getWeight();
			}
		}
		
		Map conditionProperties = new HashedMap();
		conditionProperties.put(WeightedJoinCondition.PROP_ACCEPTANCE_LEVEL, acceptanceLevel);
		condition = new S_WeightedJoinCondition(A_outputWrapperColumnDefinitions, B_outputWrapperColumnDefinitions,A_outputWrapperColumnDefinitions_Backup, B_outputWrapperColumnDefinitions_Backup, distances, distances_backup, weights, weights_backup, emptyValues, dblPPRLThreshold, intBloomFilterSize, ImputationRuleSet, conditionProperties);
		
		Map conditionProperties_Backup = new HashedMap();
		
		conditionProperties_Backup.put(WeightedJoinCondition.PROP_ACCEPTANCE_LEVEL, acceptanceLevel);
		condition_Backup = new S_WeightedJoinCondition(A_outputWrapperColumnDefinitions_Backup, B_outputWrapperColumnDefinitions_Backup, A_outputWrapperColumnDefinitions_Backup, B_outputWrapperColumnDefinitions_Backup, distances_backup, distances_backup, weights_backup, weights_backup, emptyValues, dblPPRLThreshold, intBloomFilterSize, ImputationRuleSet, conditionProperties);

		
		//if EM is used to generate the weights
		if (weightIdentifer.equals(WeightIdentifier.EM) == true){
			
			EMWeightGenerator = new S_EMThread(A_EMDataSource, B_EMDataSource, condition, intEMBlockPairIndex, intRandomSampleSize);
			EMWeightGenerator.start();

			try {
				EMWeightGenerator.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int EMWeights[] = EMWeightGenerator.getFinalWeights();
			
			EMWeightsDouble = new double[EMWeights.length];
			
			System.out.println("Final weights: ");

			for(int i = 0; i<EMWeights.length;i++){
				EMWeightsDouble[i] = Double.parseDouble(String.valueOf(EMWeights[i])) / 100;
				System.out.println(EMWeightsDouble[i]);
			}
			
			//EM for backup fields
			if(intMissingDataValue==this.BACKUP_OPTIMAL || intMissingDataValue==this.BACKUP_FULL){
				EMWeightGenerator_Backup = new S_EMThread(A_EMDataSource_Backup, B_EMDataSource_Backup, condition_Backup, intEMBlockPairIndex, intRandomSampleSize);
				EMWeightGenerator_Backup.start();

				try {
					EMWeightGenerator_Backup.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				int EMWeights_Backup[] = EMWeightGenerator_Backup.getFinalWeights();
				EMWeightsDouble_Backup = new double[EMWeights_Backup.length];
				System.out.println("Final backup weights: ");

				for(int i = 0; i<EMWeightsDouble_Backup.length;i++){
					EMWeightsDouble_Backup[i] = Double.parseDouble(String.valueOf(EMWeights_Backup[i])) / 100;
					System.out.println(EMWeightsDouble_Backup[i]);
				}
			}

			condition = new S_WeightedJoinCondition(A_outputWrapperColumnDefinitions, B_outputWrapperColumnDefinitions, A_outputWrapperColumnDefinitions_Backup, B_outputWrapperColumnDefinitions_Backup ,distances, distances_backup, EMWeightsDouble, EMWeightsDouble_Backup, emptyValues, dblPPRLThreshold, intBloomFilterSize, ImputationRuleSet, conditionProperties);
		}
		
		t1 = System.currentTimeMillis();
			
			
					
	}
	
	private int getKeyIndex(String parKey){
		int result = -1;
		for(int i=0; i<mappedPairs.size(); i++){
			if(mappedPairs.get(i).getAttribute().equals(parKey)){
				result = i;
				break;
			}
		}
		return result;
	}
	
	//Set up the result saver
	//result can be saved to both CSV file or 
	
	private void setupResultSaver(){
		
		try{
			
			if(linkPair==null){
				Map saverProps = new HashedMap();
				saverProps.put("output-file", "i:\\results.csv");
				saverProps.put("encoding", "UTF-8");
				
				saver = new CSVFileSaver(saverProps);
			}else{
				String resultTableName = strConfigFilePath.split("/")[strConfigFilePath.split("/").length-1].split("[.]")[0].toLowerCase();
				
				
				String leftTableName = xmlr.getTableName(XML_Reader.SOURCEA);
				String leftDatabaseName = xmlr.getDatabaseName(XML_Reader.SOURCEA);
				
				String leftLink = linkPair.getColA();
				
				String rightTableName = xmlr.getTableName(XML_Reader.SOURCEB);
				String rightDatabaseName = xmlr.getDatabaseName(XML_Reader.SOURCEB);
				
				String rightLink = linkPair.getColB();
				
				Map saverProps = new HashedMap();
				saverProps.put(MySQLSaver.TARGET_TABLE_NAME, resultTableName);
				
				
				saverProps.put(MySQLSaver.LEFT_TABLE_NAME, leftTableName);
				saverProps.put(MySQLSaver.LEFT_DATABASE_NAME, leftDatabaseName);
				saverProps.put(MySQLSaver.LEFT_LINK_NAME, leftLink);

				saverProps.put(MySQLSaver.RIGHT_TABLE_NAME, rightTableName);
				saverProps.put(MySQLSaver.RIGHT_DATABASE_NAME, rightDatabaseName);
				saverProps.put(MySQLSaver.RIGHT_LINK_NAME, rightLink);
				
				if(dbms == DBMS.MySQL){
					
					saver = new MySQLSaver(saverProps, xmlr.getDatabaseConnection(XML_Reader.SAVER));
				}else{

					//PostgreSQL
					
					
					String leftSchema = xmlr.getSchema(XML_Reader.SOURCEA);
					String resultSchema = xmlr.getSchema(XML_Reader.SAVER);
					String rightSchema = xmlr.getSchema(XML_Reader.SOURCEB);
					
					saverProps.put(MySQLSaver.TARGET_SCHEMA, resultSchema);
					saverProps.put(MySQLSaver.LEFT_SCHEMA, leftSchema);
					saverProps.put(MySQLSaver.RIGHT_SCHEMA, rightSchema);
					
					//saverProps.put(MySQLSaver.TARGET_TABLE_NAME, resultTableName);
					
					saver = new PostgreSQLSaver(saverProps, xmlr.getDatabaseConnection(XML_Reader.SAVER));
				}

			}
		}catch(RJException myException){
			myException.printStackTrace();
		}
	}
	

	public Boolean compareImputationRules(ImputationRule rule1, ImputationRule rule2){
		Boolean result = true;
		
		for (int i:rule2.FieldsWithData){
			if(!rule1.FieldsWithData.contains(i)){
				result = false;
			}
		}

		for (int i:rule1.FieldsWithData){
			if(!rule2.FieldsWithData.contains(i)){
				result = false;
			}
		}
		
		if(rule1.FieldWithMissingData!=rule2.FieldWithMissingData){
			result = false;
		}

		return result;
	}
	
	public ImputationRule createImputationRule(ArrayList<String[][]> parDataA, ArrayList<String[][]> parDataB, ArrayList<Integer> parFieldsWithData, int parFieldWithMissingData){
		ImputationRule result = new ImputationRule();
		int LocalThreshold = 80;
		
		int MatchQty = 0;
		int TotalQty = 0;
		int NonMatchQty = 0;
		
		for(int i = 0; i<parDataA.size(); i++){
			for (int j=0; j<parDataB.size();j++){
				
				//Check for null values
				Boolean isNull = false;
				for (int k=0; k<parDataA.get(i).length;k++){
					if(parDataA.get(i)[1][k].equals("") || parDataA.get(i)[1][k].equals("")){
						isNull = true;
					}
				}
				
				if(isNull== false){
					Boolean isMatch = true;
					DataCell dtaCellA;
					DataCell dtaCellB;
					DataCell dtaMissingA;
					DataCell dtaMissingB;
					
					for(int k=0;k<parFieldsWithData.size();k++){
						dtaCellA = new DataCell(DataColumnDefinition.TYPE_STRING, parDataA.get(i)[1][parFieldsWithData.get(k)]);
						dtaCellB = new DataCell(DataColumnDefinition.TYPE_STRING, parDataB.get(j)[1][parFieldsWithData.get(k)]);
						
						if(distances[parFieldsWithData.get(k)].distance(dtaCellA, dtaCellB)< LocalThreshold){
							isMatch = false;
						}
					}
					
					if(isMatch == true){
						
						TotalQty++;
						dtaMissingA = new DataCell(DataColumnDefinition.TYPE_STRING, parDataA.get(i)[1][parFieldWithMissingData]);
						dtaMissingB = new DataCell(DataColumnDefinition.TYPE_STRING, parDataA.get(j)[1][parFieldWithMissingData]);
	
						if(distances[parFieldWithMissingData].distance(dtaMissingA, dtaMissingB)>LocalThreshold){
							MatchQty++;
						}else{
							NonMatchQty++;
						}
					}
				}
			}
		}
		
		result.FieldsWithData = parFieldsWithData;
		result.FieldWithMissingData = parFieldWithMissingData;
		
		
		if ((double)MatchQty/TotalQty>0.8){
			result.ImputedValue = 100;
		}else{
			result.ImputedValue = 0;
		}
		
		return result;
	}
	
	//Preprocessing
	//Task 1: Backup Column Selection (OPTIMAL Backup)
	public void preprocess(){
		
		if(intMissingDataValue == this.DISTANCE_IMPUTATION){
			
			//Create special join for Imputation rules
			try {
				AbstractJoin myIRJoin;
				IRCondition myIRCondition;
				
				HashedMap IRConditioProps = new HashedMap();
				IRConditioProps.put(WeightedJoinCondition.PROP_ACCEPTANCE_LEVEL, acceptanceLevel);
				
				HashedMap IRJoinProps = new HashedMap();
				
				myIRCondition = new IRCondition(A_outputWrapperColumnDefinitions, B_outputWrapperColumnDefinitions, distances, EMWeightsDouble, emptyValues, IRConditioProps);
				myIRJoin = new IRJoin(A_EMDataSource, B_EMDataSource, outFormat, myIRCondition, IRJoinProps);
				
				ConfiguredSystem IRSystem = new ConfiguredSystem(A_EMDataSource, B_EMDataSource, myIRJoin, null);
				IRGenerator ruleGenerator = new IRGenerator(IRSystem, mappedPairs, xmlr, distances, EMWeightsDouble);
				ImputationRuleSet = ruleGenerator.generate();
				
				/*for (int t=0;t<ImputationRuleSet.size();t++){
					if(ImputationRuleSet.get(t).ImputedValue==-1){
						if((double)ImputationRuleSet.get(t).intUp/ImputationRuleSet.get(t).intDown>0.8){
							ImputationRuleSet.get(t).ImputedValue = 100;
						}else{
							ImputationRuleSet.get(t).ImputedValue = 0;
						}
					}
					ImputationRuleSet.get(t).printContent();
				}
				
				int oo = 0;
				oo++;*/
				
			} catch (RJException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
			//Load the data
			
			//ImputationRuleGenerator ruleGenerator = new ImputationRuleGenerator(mappedPairs, xmlr, distances, EMWeightsDouble);
			//ruleGenerator.generate();
			
			/*long t5 = System.currentTimeMillis();
			String strFieldListA = "";
			String strWhereA = "";
			
			ArrayList<String[][]> sourceADataFull = new ArrayList<String[][]>();
			ArrayList<String[][]> sourceBDataFull = new ArrayList<String[][]>();
			
			
			for (int i=0; i<mappedPairs.size(); i++){
				strFieldListA += mappedPairs.get(i).getColA().trim() + ",";
				strWhereA = mappedPairs.get(i).getColA().trim() + " Is not null ,";
			}
			
			strFieldListA = strFieldListA.substring(0, strFieldListA.length()-1);
			strWhereA = strWhereA.substring(0, strWhereA.length()-1);
			
			//Create field list
			String strFieldListB = "";
			String strWhereB = "";
			
			double TotalDataWeights = 0;
			double TotalMissingWeights = 0;
			
			for (int i=0; i<mappedPairs.size(); i++){
				strFieldListB += mappedPairs.get(i).getColB().trim() + ",";
				strWhereB = mappedPairs.get(i).getColB().trim() + " Is not null ,";
			}
	
			strFieldListB = strFieldListB.substring(0, strFieldListB.length()-1);
			strWhereB = strWhereB.substring(0, strWhereB.length()-1);
			
			DatabaseConnection dbcA = xmlr.getDatabaseConnection(xmlr.SOURCEA);
			DatabaseConnection dbcB = xmlr.getDatabaseConnection(xmlr.SOURCEB);

			ResultSet sourceARowsFull =  dbcA.getTableQuery("SELECT "+strFieldListA+" FROM "+xmlr.getTableName(xmlr.SOURCEA));
			ResultSet sourceBRowsFull =  dbcB.getTableQuery("SELECT "+strFieldListB+" FROM "+xmlr.getTableName(xmlr.SOURCEB));
			
			String[][] dataRowA;
			while ((dataRowA = dbcA.getNextResultWithColName(sourceARowsFull)) != null) {
				sourceADataFull.add(dataRowA);
			}
			
			String[][] dataRowB;
			while ((dataRowB = dbcB.getNextResultWithColName(sourceBRowsFull)) != null) {
				sourceBDataFull.add(dataRowB);
			}
			
			for (int i1=0; i1<sourceADataFull.size();i1++){
				for(int i2=0;i2<sourceBDataFull.size();i2++){
				
					//Find fields with data and missing data
					ArrayList<Integer> FieldsWithData = new ArrayList<Integer>();
					ArrayList<Integer> FieldsWithMissingData = new ArrayList<Integer>();
					TotalDataWeights = 0;
					TotalMissingWeights = 0;
					
					for(int j = 0;j<sourceADataFull.get(i1)[1].length;j++){
						if(!sourceADataFull.get(i1)[1][j].equals("") && !sourceBDataFull.get(i2)[1][j].equals("")){
							FieldsWithData.add(j);
							TotalDataWeights += EMWeightsDouble[j]; 
						}else{
							FieldsWithMissingData.add(j);
							TotalMissingWeights += EMWeightsDouble[j];
						}
					}
					
					if(FieldsWithMissingData.size()>0){
						for (int j=0; j<FieldsWithMissingData.size();j++){
							ImputationRule myRule = new ImputationRule();
							myRule.FieldsWithData = FieldsWithData;
							myRule.FieldWithMissingData = FieldsWithMissingData.get(j);
							
							Boolean isExisting = false;
							for (int k=0;k<ImputationRuleSet.size();k++){
								if(compareImputationRules(myRule, ImputationRuleSet.get(k))){
									isExisting = true;
								}
							}
							
							if(isExisting==false){
								if(TotalMissingWeights<TotalDataWeights){
									myRule = createImputationRule(sourceADataFull, sourceBDataFull, FieldsWithData, FieldsWithMissingData.get(j));
								}else{
									myRule.ImputedValue = 0;
								}
								ImputationRuleSet.add(myRule);
								myRule.printContent();
							}
						}
					}
				}
			}
			
			System.out.println("Run-time: "+(System.currentTimeMillis()-t5));
			
			int ppp = 0;
			ppp++;*/
			
		}else if(intMissingDataValue == this.BACKUP_FULL){
			
			mappedPairs_select = (ArrayList<MappedPair>) mappedPairs.clone();
			for(int j=0;j<mappedPairs_backup.size(); j++){
					mappedPairs_select.add(mappedPairs_backup.get(j));
			}
				
		}else if(intMissingDataValue == this.BACKUP_OPTIMAL){

			ConfiguredSystem IRSystem = new ConfiguredSystem(A_EMDataSource, B_EMDataSource, null, null);
			CICGenerator myCICGenerator = new CICGenerator(IRSystem, mappedPairs, mappedPairs_backup, xmlr);
			BackupField[] SortedBest = myCICGenerator.generate();
			
			//Create optimal backup mapped pairs
			
			mappedPairs_select = (ArrayList<MappedPair>) mappedPairs.clone();
			if(SortedBest.length>0){
				for(int i=0; i<SortedBest[0].FieldName.split("[|]").length; i++){
					for(int j=0;j<mappedPairs_backup.size(); j++){
						if(mappedPairs_backup.get(j).getColA().equals(SortedBest[0].FieldName.split("[|]")[i])){
							mappedPairs_select.add(mappedPairs_backup.get(j));
							break;
						}
					}
				}
			}
			
			
			//Load the data
			//Create field list

			/*String strFieldListA = "";
			int MinQuasiSetSize = mappedPairs.size();
			
			for (int i=0; i<mappedPairs.size(); i++){
				strFieldListA += mappedPairs.get(i).getColA().trim() + ",";
			}
	
			for (int i=0; i<mappedPairs_backup.size(); i++){
				strFieldListA += mappedPairs_backup.get(i).getColA().trim() + ",";
			}
			
			strFieldListA = strFieldListA.substring(0, strFieldListA.length()-1);
			
			//Create field list
			String strFieldListB = "";
			for (int i=0; i<mappedPairs.size(); i++){
				strFieldListB += mappedPairs.get(i).getColB().trim() + ",";
			}
	
			for (int i=0; i<mappedPairs_backup.size(); i++){
				strFieldListB += mappedPairs_backup.get(i).getColB().trim() + ",";
			}
	
			strFieldListB = strFieldListB.substring(0, strFieldListB.length()-1);
			
			DatabaseConnection dbcA = xmlr.getDatabaseConnection(xmlr.SOURCEA);
			DatabaseConnection dbcB = xmlr.getDatabaseConnection(xmlr.SOURCEB);
			
			ResultSet sourceARows =  dbcA.getTableQuery("SELECT "+strFieldListA+" FROM "+xmlr.getTableName(xmlr.SOURCEA));
			ResultSet sourceBRows =  dbcB.getTableQuery("SELECT "+strFieldListB+" FROM "+xmlr.getTableName(xmlr.SOURCEB));
			
			//Summarize the best backup field for Source A
			String[][] dataRowA; int count = 0;
			HashedMap BackupValueA = new HashedMap();
			
			CombinationCreator combinationCreator = new CombinationCreator(); 
			
			int TotalMissingCountA = 0;
			while ((dataRowA = dbcA.getNextResultWithColName(sourceARows)) != null) {
				Boolean hasNull = false;
				int LocalMissingCountA = 0;
				for(int i=0; i<mappedPairs.size(); i++){
					//TODO: What is the real null value
					
					if(StringUtils.isNullOrEmpty(dataRowA[1][i])){
						hasNull = true;
						LocalMissingCountA++;
					}
				}	
				
				if(hasNull==true){
					TotalMissingCountA++;
					ArrayList<String> noneNullList = new ArrayList<String>();
					if(LocalMissingCountA<=mappedPairs_backup.size()){
						for(int j = 0; j<mappedPairs_backup.size();j++){
							if(!StringUtils.isNullOrEmpty(dataRowA[1][mappedPairs.size()+j])){
								noneNullList.add(dataRowA[0][mappedPairs.size()+j]);
							}
						}
						combinationCreator.generate(noneNullList, LocalMissingCountA);
						for(int j=0;j<combinationCreator.results.size(); j++){
							//Create the key
							String strKey= "";
							for(int t=0; t<combinationCreator.results.get(j).size();t++){
								strKey += combinationCreator.results.get(j).get(t)+"|";
							}
							
							strKey = strKey.substring(0, strKey.length()-1);
							
							if(BackupValueA.containsKey(strKey)){
								int intCurrentValue = Integer.parseInt(BackupValueA.get(strKey).toString())+1;
								BackupValueA.remove(strKey);
								BackupValueA.put(strKey, intCurrentValue);
							}else{
								BackupValueA.put(strKey, 1);
							}
						}
					}
				}
			}
			
			
			//Sort the result from A
			HashedMap combinedBackupFields = (HashedMap) BackupValueA.clone();
			BackupField[] sortedFieldsA = new BackupField[BackupValueA.size()];
			int SortedQtyA = 0;
			while (!BackupValueA.isEmpty()){
				int max = 0;
				String maxKey = "";
				for(int i=0; i<BackupValueA.keySet().toArray().length;i++){
					if(Integer.parseInt(BackupValueA.get(BackupValueA.keySet().toArray()[i].toString()).toString())>max){
						max = Integer.parseInt(BackupValueA.get(BackupValueA.keySet().toArray()[i].toString()).toString());
						maxKey = BackupValueA.keySet().toArray()[i].toString();
					}
				}
				sortedFieldsA[SortedQtyA] = new BackupField();
				sortedFieldsA[SortedQtyA].FieldName = maxKey;
				sortedFieldsA[SortedQtyA].Value = max;
				SortedQtyA++;
				BackupValueA.remove(maxKey);
			}
			
			System.out.println("Sorted result from A:");
			for(BackupField bkf: sortedFieldsA){
				System.out.println("Key: "+bkf.FieldName+", value: "+bkf.Value);
			}
			
			//Summarize the best backup field for Source B
			String[][] dataRowB; count = 0;
			HashedMap BackupValueB = new HashedMap();
			
			int TotalMissingCountB = 0;
			while ((dataRowB = dbcB.getNextResultWithColName(sourceBRows)) != null) {
				Boolean hasNull = false;
				int LocalMissingCountB = 0;
				for(int i=0; i<mappedPairs.size(); i++){
					//TODO: What is the real null value
					
					if(StringUtils.isNullOrEmpty(dataRowB[1][i])){
						hasNull = true;
						LocalMissingCountB++;
					}
				}	
				
				if(hasNull==true){
					TotalMissingCountB++;
					ArrayList<String> noneNullList = new ArrayList<String>();
					if(LocalMissingCountB<=mappedPairs_backup.size()){
						for(int j = 0; j<mappedPairs_backup.size();j++){
							if(!StringUtils.isNullOrEmpty(dataRowB[1][mappedPairs.size()+j])){
								noneNullList.add(dataRowB[0][mappedPairs.size()+j]);
							}
						}
						combinationCreator.generate(noneNullList, LocalMissingCountB);
						for(int j=0;j<combinationCreator.results.size(); j++){
							//Create the key
							String strKey= "";
							for(int t=0; t<combinationCreator.results.get(j).size();t++){
								strKey += combinationCreator.results.get(j).get(t)+"|";
							}
							
							strKey = strKey.substring(0, strKey.length()-1);
							
							if(BackupValueB.containsKey(strKey)){
								int intCurrentValue = Integer.parseInt(BackupValueB.get(strKey).toString())+1;
								BackupValueB.remove(strKey);
								BackupValueB.put(strKey, intCurrentValue);
							}else{
								BackupValueB.put(strKey, 1);
							}
						}
					}
				}
			}
			
			//Sort the result from B
			BackupField[] sortedFieldsB = new BackupField[BackupValueB.size()];
			int SortedQtyB = 0;
			while (!BackupValueB.isEmpty()){
				int max = 0;
				String maxKey = "";
				for(int i=0; i<BackupValueB.keySet().toArray().length;i++){
					if(Integer.parseInt(BackupValueB.get(BackupValueB.keySet().toArray()[i].toString()).toString())>max){
						max = Integer.parseInt(BackupValueB.get(BackupValueB.keySet().toArray()[i].toString()).toString());
						maxKey = BackupValueB.keySet().toArray()[i].toString();
					}
				}
				sortedFieldsB[SortedQtyB] = new BackupField();
				sortedFieldsB[SortedQtyB].FieldName = maxKey;
				sortedFieldsB[SortedQtyB].Value = max;
				SortedQtyB++;
				BackupValueB.remove(maxKey);
			}
	
			System.out.println("Sorted result from B:");
			for(BackupField bkf: sortedFieldsB){
				System.out.println("Key: "+bkf.FieldName+", value: "+bkf.Value);
			} 
			
			//Translate B to A
			BackupField[] translatedFieldsB = new BackupField[sortedFieldsB.length];
			for(int i=0; i<sortedFieldsB.length; i++){
				String translatedKey = "";
				for(int t=0; t<sortedFieldsB[i].FieldName.split("[|]").length; t++){
					for (int j=0; j<mappedPairs_backup.size(); j++){
						if(mappedPairs_backup.get(j).getColB().equals(sortedFieldsB[i].FieldName.split("[|]")[t])){
							translatedKey += mappedPairs_backup.get(j).getColA()+"|";
							break;
						}
					}
				}
				translatedKey = translatedKey.substring(0, translatedKey.length()-1);
				
				translatedFieldsB[i] = new BackupField();
				translatedFieldsB[i].FieldName = translatedKey;
				translatedFieldsB[i].Value = sortedFieldsB[i].Value;
			}
			
			//Combine the two sources
			
			for(int i = 0; i<translatedFieldsB.length; i++){
				if(combinedBackupFields.containsKey(translatedFieldsB[i].FieldName)){
					int intCurrentValue = Integer.parseInt(combinedBackupFields.get(translatedFieldsB[i].FieldName).toString()) + translatedFieldsB[i].Value;
					combinedBackupFields.remove(translatedFieldsB[i].FieldName);
					combinedBackupFields.put(translatedFieldsB[i].FieldName, intCurrentValue);
				}else{
					combinedBackupFields.put(translatedFieldsB[i].FieldName, translatedFieldsB[i].Value);
				}
			}
			
			//Sort the combined result
			BackupField[] combinedFields = new BackupField[combinedBackupFields.size()];
			int combinedQty = 0;
			while (!combinedBackupFields.isEmpty()){
				int max = 0;
				String maxKey = "";
				for(int i=0; i<combinedBackupFields.keySet().toArray().length;i++){
					if(Integer.parseInt(combinedBackupFields.get(combinedBackupFields.keySet().toArray()[i].toString()).toString())>max){
						max = Integer.parseInt(combinedBackupFields.get(combinedBackupFields.keySet().toArray()[i].toString()).toString());
						maxKey = combinedBackupFields.keySet().toArray()[i].toString();
					}
				}
				combinedFields[combinedQty] = new BackupField();
				combinedFields[combinedQty].FieldName = maxKey;
				combinedFields[combinedQty].Value = max;
				combinedQty++;
				combinedBackupFields.remove(maxKey);
			}
			
			System.out.println("Combined result");
			for(BackupField bkf: combinedFields){
				System.out.println("Key: "+bkf.FieldName+", value: "+bkf.Value);
			}
			
			//Create optimal backup mapped pairs
			
			mappedPairs_select = (ArrayList<MappedPair>) mappedPairs.clone();
			if(combinedFields.length>0){
				for(int i=0; i<combinedFields[0].FieldName.split("[|]").length; i++){
					for(int j=0;j<mappedPairs_backup.size(); j++){
						if(mappedPairs_backup.get(j).getColA().equals(combinedFields[0].FieldName.split("[|]")[i])){
							mappedPairs_select.add(mappedPairs_backup.get(j));
							break;
						}
					}
				}
			}*/
		}
		
	}
	
	public void finalLinkageSetup(){
		
		try{
			//Set Imputation rule set
			
			condition.setImputationRuleSet(ImputationRuleSet);
			
			//Final Join
			Map joinProperties = new HashedMap();
			
			if(joinMethod.equals(JoinMethod.NESTED_LOOP_JOIN)){
				join = new NestedLoopJoin(A_DataSource, B_DataSource, outFormat, condition, new HashedMap());				
			}else if(joinMethod.equals(JoinMethod.BLOCKING_SEARCH)){
	
				joinProperties.put(BlockingJoin.BLOCKING_PARAM, String.valueOf(intBlockPairIndex));
				joinProperties.put(BlockingJoin.BLOCKING_FUNCTION, "equality");
				joinProperties.put(BlockingJoin.MAX_CPU, strMaxCPU);
	
					join = new BlockingJoin(A_DataSource, B_DataSource, outFormat, condition, joinProperties);
			}else if(joinMethod.equals(JoinMethod.SORTED_NEIGHBOURHOOD)){
				
				strSNMOrder = xmlr.getSNMKeys();
				
				if(strSNMOrder!=null){
					//Rebuild the sorting order from the SNM keys
					String strSourceAOrder = "";
					String strSourceBOrder = "";
					for(int i=0; i<strSNMOrder.length; i++){
						MappedPair currentMappedPair = mappedPairs.get(getKeyIndex(strSNMOrder[i].trim()));
						strSourceAOrder += currentMappedPair.getColA()+",";
						strSourceBOrder += currentMappedPair.getColB()+",";
					}
					
					strSourceAOrder = strSourceAOrder.substring(0,strSourceAOrder.length()-1);
					strSourceBOrder = strSourceBOrder.substring(0,strSourceBOrder.length()-1);
					
					intSNMWindowSize = xmlr.getSNMWindowsSize();
					
					joinProperties.put(SNMJoin_v1.PARAM_WINDOW_SIZE, String.valueOf(intSNMWindowSize));
					joinProperties.put(SNMJoin_v1.PARAM_SORT_ORDER_A, strSourceAOrder);
					joinProperties.put(SNMJoin_v1.PARAM_SORT_ORDER_B, strSourceBOrder);
					
					join = new SNMJoin_v1(A_DataSource, B_DataSource, outFormat, condition, joinProperties);
				}
			}
			
			//Final system
			system = new ConfiguredSystem(A_DataSource, B_DataSource, join, saver);
			
		}catch(RJException myException){
			myException.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void start(){
		
		rosita.linkage.tools.LinkageProcessStarter processStarter = new rosita.linkage.tools.LinkageProcessStarter();
		processStarter.startProcessAndWait(system);
	}
	
	public AbstractDistance generateDistance(Algorithm parAlgorithm, MappedPair parPair){
		AbstractDistance result = null;
		
		Map props = new HashedMap();
		
		
		if (parAlgorithm.equals(Algorithm.EQUAL_FIELDS_BOOLEAN_DISTANCE)){
			result = new EqualFieldsDistance(props);
		}
		else if(parAlgorithm.equals(Algorithm.EDIT_DISTANCE)){ 
			props.put(EditDistance.PROP_BEGIN_APPROVE_LEVEL, "0.2");
			props.put(EditDistance.PROP_END_APPROVE_LEVEL, "0.4");
			result = new EditDistance(props);
		}
		else if(parAlgorithm.equals(Algorithm.JARO_WINKLER)){ 
			props.put("pref-length", "4");
			props.put("pref-weight", "0.1");
			result = new JaroWinkler(props);
		}
		else if(parAlgorithm.equals(Algorithm.DATE_DISTANCE)){
			if(parPair.getDateFormatA()!=null && parPair.getDateFormatB()!=null){
				props.put(DateDistance.PROP_FORMAT1 , parPair.getDateFormatA());
				props.put(DateDistance.PROP_FORMAT2, parPair.getDateFormatB());
				result = new DateDistance(props);
			}else{
				//TODO: Throw exception
			}	
		}
		else if(parAlgorithm.equals(Algorithm.ADDRESS_DISTANCE)){
			props.put(AddressDistance.PROP_BEGIN_APPROVE_LEVEL, "0.0");
			props.put(AddressDistance.DEFAULT_END_APPROVE_LEVEL, "0.3");
			props.put("resolve-secondary-location", "true");
			result = new AddressDistance(props);
		}
		else if(parAlgorithm.equals(Algorithm.QGRAM_DISTANCE)){
			props.put("q", "3");
			props.put(QGramDistance.PROP_DISAPPROVE, "0.4");
			props.put(QGramDistance.PROP_APPROVE, "0.2");
			result = new QGramDistance(props);
		}
		else if(parAlgorithm.equals(Algorithm.SOUNDEX_DISTANCE)){
			props.put("soundex-length", "5");
			props.put("match-level-start", "0");
			props.put("math-level-end", "0");
			result = new SoundexDistance(props);
		}else if(parAlgorithm.equals(Algorithm.PPRL)){
			result = new PPRLDistance(dblPPRLThreshold);
		}else if(parAlgorithm.equals(Algorithm.NONE)){
			result = new NONEDistance();
		}
		return result;
	}
}
