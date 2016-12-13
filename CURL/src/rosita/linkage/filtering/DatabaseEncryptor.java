package rosita.linkage.filtering;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import rosita.linkage.RecordBlock;
import rosita.linkage.analysis.DBMS;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.FileIO;
import rosita.linkage.util.MappingConfig;
import rosita.linkage.util.MyUtilities;
import rosita.linkage.util.StopWatch;


public class DatabaseEncryptor 
{
	// These variables are really only for testing purposes
	private int MAX_COUNT = 2000;
	private int MAX_READ = -1;
	private boolean VERBOSE = false;
	private boolean DO_WRITE = true;
	private boolean ONE_BLOCK = false;
	private boolean LOG_TIMES = false;
	private int numFilters = 4;

	// Timing Utilities
	private StopWatch timer = new StopWatch();
	private StopWatch blockTimer = null;
	private String read_times = null;
	private String process_times = null;
	private String block_times = null;

	// These members below are used for the encrypting procedure
	private DatabaseConnection readDBC = null;
	private DatabaseConnection writeDBC = null;

	private String readTable;
	private String writeTable;
	
	private DBMS myDBMS;

	private List<E_Record> e_records = null;
	private int readRecordCount;
	
	private MappingConfig mapConfig = null;
	
	private boolean setupComplete = false;
	private boolean readingComplete = false;


	/**
	 * Default Class Constructor. 
	 * @param readDBC - A DatabaseConnection to the database from which we will read
	 * @param writeDBC - A Data100baseConnection to the database to which we will write the encrypted values
	 * @param mapConfig - A properly initialized mapping configuration object.
	 */
	public DatabaseEncryptor(DatabaseConnection readDBC, DatabaseConnection writeDBC,
			MappingConfig mapConfig, DBMS parDBMS)
	{
		this.readDBC = readDBC;
		this.writeDBC = writeDBC;
		this.myDBMS = parDBMS;
		// Grab as much as we can from mapConfig right now
		this.readTable = mapConfig.getTable(MappingConfig.SOURCE_A);
		this.writeTable = mapConfig.getTable(MappingConfig.SOURCE_B);
		
		this.mapConfig = mapConfig;
	}
	
	/**
	 * Set up objects needed for the reading & writing procedure.
	 */
	private void setup()
	{
		if (VERBOSE) {
			System.out.println("Database Reader & Writer Setup...");
			timer.start();
		}
		
		if(myDBMS.equals(DBMS.MySQL)){

			// Ensure table existence
			if (! readDBC.checkTableExists(readTable) ) 
				MyUtilities.end("Error: table " + readTable + " does not exist!\n" +
				"Program Terminated.");			
			
			if (! writeDBC.checkTableExists(writeTable) ) 
				MyUtilities.end("Error: table " + writeTable + " does not exist!\n" +
				"Program Terminated.");			
			
			// Get the number of rows in the the readTable
			readRecordCount = readDBC.getRowCount(readTable);		
		}else if(myDBMS.equals(DBMS.PostgreSQL) || myDBMS.equals(DBMS.SQLServer)){
			// Ensure table existence
			if (! readDBC.checkPostgreTableExists(readTable, readDBC.getSchema()) ) 
				MyUtilities.end("Error: table " + readTable + " does not exist!\n" +
				"Program Terminated.");			
			
			if (! writeDBC.checkPostgreTableExists(writeTable, writeDBC.getSchema()) ) 
				MyUtilities.end("Error: table " + writeTable + " does not exist!\n" +
				"Program Terminated.");		
			
			// Get the number of rows in the the readTable
			readRecordCount = readDBC.getRowCount("["+readDBC.getDatabase()+"]"+".["+readDBC.getSchema()+"].["+readTable+"]");	
		}
			
		
		// Attach the mapConfig object to Record.
		E_Record.setup(mapConfig, numFilters);
		e_records = new ArrayList<E_Record>(readRecordCount);
		
		if (VERBOSE) {
			timer.stop();
			System.out.format("Database reader & Writer Setup completed succesfully in %f seconds%n",
					timer.getElapsedTimeSecsDouble());
		}

		setupComplete = true;
	}

	/**
	 * Once things are setup. perform the read from the readDBC
	 */
	private void read()
	{
		if (!setupComplete) return;

		if (VERBOSE || LOG_TIMES) {
			timer.start();
		}
		if (VERBOSE) {
			System.out.println("Started reading in records from " + readTable + "..." );
		}

		// TODO: Doing a select * will eat up memory. (Do it in blocks?)
		ResultSet sqlResults = readDBC.getTableQuery("SELECT * FROM " + ((myDBMS.equals(DBMS.PostgreSQL)||myDBMS.equals(DBMS.SQLServer))?readDBC.getSchema()+".":"")+readTable);

		// Try to read in Record data from database
		String[] dataRow; String[] encryptionTypes; int count = 0;
		
		encryptionTypes = new String[mapConfig.getMappedPairs().size()];
		for(int i=0;i<mapConfig.getMappedPairs().size();i++){
			encryptionTypes[i] = mapConfig.getMappedPairs().get(i).encryptionType; 
		}
		//encryptTypes = readDBC.getEncryptionTypes
		
		while ((dataRow = readDBC.getNextResult(sqlResults)) != null && count != MAX_READ) {
			e_records.add(new E_Record(dataRow, encryptionTypes));
		}
				
		// Release ResultSet & close connection to database
		sqlResults = null;
		readDBC.close();


		if (VERBOSE || LOG_TIMES) {
			timer.stop();
		}

		if (VERBOSE) {
			System.out.format("Sucessfully read in  %d records from table %s in %f seconds.%n", 
					e_records.size(), readTable, timer.getElapsedTimeSecsDouble());
		}

		if (LOG_TIMES) {
			String s = "" + readRecordCount + "\t" + timer.getElapsedTimeSecsDouble();
			FileIO.AppendToFile(s, read_times);
		}

		readingComplete = true;
	}

