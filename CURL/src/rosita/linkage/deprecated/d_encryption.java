package rosita.linkage.deprecated;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import rosita.linkage.main;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.FileIO;
import rosita.linkage.util.MyUtilities;

public class d_encryption 
{
	// Constants used as program configurations
	// TODO: These really could be specified by the user or before d_encryption is called.
	private static final String encryption_config = "cfg/encryption.properties";
	private static final String encryption_times = "log/encryptionTimes/encryptionTimes.txt";
	private static final String block_times	= "log/blockTimes/blockTimes.txt";

	// The maximum block size (usually set at 5,000)
	// TODO: Testing should be done to find an optimum block size
	private static final int MAX_COUNT = 5000;

	// Flag used to toggle actually writing the records to the database
	// This is only really used for testing purposes, 
	// set to 'true' if records aren't being written to MySQL ;)
	private static final boolean do_write = false;

	/** 
	 * Suppress default constructor for non-instantiability
	 */
	private d_encryption() 
	{
		throw new AssertionError();
	}

	/**
	 * This function is one of two routes of execution for the PPRL process.
	 * This encryption function takes uses the PERSON table from the ETL
	 *  database, reads in data, cleans it, and then writes it back to ETL.
	 *  The connection information can all be specified by the values in 
	 *  cfg/encryption.properties
	 */
	public static void encryption()
	{
		// -------------------------------------------------------------------------------------- 
		// -- 1. Database Read Setup
		// --------------------------------------------------------------------------------------

		// Message: Reader setup.
		System.out.println("Database Reader Setup...");

		// Read in properties for encryption
		Properties p = MyUtilities.readProperties(encryption_config);
		String r_url = p.getProperty("R_URL");
		String r_database = p.getProperty("R_DATABASE");
		String r_table = p.getProperty("R_TABLE");
		String r_user = p.getProperty("R_USER");
		String r_password = p.getProperty("R_PASSWORD");

		// Database Declarations/Initializations
		DatabaseConnection readDBC = new DatabaseConnection(r_url, 
				main.mysqlDriver, r_database, r_user, r_password);

		// Ensure database table exists before reading
		if (! readDBC.checkTableExists(r_table)) {
			System.err.println("Error: table " + r_table + " does not exist!");
			System.err.println("Program Terminated.");
			System.exit(1);
		}

		// Get the total number of records.
		ResultSet sqlResults = readDBC.getTableQuery("SELECT COUNT(*) AS COUNT FROM " + r_table + " ;");
		String[] dataRow = readDBC.getNextResult(sqlResults);
		final int recordCount = new Integer(dataRow[0]);

		// Message: Reader setup success.
		System.out.println("Database Reader Setup Successful!");


		// --------------------------------------------------------------------------------------
		// -- 2. Database Write Setup
		// --------------------------------------------------------------------------------------

		// Message: Writer setup.
		System.out.println("Database Writer Setup...");

		// Read in properties for encryption
		p = MyUtilities.readProperties(encryption_config);
		String w_url = p.getProperty("W_URL");
		String w_database = p.getProperty("W_DATABASE");
		String w_table = p.getProperty("W_TABLE");
		String w_user = p.getProperty("W_USER");
		String w_password = p.getProperty("W_PASSWORD");

		// Database Declarations/Initializations
		DatabaseConnection writeDBC = new DatabaseConnection(w_url, 
				main.mysqlDriver, w_database, w_user, w_password);

		// Ensure database table exists before writing
		if (! writeDBC.checkTableExists(w_table)) {
			System.err.println("Error: table " + w_table + " does not exist!");
			System.err.println("Program Terminated.");
			System.exit(1);
		}

		// Read in one line to get columnNames
		ResultSet wSQLResults = writeDBC.getTableQuery("SELECT * FROM "+ w_table + " LIMIT 1");
		String[] writeColumnNames = readDBC.getColumnNames(wSQLResults);
		
		// Let Record Know what order columnNames should be written in
		Record.setWriteOrder(writeColumnNames);

		// Convert ColumnNames to a CSV, prepping for the MySQL insert query.
		StringBuilder writeColumnNamesCSV = new StringBuilder();
		for (int i = 1; i < writeColumnNames.length; i++) {
			writeColumnNamesCSV.append(writeColumnNames[i]);
			if (i != writeColumnNames.length - 1)
				writeColumnNamesCSV.append(",");
		}

		// Message: Writer setup success
		System.out.println("Database Writer Setup Successful!");


		// --------------------------------------------------------------------------------------
		// -- 3. Database READING
		// --------------------------------------------------------------------------------------

		// Message: Reading procedure starting
		System.out.println("Started reading in records from " + r_table + "..." );

		// Initialize array list of records, ensuring capacity.
		List<Record> records = new ArrayList<Record>(recordCount);		

		// Start timer for DB read
		Date d1 = new Date();

		// Read in the entire table into a ResultSet
		// TODO: Doing a select * will eat up memory. (Do it in blocks?)
		sqlResults = readDBC.getTableQuery("SELECT * FROM " + r_table);
		ResultSetMetaData sqlRSMD = readDBC.getTableMetaData(sqlResults);
		String[] columnNames = readDBC.getColumnNames(sqlRSMD);

		// Set Record columnNameMap. Generate map from MyUtilities.returnMap()
		Record.setColumnNameMap(MyUtilities.returnMap(columnNames));

		// Try to read in Record data from database
		while ((dataRow = readDBC.getNextResult(sqlResults)) != null )
			records.add(new Record(dataRow, false, false));

		// Release ResultSet & close connection to database
		sqlResults = null;
		readDBC.close();

		// Stop timer for DB read
		Date d2 = new Date();
		double readTime = MyUtilities.subtractTimeDouble(d1, d2);

		// Message: Reading procedure success. 
		System.out.println("Sucessfully read in " + records.size() + " records from table: " + r_table);
		// Message: Reading procedure time.
		System.out.println("Read Time: " + readTime);

		
		// --------------------------------------------------------------------------------------
		// -- Encrypt, Clean & Write
		// -- Only process MAX_COUNT records at a time
		// --------------------------------------------------------------------------------------

		// Message: Encrypt, Clean, & Write procedure started.
		System.out.println("Encrypting, Cleaning, & Writing Records...");

		// Initialize timers.
		double writeTime = 0.0;
		double cleanEncryptTime = 0.0;

		// Declare / Initialize items to manage encryption and writing with blocks.
		List<Record> recordBlock = new ArrayList<Record>(MAX_COUNT);
		Record r = null;
		int numRecords;
		int totalProcessed = 0;
		int index;

		// Determine the number of blocks
		// There's too many blocks if recordCount is a multiple of MAX_COUNT
		int numBlocks = (recordCount / MAX_COUNT) + 1;
		if (recordCount % MAX_COUNT == 0) numBlocks--; 
		
		// Iterate across all blocks forwards
		for (int i = 1; i <= numBlocks; i++)
		{
			// Start block timer
			Date b1 = new Date();

			// Ensure that we don't go past the number of records
			if (i == numBlocks && recordCount % MAX_COUNT != 0)
				numRecords = recordCount % MAX_COUNT;
			else numRecords = MAX_COUNT;

			// Initialize record block
			recordBlock.clear();
			for (int j = 0; j < numRecords; j++) {
				index = recordCount - 1 - (totalProcessed + j);
				//System.out.println("index:" + index);
				recordBlock.add(records.remove(index));
			}
			totalProcessed += numRecords;

			// Start Clean & Encrypt Timer
			d1 = new Date();

			// Clean & Encrypt the records.public String getValue(String key)
			// Since recordBlock contains references the records,
			// We can pass the reference to recordBlock by value
			//ThreadHelper.processRecords(recordBlock, true, true);

			// Stop Clean & Encrypt Timer
			d2 = new Date();
			cleanEncryptTime += MyUtilities.subtractTimeDouble(d1, d2);

			// Start timer for database write
			d1 = new Date();

			// Prepare a query with this block's records.
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO " + w_table + " (");
			query.append(writeColumnNamesCSV.toString() + ")\n");
			query.append("VALUES\n");

			// Continue query prep with all of the values from this record block.
			while (recordBlock.size() != 0)
			{
				// Remove values backwards to maintain order, and conserve memory
				r = recordBlock.remove(recordBlock.size() -1);

				query.append( "(" + r.toQuotedString() + ")" );

				if (recordBlock.size() == 0)
					query.append("\n");
				else 
					query.append(",\n");
			}

			// Write the records.
			if (do_write)
				writeDBC.executeQuery(query.toString());
			else
				System.out.println(query.toString());
			
			// Stop write timer		
			d2 = new Date();
			writeTime += MyUtilities.subtractTimeDouble(d1, d2);

			// Stop block timer
			Date b2 = new Date();
			double blockTime = MyUtilities.subtractTimeDouble(b1, b2);

			// Write blockTime to file
			String s = "" + i + "\t" + blockTime + "\n";
			FileIO.AppendToFile(s, block_times);

			// Message: Print out the time to process this block.
			System.out.println("Succesfully processed block: " + i + "/" + numBlocks + " in " + blockTime + " seconds.");

		}

		// --------------------------------------------------------------------------------------
		// -- Logging
		// --------------------------------------------------------------------------------------

		// Print performance times
		System.out.println("Cleaning & Encryption Time: " + cleanEncryptTime );
		System.out.println("Writing Time: " + writeTime);	
		System.out.println("done.");

		// Write performance to file.
		String s = "";
		s += recordCount + "\t";
		s += MyUtilities.toDecimals(readTime, 2) + "\t";
		s += MyUtilities.toDecimals(cleanEncryptTime, 2) + "\t";
		s += MyUtilities.toDecimals(writeTime, 2) + "\t\n";
		FileIO.AppendToFile(s, encryption_times);
	
	}

}
