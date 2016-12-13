import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;


/**
 * This just contains the original encrypt() method that was used for the patients
 * @author Brandon Abbott
 *
 */

public class driver
{
	/**
	 * Uses Bloom Filters for encryption and outputs to 
	 * STDOUT, a file, or a database.
	 * -- A different, newer version of the one below
	 */
	private static void encrypt()
	{		
		// ----------------------------------------------------
		// -- Clean, Encrypt and Write
		// ----------------------------------------------------
		String output_name = p.getProperty("OUTPUT_NAME");
		// Start timer for encryption & write
		d1 = new Date();
		Record r;
		int n = records.size();
		int numBlocks = (n / MAX_COUNT) + 1;
		int numRecords;
		
		// Set up timers
		double cleanTime = 0.0;
		double encryptTime = 0.0;
		double writeTime = 0.0;
		double removeTime = 0.0;
		
		try
		{
			if (! dbc.checkTableExists(database, output_name))
				// 2. Create table if necessary
				dbc.createTable(output_name);

			// Iterate through each block of records
			for (int i = 0; i < numBlocks; i++)
			{
				// Don't go past the total number of records
				if ((i == 1 && numBlocks == 1) || i == numBlocks-1) 
					numRecords = n % MAX_COUNT;
				else numRecords = MAX_COUNT;
				
				// ----------- CLEANING  ----------- 
				d1 = new Date();
				for (int j = 0; j < numRecords; j++ ){
					//r = records.get(j);
					//r.cleanValues();
				}
				d2 = new Date();
				cleanTime += MyUtilities.subtractTimeDouble(d1, d2);
				System.out.println("CleanTime: " + MyUtilities.subtractTime(d1, d2) );
				
				// ----------- ENCRYPTION  ----------- 
				d1 = new Date();
				for (int j = 0; j < numRecords; j++ ){
					r = records.get(j);
					r.encryptValues();
				}
				d2 = new Date();
				encryptTime += MyUtilities.subtractTimeDouble(d1, d2);
				System.out.println("EncryptTime: " + MyUtilities.subtractTime(d1, d2) );
				
				// ----------- WRITE ----------- 
				d1 = new Date();
				// Prepare a query with this block's records
				StringBuilder query = new StringBuilder();
				for (int j = 0; j < numRecords; j++ ){
					query.append(records.remove(j).toSQLInsertionStatement(output_name));
				}
				MyUtilities.WriteFile(query.toString(), "C:\\cygwin\\home\\123476\\workspace\\client_project\\mysqltemp.sql");
				d2 = new Date();
				writeTime += MyUtilities.subtractTimeDouble(d1, d2);
				System.out.println("PrepareTime: " + MyUtilities.subtractTime(d1, d2) );
				
				d1 = new Date();
				// Execute the write command
				dbc.executeQuery("SOURCE C:\\cygwin\\home\\123476\\workspace\\client_project\\mysqltemp.sql");
				d2 = new Date();
				writeTime += MyUtilities.subtractTimeDouble(d1, d2);
				System.out.println("WriteTime: " + MyUtilities.subtractTime(d1, d2) );
				
			}
			// --------- Print Results / Block ------------
			System.out.println("TOTAL CleanTime: " + cleanTime );
			System.out.println("TOTAL EncryptTime: " + encryptTime);
			System.out.println("TOTAL WriteTime: " + writeTime);
			System.out.println("TOTAL ClearTime: " + removeTime);
			System.out.println("TotalTime: " + (cleanTime+encryptTime+writeTime+removeTime));
			
			System.out.println("Done");
			System.exit(0);
			
			
			
			while (records.size() != 0)
			{
				// get the last record
				r = records.remove(0);
				// clean it
				r.cleanValues();
				// encrypt it
				r.encryptValues();
				// try to write it
				if ( ! dbc.insertIntoTable(output_name, r))
					throw new Exception();
				// print progress
				
				
			}
		} catch (Exception e) {
			System.err.println("Could not write to DB");
			e.printStackTrace();
		}


		// Stop timer for encryption & write & print
		d2 = new Date();
		System.out.println("Encrypt & Write: " + MyUtilities.subtractTime(d1, d2));
		
		System.out.println("Done");
		System.exit(0);


		
		
		// ----------------------------------------------------
		// -- Record Encryption
		// ----------------------------------------------------
		// Start timer for encryption
		d1 = new Date();

		// Try to encrypt fields of patient
		for (int i = 0; i < records.size(); i++)
		{
			// Encrypt patient
			records.get(i).encryptValues();

			// Show progress of encryption
			MyUtilities.PrintPercentProgress(i, records.size(), 2, '=');
		}

		// Stop timer for encryption & print
		d2 = new Date();
		System.out.println("Encrypt: " + MyUtilities.subtractTime(d1, d2));

		// ----------------------------------------------------
		// -- Record Writing
		// ----------------------------------------------------

		// Start timer for DB write
		d1 = new Date();

		// Determine how to write encrypted Data
		String writer = p.getProperty("WRITER");
		if (writer.equals("database"))
			//			System.err.println("records to db not yet set up.");
			MyUtilities.WriteRecordsToDatabase(records, dbc, database, output_name);
		else if (writer.equals("stdout"))
			MyUtilities.WriteRecordsToStdOut(records);
		else	
			MyUtilities.WriteRecordsToFile(records, output_name);

		// Stop DB write timer & print
		d2 = new Date();
		System.out.println("Write: " + MyUtilities.subtractTime(d1, d2));

		// Clean up and exit.
		dbc.close();
		System.out.println("Program finished successfully.");


	}

