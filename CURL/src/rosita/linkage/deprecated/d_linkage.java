package rosita.linkage.deprecated;


import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rosita.linkage.main;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.FileIO;
import rosita.linkage.util.CSVParser;
import rosita.linkage.util.MyUtilities;



public class d_linkage 
{

	// Constants used as program configurations
	private static final String linkage_config = "cfg/linkage.properties";
	private static final String field_weights = "data/FieldWeights.txt";
	private static final String block_sizes = "log/blockSizes/blockSizes.dat";
	private static final int MAX_COUNT = 100000; 

	// Keep track of the linkage and record table across this class.
	private static String l_table = "";
	private static String r_table = "";

	// The set of values to be used for matching
	private static ArrayList<String> matchingSet = populateMatchingSet();

	/** 
	 * Suppress default constructor for non-instantiability
	 */
	private d_linkage() 
	{
		throw new AssertionError();
	}


	private static void printMap(HashMap<String, Integer> m)
	{
		for (String key : m.keySet())
			System.out.format("%s : %d %n", key, m.get(key));
	}



	/**
	 *  Linkage function
	 */
	public static void linkage()
	{
		// ******************************************************************************
		// ** 1. Linked Records Database Setup & Read
		// ******************************************************************************

		// Message: Linked Records Setup & Read
		System.out.println("Linked Records Setup & Read...");

		DatabaseConnection linkedDBC = connectWithProperties(linkage_config, 'L');

		// Message: Reading performed
		System.out.println("Reading in linked records from: " + l_table + "..." );

		// Get the whole linked records table
		ResultSet linkedSqlResults = linkedDBC.getTable(l_table);
		String[] linkedColumnNames = linkedDBC.getColumnNames(linkedSqlResults);

		// Set Linked Record columnNameMap
		HashMap<String, Integer> linkedColumnNameMap = MyUtilities.returnMap(linkedColumnNames);
		Record.setLinkedColumnNameMap(linkedColumnNameMap);

		// Also determine number of linked records
		int linkedRecordCount = linkedDBC.getRowCount(linkedSqlResults);

		// Read from LinkedRecords database into LinkedRecords ArrayList
		String[] dataRow; int count = 0;
		ArrayList<Record> linkedRecords = new ArrayList<Record>(linkedRecordCount);
		while ((dataRow = linkedDBC.getNextResult(linkedSqlResults)) != null && count++ < MAX_COUNT)
			linkedRecords.add(new Record(dataRow, true, true));

		// Message: Linked reading/setup success.
		System.out.println("Linked Database Reading Successful!");


		// ******************************************************************************
		// **  2. Records Database Setup
		// ******************************************************************************

		// Message: Regular record setup
		System.out.println("Record Databse Read Setup...");

		// Database Declarations/Initializations
		DatabaseConnection readDBC = connectWithProperties(linkage_config, 'R');

		// Only read in one record so we can get the column names.
		ResultSet sqlResults = readDBC.getTableQuery("SELECT * FROM "+ r_table + " LIMIT 1");
		String[] columnNames = readDBC.getColumnNames(sqlResults);

		// Set Record columnNameMap
		HashMap<String, Integer> columnNameMap = MyUtilities.returnMap(columnNames);
		Record.setColumnNameMap(columnNameMap);

		// Also grab the total number of records
		sqlResults = readDBC.getTableQuery("SELECT COUNT(*) AS COUNT FROM " + r_table + " ;");
		dataRow = readDBC.getNextResult(sqlResults);
		int recordCount = new Integer(dataRow[0]);

		// Message: Record setup
		System.out.println("Record Databse Read Setup Succcess!");


		// ******************************************************************************
		// ** 3. Field  Weights Generation (Wa & Wd)
		// ******************************************************************************

		// Message: Field weight setup
		System.out.println("Setting Up Field Weights...");

		// Try to read in field weight data from file
		CSVParser csvp = new CSVParser(MyUtilities.ReadFile(field_weights));
		String[] fieldWeightColumnNames = null;
		String[] str_matches    = null;
		String[] str_nonMatches = null;
		try {
			fieldWeightColumnNames = csvp.readNext();
			str_matches = csvp.readNext();
			str_nonMatches = csvp.readNext();
		} catch (IOException e1) {
			System.err.println("Could not read field weights from file.");
			e1.printStackTrace();
			System.exit(1);
		}

		// Convert string arrays to arrays of doubles
		Double[] dbl_matches = new Double[str_matches.length];
		Double[] dbl_nonMatches = new Double[str_nonMatches.length];;
		for (int i = 0; i < str_matches.length; i++)
			dbl_matches[i] = new Double(str_matches[i]);
		for (int i = 0; i < str_nonMatches.length; i++)
			dbl_nonMatches[i] = new Double(str_nonMatches[i]);

		// Create map of field weight names to weight indices
		HashMap<String, Integer> fieldWeightMap = MyUtilities.returnMap(fieldWeightColumnNames);

		// I could have created 2 maps of String -> Doubles for matches/nonMatches
		// But there could be more (or less) fieldWeights in the file than we need to match with.

		// Message: Field weight setup success.
		System.out.println("Field Weight Setup Successful!");


		// ******************************************************************************
		// ** 4. Calculate DownWeights from Missing Record Data, Wa & Wd
		// ******************************************************************************

		// Message: Downweight & Missing values being calculated...
		System.out.println("Calculating downweight data from missing values in records & linked records...");

		// Create two hashMaps of attributes to the number of missing values for that attribute.
		Map<String, Integer> missingRecords = getMissingRecords(columnNames, columnNameMap, readDBC, r_table);
		Map<String, Integer> linkedMissingRecords = getMissingRecords(linkedColumnNames, linkedColumnNameMap, linkedDBC, l_table);

		// Calculate missing percentages for Record and LinkedRecord attributes
		Map<String, Double> missingPercentages = new HashMap<String, Double>(matchingSet.size());

		// Iterate through all values that are being matched upon to get missing percentages
		for (String key : matchingSet)
		{
			// Add the values from each missing map.
			int n = missingRecords.get(key) + linkedMissingRecords.get(key);

			// Divide the sum by the total number in "Records" + "Linked Records"
			double percentage = (double)n / ((double) (recordCount + linkedRecordCount));
			missingPercentages.put(key, percentage);
		}

		// Print percentages.
		//for (String key : missingPercentages.keySet())
		//	System.out.println(key + " : " + MyUtilities.toDecimals( 100*missingPercentages.get(key), 2) + "%" );


		// Loop Through Each Wa & Wd value & weight it down with missing percentages
		for (String key : matchingSet)
		{	
			double Wa = dbl_matches[fieldWeightMap.get(key)];
			double Wd = dbl_nonMatches[fieldWeightMap.get(key)];

			dbl_matches[fieldWeightMap.get(key)]= Wa * ( 1 - missingPercentages.get(key)); 
			dbl_nonMatches[fieldWeightMap.get(key)] = Wd * ( 1 - missingPercentages.get(key));
		}

		// Print aggreement/disagreement weights.
		//System.out.println("----------------------------------------------");
		//for (String key : matchingSet) {
		//	System.out.println(key + " Wa: " + dbl_matches[fieldWeightMap.get(key)]);
		//	System.out.println(key + " Wd: " + dbl_nonMatches[fieldWeightMap.get(key)]);
		//}

		// Message: Down weight calculation success.
		System.out.println("Downweight calculation success!");


		// ******************************************************************************
		// ** 5. Perform the Linkage Step
		// ******************************************************************************
		// ** At this point in the code, we have (at least) the following important
		// **  data structures initialized and at our disposal.
		// **
		// ** ArrayList<Record> records;
		// ** ArrayList<Record> linkedRecords;
		// **
		// ** Map<String, Integer> missingRecords
		// ** Map<String, Integer> linkedMissingRecords;
		// **
		// ** Map<String, Double> missingPercentages;
		// **
		// ** HashMap<String, Integer> fieldWeightMap;
		// ** Double[] dbl_matches;
		// ** Double[] dbl_nonMatches;
		// ******************************************************************************

		// Message: Linkage started
		System.out.println("Performing the linkage step...");

		// Iterate through all the linked records
		for (int i = 0; i < linkedRecordCount; i++)
		{
			
			// Start timer for one linked record
			Date d1 = new Date();
			
			// Get a linkedRecord from the set
			Record lr = linkedRecords.get(i);

			// Get the DOB HASH of this linked record.
			String hash = lr.getValue(MyUtilities.DOBHASH);

			// Generate record set with only this hash
			// TODO: Fix this query so that it will reterive the "records" DOBHASH field.
			String query = "SELECT * FROM " + r_table + " \nWHERE ";
			query += "PE_DOBHASH" + "=\"" + hash + "\";";
			sqlResults = readDBC.getTableQuery(query);

			// Determine how big the set is
			Integer blockSize = readDBC.getRowCount(sqlResults);
			Double blockPercentage = (double)blockSize / (double)recordCount;
			blockPercentage = MyUtilities.toDecimals(blockPercentage, 4);
			FileIO.AppendToFile(blockPercentage.toString() + "\n", block_sizes);
			
			
			Date d2 = new Date();

			System.out.format("Linked Record %s/%s - %s ms%n", 
					i, linkedRecordCount, MyUtilities.subtractTime(d1, d2) );

			// TODO: Get code below here to work!
			//			ArrayList<Record> records = new ArrayList<Record>();
			//			while ((dataRow = readDBC.getNextResult(sqlResults)) != null)
			//				records.add(new Record(dataRow, true, true));
			//
			//			// Determine the sensitivity for each record in this set
			//			double sens;
			//			List<Double> sensList = new ArrayList<Double>();
			//
			//			for (int i = 0; i < records.size(); i++)
			//			{
			//				Record r = records.get(i);
			//				sens = getSensitivity(r, lr, fieldWeightMap, dbl_matches, dbl_nonMatches);
			//				sensList.add(sens);
			//			}
			//			Collections.sort(sensList);
			//
			//			String s = "";
			//			for (Double d : sensList)
			//			{
			//				s += d + "\n";
			//			}
			//			MyUtilities.AppendToFile(s, "log/linkage.dat");
		}


		System.out.println("done");


	}