	/**
	 * Once setup and reading is complete, clean, encrypt and write the records.
	 */
	private void cleanEncryptWrite()
	{
		if (!readingComplete || !setupComplete) return;

		if (VERBOSE) {
			timer.start();
			System.out.println("Encrypting, Cleaning, & Writing Records...");
		}

		// Calculate how many blocks we need to process
		int numBlocks = (readRecordCount / MAX_COUNT) + 1;
		// There's too many blocks if recordCount is a multiple of MAX_COUNT
		if (readRecordCount % MAX_COUNT == 0) numBlocks--; 

		RecordBlock block = new RecordBlock(MAX_COUNT);
		int numRecords;
		blockTimer = new StopWatch();
				
		for (int i = 0; i < numBlocks; i++)
		{	
			blockTimer.start();

			// Ensure that we don't go past the number of records
			if (i == numBlocks && readRecordCount % MAX_COUNT != 0)
				numRecords = readRecordCount % MAX_COUNT;
			else numRecords = MAX_COUNT;

			block.clear();
			
			for (int j = 0; j < numRecords; j++) {
				//int index = readRecordCount - 1 - (block.getTotalProcessed() + j);
				//int index = i * numRecords + j;
				//System.out.println("index:" + index);
				if (e_records.size() != 0)
					block.loadRecord(e_records.remove(0));
			}
			
			block.processRecords(true, true);
			
			// Prepare a query with this block's records.
			StringBuilder query = new StringBuilder();
			
			String tempTableName = "";
			if (myDBMS.equals(DBMS.PostgreSQL)){
				tempTableName = writeDBC.getSchema()+"."+writeTable;
			}else if (myDBMS.equals(DBMS.SQLServer)){
				tempTableName = "["+writeDBC.getDatabase()+"].["+writeDBC.getSchema()+"].["+writeTable+"]";
			}else{
				tempTableName = writeTable;
			}
			
			query.append("INSERT INTO " + tempTableName + " (");
			query.append(mapConfig.getColumnNamesCSV(MappingConfig.SOURCE_B) );
			query.append("," + mapConfig.getBlockingColumnName(MappingConfig.SOURCE_B) + ")\n");
			query.append("VALUES\n");
			if(myDBMS.equals(DBMS.MySQL)){
				query.append(block.getRecordsAsMySQLTuples());
			}else if (myDBMS.equals(DBMS.PostgreSQL)||myDBMS.equals(DBMS.SQLServer)){
				query.append(block.getRecordsAsPostgresTuples());
			}
					
			if (DO_WRITE)
				writeDBC.executeQuery(query.toString());
			else
				System.out.println(query.toString());

			if (ONE_BLOCK)
				break;

			blockTimer.stop();

			if (VERBOSE)
				System.out.format("Succesfully processed block: %d/%d in %f seconds %n", 
						i, numBlocks, blockTimer.getElapsedTimeSecsDouble());
			if (LOG_TIMES)
				FileIO.AppendToFile("" + numRecords + "\t" + blockTimer.getElapsedTimeSecsDouble(), block_times);
		}

		timer.stop();
		
		if (VERBOSE) 
			System.out.format("Succesfully cleaned, encrtypted and wrote %d records to table %s in %f seconds.%n",
					block.getTotalProcessed(), writeTable, timer.getElapsedTimeSecsDouble());
		
		if (LOG_TIMES)
			FileIO.AppendToFile("" + block.getTotalProcessed() + "\t" + timer.getElapsedTimeSecsDouble(), process_times);


	}

	/**
	 * Calls setup(), read(), and cleanEncryptWrite().
	 * Uses one database as input, encrypts its rows and writes to another database
	 * Order, at this time is unfortunately not preserved. 
	 */
	public void encryptDB()
	{
		this.setup();

		this.read();

		this.cleanEncryptWrite();

		if (VERBOSE) 
			System.out.println("Encrpytion procedure complete.");
	}

	// ******************************************************************************
	// ** Getters for DatabaseEncryptor members
	// ******************************************************************************

	public boolean getDoWrite()
	{
		return DO_WRITE;
	}

	public boolean getLogTimes()
	{
		return LOG_TIMES;
	}

	public int getMaxCount()
	{
		return MAX_COUNT;
	}

	public int getMaxRead()
	{
		return MAX_READ;
	}
	
	public boolean getOneBlock()
	{
		return ONE_BLOCK;
	}
	
	public boolean getVerbose()
	{
		return VERBOSE;
	}
	
	// ******************************************************************************
	// ** Getters for DatabaseEncryptor members
	// ******************************************************************************
	/**
	 * Tell the DatabaseEncryptor to write "encryption", "read", & "block" times to files
	 * @param read_times - The path to where the the read times should be written.
	 * @param encrpytion_times - The path to where the encryption times should be written.
	 * @param block_times - The path to where the block processing times should be written.
	 */
	public void setLogTimes( String read_times, String process_times, String block_times)
	{
		LOG_TIMES = true;
		this.process_times = process_times;
		this.read_times = read_times;
		this.block_times = block_times;	
	}
	
	public void setDoWrite(boolean b)
	{
		DO_WRITE = b;
	}

	public void setMaxCount(int n)
	{
		MAX_COUNT = n;
	}
	
	public void SetMaxRead(int n)
	{
		MAX_READ = n;
	}
	
	public void setOneBlock(boolean b)
	{
		ONE_BLOCK = b;
	}

	public void setVerbose(boolean b)
	{
		VERBOSE = b;
	}
}