	//------------------------------------------------------------------------------
	private static void encrypt(){
		// Read in database connection credentials from file "properties_location"
		MyUtilities.ReadProperties(encrypt_config);

		// Database Declarations/Initializations
		DatabaseConnection dbc = new DatabaseConnection(MyUtilities.getUrl() + MyUtilities.getDatabase(), 
				MyUtilities.getDriver(), MyUtilities.getUser(), MyUtilities.getPassword());

		ResultSet sqlResults = dbc.getTable(MyUtilities.getTable());
		ResultSetMetaData sqlRSMD = dbc.getTableMetaData(sqlResults);
		int rowCount = dbc.getRowCount(sqlResults);
		int columnCount = dbc.getColumnCount(sqlResults);
		String[] columnNames = dbc.getColumnNames(sqlRSMD);

		// Set Patient columnNameMap
		Patient.setColumnNameMap(MyUtilities.returnMap(columnNames));

		// Initialize items to manage patient data
		ArrayList<Patient> pData = new ArrayList<Patient>();
		Patient tempPatient = null;
		//int limit = (int) Math.ceil(rowCount / 3500.0);
		String[] rowOfData;

		// Start timer for DB read
		Date d1 = new Date();

		// Try to read in patient data from database
		try {
			for (int j = 0; j < limit; j++){
				// create a temporary patient & init from DB
				rowOfData  = new String[columnCount];
				for(int z = 0; z < columnCount; z++)
					rowOfData[z] = sqlResults.getString(columnNames[z]);
				tempPatient = new Patient(rowOfData);

				// Add patient to pData list
				pData.add(tempPatient);
				// Move to next result in data set
				sqlResults.next();

				// Show progress of read
				//MyUtilities.PrintPercentProgress(j, limit, 2, '=');

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Stop timer for DB read & print
		Date d2 = new Date();
		System.out.println("Read: " + MyUtilities.subtractTime(d1, d2));
		// Start timer for encryption
		d1 = new Date();

		// Try to encrypt fields of patient
		try {
			for (int j = 0; j < pData.size(); j++)
			{
				// Encrypt patient
				pData.get(j).toEncryptPatient();

				// Show progress of encryption
				//MyUtilities.PrintPercentProgress(j, pData.size(), 2, '=');
			}
		} catch (Exception e) {
			System.out.println("Could not encrypt patients..");
			e.printStackTrace();
		}

		// Stop timer for encryption & print
		d2 = new Date();
		System.out.println("Encrypt: " + MyUtilities.subtractTime(d1, d2));
		// Start timer for DB write
		d1 = new Date();


		// Determine how to write encrypted Data
		if (MyUtilities.getWriter().equals("database"))
			MyUtilities.WriteToDatabase(pData, dbc, MyUtilities.getDatabase(), MyUtilities.getOutput_name());
		else if (MyUtilities.getWriter().equals("stdout"))
			MyUtilities.WriteToStdOut(pData);
		else	
			MyUtilities.WriteToFile(pData, MyUtilities.getOutput_name());


		// Stop DB write timer & print
		d2 = new Date();
		System.out.println("Write: " + MyUtilities.subtractTime(d1, d2));

		// Clean up and exit.
		dbc.close();
		System.out.println("Program finished successfully.");
	}

}