	/**
	 * Populate the set of values that need to be matched upon.
	 * This may be used in the linkage or encryption procedure.
	 * @return - An ArrayList of containing predefined markers that should be matched upon
	 */
	private static ArrayList<String> populateMatchingSet()
	{
		if (matchingSet == null)
		{
			matchingSet = new ArrayList<String>();
			matchingSet.add(MyUtilities.LASTNAME);
			matchingSet.add(MyUtilities.FIRSTNAME);
			matchingSet.add(MyUtilities.SSN);
			matchingSet.add(MyUtilities.GENDER);
			matchingSet.add(MyUtilities.ADDR_1);
			matchingSet.add(MyUtilities.CITY);	
		}

		return matchingSet;

	}

	/**
	 * Return a map of column names -> number of missing values in the data set.
	 * Query MySQL using a database connection to make the determination.
	 * @param columnNames - the column names of the dataset
	 * @param columnNameMap - The columns to match upon -> indicies of original column names.
	 * @param dbc - a valid database connection.
	 * @param tableName - the table to determine missing values from.
	 * @return - A map of the MATCHING NAMES to the number of mising values for each column.
	 */
	private static HashMap<String, Integer> getMissingRecords(String[] columnNames, HashMap<String, Integer> columnNameMap, 
			DatabaseConnection dbc, String tableName )
			{
		int n = matchingSet.size();
		HashMap<String,Integer> missingRecords = new HashMap<String, Integer>(n);
		StringBuilder recordQuery = new StringBuilder("");
		String encryptedNotInMapValue = Record.getEncryptedNotInMapValue();

		// Iterate through each value that needs to be matched upon
		for (int i = 0; i < n; i++)
		{
			String key = matchingSet.get(i);

			// Determine the index for the linked and regular records.
			int index = columnNameMap.get(key);

			// Get the colunNames
			String name = columnNames[index];

			recordQuery.append("SELECT '" + key + "' as count_type, ");
			recordQuery.append("COUNT(" + name + ")\n");
			recordQuery.append("FROM " + tableName + "\n");
			recordQuery.append("WHERE " + name + "=\"" + encryptedNotInMapValue + "\"\n");

			if (i == matchingSet.size() -1 )
				recordQuery.append(";\n");
			else 
				recordQuery.append("UNION ALL \n");
		}

		String[] dataRow;
		ResultSet sqlResults = dbc.getTableQuery(recordQuery.toString());
		while ((dataRow = dbc.getNextResult(sqlResults)) != null)
			missingRecords.put(dataRow[0], new Integer(dataRow[1]) );

		return missingRecords;
			}


