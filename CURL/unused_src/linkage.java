import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


public class linkage 
{
	// Properties file location
	private static final String properties_location = "cfg/linkage.properties";
	
	// Credentials for Database connections
	private static String database = "default_database";
	private static String table = "default_table";
	private static String url = "default_url";
	private static String driver = "default_driver";
	private static String user = "default_user";
	private static String password = "default_password";
	private static DatabaseConnection dbc = null;
	
	// Column Names References
	public static final String SSN = "SSN";
	public static final String FIRSTNAME = "FIRSTNAME";
	public static final String LASTNAME = "LASTNAME";
	public static final String DOB = "DOB";
	public static final String SEX = "SEX";
	public static final String ADDR = "ADDR";
	public static final String CITY = "CITY";
	public static final String STATE = "STATE";
	public static final String ZIP = "ZIP";
	public static final String PHONE = "PHONE";
	
	// TODO add function documentation here
	private static HashMap<String, Integer> returnMap(String[] columnName){
		HashMap<String, Integer> myMap = new HashMap<String, Integer>();

		for (int i = 0; i < columnName.length; i++)
		{
			if (columnName[i].contains("SSN"))
				myMap.put(SSN, i);
			else if(columnName[i].contains("SOCIAL"))
				myMap.put(SSN, i);
			else if(columnName[i].contains("FIRST"))
				myMap.put(FIRSTNAME, i);
			else if(columnName[i].contains("FN"))
				myMap.put(FIRSTNAME, i);
			else if(columnName[i].contains("LAST"))
				myMap.put(LASTNAME, i);
			else if(columnName[i].contains("LN"))
				myMap.put(LASTNAME, i);
			else if(columnName[i].contains("SIR"))
				myMap.put(LASTNAME, i);
			else if(columnName[i].contains("BIRTH"))
				myMap.put(DOB, i);
			else if(columnName[i].contains("DOB"))
				myMap.put(DOB, i);
			else if(columnName[i].contains("DATE") && columnName[i].contains("B"))
				myMap.put(DOB, i);
			else if(columnName[i].contains("SEX"))
				myMap.put(SEX, i);
			else if(columnName[i].contains("CITY"))
				myMap.put(CITY, i);
			else if(columnName[i].contains("GEN"))
				myMap.put("SEX", i);
			else if(columnName[i].contains("ADD"))	
				myMap.put(ADDR, i);
			else if(columnName[i].contains("STATE"))
				myMap.put(STATE, i);
			else if(columnName[i].contains("Z"))	
				myMap.put(ZIP, i);
			else if(columnName[i].contains("PHO"))
				myMap.put(PHONE, i);
		}
		return myMap;
	}
	
	
	/**
	 * ReadProperties reads in database login credentials from
	 * file "properties_location."  Each credential is set as 
	 * a global variable.
	 * @return void
	 */
	private static void ReadProperties()
	{
		Properties props = new Properties();

		// Try loading properties from file
		try { 
			props.load(new FileInputStream(properties_location));
		} catch (FileNotFoundException e) {
			System.out.println("Error: File " + properties_location + " not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error: Unknown IO Exception.");
			e.printStackTrace();
		}

		// File load success, getProperties
		database = props.getProperty("DATABASE");
		table = props.getProperty("TABLE");
		url = props.getProperty("URL");
		driver = props.getProperty("DRIVER");
		user = props.getProperty("USER");
		password = props.getProperty("PASSWORD");
	}
	
	public static double getSensitivity(Patient input1, Patient input2) throws Exception{
		BF compare, compareTo;
		double value = 0;
		double dice;
		//FieldWieghts weights = new FieldWieghts();
		for(int j = 0; j < input1.getAttributes().size(); j++){
			if(j != 3){
				dice = findDice(input1, input2, j);
				value += dice*weights.match(j) + (1-dice)*weights.nonMatch(j);
			}
		}
		value = (int)(10000*value);
		value = (value/10000);
		return value;
	}
	public static double findDice(Patient p1, Patient p2, int i){
		int hits = 0;
		int inCount = 0;
		int count = 0;
			BF check = p2.getInstance(i);
		for (int x = 0; x < check.size(); x++){
			if(check.getBit(x) && p1.getInstance(i).getBit(x))
            	hits++;
            if(check.getBit(x))
            	inCount++;
            if(p1.getInstance(i).getBit(x))
            	count++;
		}
		if((count == 0) || (inCount) == 0)
			return 0.0;
        return 2.0*((double)hits/(count + inCount));
	}
	
	
	/**
	 * Main program entry for linkage.
	 * @param args Currently unused.
	 */
	public static void main(String args[])
	{
		// Read in properties from file
		ReadProperties();
		
		// Establish database connection
		dbc = new DatabaseConnection(url + database, driver, user, password);
		ResultSet sqlResults = dbc.getTable(table);
		ResultSetMetaData sqlRSMD = dbc.getTableMetaData(sqlResults);
		int rowCount = dbc.getRowCount(sqlResults);
		int columnCount = dbc.getColumnCount(sqlResults);
		String[] columnNames = dbc.getColumnNames(sqlRSMD);
		
		// Set Patient columnNameMap
		Patient.setColumnNameMap(returnMap(columnNames));
		
		// Initialize items to manage patient data
		ArrayList<Patient> pData = new ArrayList<Patient>();
		Patient tempPatient = null;
		String [] rowOfData;
		
		// Read data into patients
		try
		{
			for (int i = 0; i < rowCount; i++ )
			{
				rowOfData = new String[columnCount];
				for (int j = 0; j < columnCount; j++)
					rowOfData[j] = sqlResults.getString(columnNames[j]);
				tempPatient = new Patient(rowOfData);
				tempPatient.setIsEncrypted(true);
				pData.add(tempPatient);
				sqlResults.next();	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ---------------- LINKAGE START -----------------		
		Patient match = pData.get(33695);
		
		Patient p = new Patient ("Bert", "Jahde", "543830244", "08/27/1998",
				"", "M", "3264 N Pleasantview Dr", "", 
				"CASTLE Rock", "", "303-814-2902", "Henry", false);
				;
		p.toEncryptPatient();
		
		System.out.println("Sensitivity:" + getSensitivity(match, p));
		
		
		
		
		
	}
}