	/**
	 * Establish a database connection with a properties file.
	 * @param propertiesLocation - The location of the properties file.
	 * @param prefix - The prefix to use for reading in properties.  
	 * @return - An initialized database connection
	 */
	private static DatabaseConnection connectWithProperties(String propertiesLocation, char prefix)
	{
		Properties p = MyUtilities.readProperties(propertiesLocation);
		String url = p.getProperty( prefix + "_URL");
		String database = p.getProperty(prefix + "_DATABASE");
		String table = p.getProperty(prefix +  "_TABLE");
		String user = p.getProperty(prefix + "_USER");
		String password = p.getProperty(prefix + "_PASSWORD");

		DatabaseConnection dbc = new DatabaseConnection(url, 
				main.mysqlDriver, database, user, password);

		// Ensure database table exists before reading
		if (! dbc.checkTableExists(table))
		{
			System.err.println("Error: table " + table + " does not exist!");
			System.err.println("Program Terminated.");
			System.exit(1);
		}

		// Allow the rest of this file to get the table.
		if (prefix == 'L')
			l_table = table;
		if (prefix == 'R')
			r_table = table;


		return dbc;
	}


	/**
	 * Compare the bloom filter of two of the same fields of different records
	 * Return a double using the dice coefficient comparison technique
	 * @param r - The first record used for comparison
	 * @param lr - The second record used for comparison
	 * @param key - The key to determine which field should be compared upon
	 * @return - The dice coefficient of the field.
	 */
	private static double findDice(Record r, Record lr, String key)
	{
		double dice = 0;
		int hitsRecord = 0;
		int hitsLinkedRecord = 0;
		int hitsSimilar = 0;

		String sr = r.getValue(key);
		String slr = lr.getValue(key);

		for (int i = 0; i < sr.length(); i++)
		{
			if (sr.charAt(i) == '1')
				hitsRecord++;
			if (slr.charAt(i) == '1')
				hitsLinkedRecord++;
			if (slr.charAt(i) == '1' && sr.charAt(i) == '1')
				hitsSimilar++;
		}

		if (hitsRecord == 0 || hitsLinkedRecord == 0)
			return 0.0;

		dice = 2.0 * (double) hitsSimilar;
		dice /= (double)(hitsRecord + hitsLinkedRecord);

		return dice;

	}

	/**
	 * Returns a double representing the score of a record and linked record's fields
	 * scores are computed using weights for each field & similarity between each field.
	 * @param r - The Record used for comparison
	 * @param lr - The second Record used for comparison
	 * @param fieldWeightMap - a map of field weights names to indices
	 * @param matches - the weights for a match
	 * @param nonMatches - the weights for a non-match
	 * @return - The sensitivity between two different records
	 */
	private static double getSensitivity(Record r, Record lr, 
			HashMap<String, Integer> fieldWeightMap, Double[] matches, Double[] nonMatches )
	{
		double sen = 0;
		double dice = 0;

		// TODO: fix this sensitivity function!

		//		{
		//			if (fieldWeightMap.containsKey(key))
		//			{
		//				double match = matches[fieldWeightMap.get(key)];
		//				double nonMatch = nonMatches[fieldWeightMap.get(key)];
		//				dice = findDice(r, lr, key);
		//				sen += (dice * match) + ((1-dice)*nonMatch);
		//			}
		//		}

		return MyUtilities.toDecimals(sen, 4);
	}




}
