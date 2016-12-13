package rosita.linkage.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import rosita.linkage.MappedPair;


public class MappingConfig 
{
	public static final int SOURCE_A = 0;
	public static final int SOURCE_B = 1;

	private String A_table;
	private String B_table;
	
	private ArrayList<String> A_columnNames = null;
	private ArrayList<String> B_columnNames = null;

	private String A_columnNamesCSV = null;
	private String B_columnNamesCSV = null;

	private ArrayList<Integer> A_Indices = null;
	private ArrayList<Integer> B_Indices = null;

	private HashMap<String, Integer> A_attributesToIndices = null;
	private HashMap<String, Integer> B_attributesToIndices = null;

	private HashMap<Integer, String> A_IndicesToAttributes = null;
	private HashMap<Integer, String> B_IndicesToAttributes = null;

	private MappedPair blockingPair = null;

	private ArrayList<MappedPair> mappedPairs = null;

	/**
	 * Class Constructor
	 * @param A_columnNames - An array of SQL table columnNames for data source A
	 * @param B_columnNames - An array of SQL table columnNames for data source B
	 * @param mappedPairs - An array of MappedPair objects generated from the XML file
	 */
	public MappingConfig(String A_table, String[] A_columnNames, String B_table, String[] B_columnNames,
			ArrayList<MappedPair> mappedPairs, MappedPair blockingPair)
	{
		// Initialize items from parameters
		this.A_table = A_table;
		this.B_table = B_table;

		this.A_columnNames = new ArrayList<String> (Arrays.asList(A_columnNames));
		this.B_columnNames = new ArrayList<String> (Arrays.asList(B_columnNames));

		this.mappedPairs = mappedPairs;
		this.blockingPair = blockingPair;

		// Initialize all the maps & data structures
		init();

		// Call setup to fill the data structures
		setup();
	}

	/**
	 * Class Constructor
	 * @param A_columnNames - An array of SQL table columnNames for data source A
	 * @param B_columnNames - An array of SQL table columnNames for data source B
	 * @param mappedPairs - An array of MappedPair objects generated from the XML file
	 */
	public MappingConfig(String A_table, String[] A_columnNames, String B_table, String[] B_columnNames,
			ArrayList<MappedPair> mappedPairs)
	{
		// Initialize items from parameters
		this.A_table = A_table;
		this.B_table = B_table;

		this.A_columnNames = new ArrayList<String> (Arrays.asList(A_columnNames));
		this.B_columnNames = new ArrayList<String> (Arrays.asList(B_columnNames));

		this.mappedPairs = mappedPairs;

		// Initialize all the maps & data structures
		init();

		// Call setup to fill the data structures
		setup();
	}


	public String getAttributeByIndex(int index, int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
		{
			if (A_IndicesToAttributes.containsKey(index))
				return A_IndicesToAttributes.get(index);
			return "";
		}

		if (DATA_SOURCE == MappingConfig.SOURCE_B)
		{
			if (B_IndicesToAttributes.containsKey(index))
				return B_IndicesToAttributes.get(index);
			return "";
		}

		err_invalidDataSource();
		return "";
	}


	public MappedPair getBlockingPair()
	{
		return blockingPair;
	}

	public String getBlockingColumnName(int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			return blockingPair.getColA();
		if (DATA_SOURCE == MappingConfig.SOURCE_B)
			return blockingPair.getColB();

		err_invalidDataSource();
		return null;
	}


	/**
	 * Fetch the columnNames for a particular data source
	 * @param DATA_SOURCE - Either MappingConfig.SOURCE_A or SOURCE_B
	 * @return - The array of columnNames
	 */
	public ArrayList<String> getColumnNames(int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			return A_columnNames;
		if (DATA_SOURCE == MappingConfig.SOURCE_B)
			return B_columnNames;

		err_invalidDataSource();
		return null;
	}

	/**
	 * Fetch the columnNames for a particular data source, separated by commas
	 * Only get those columnNames that appear in the MappedPair
	 * @param DATA_SOURCE - Either MappingConfig.SOURCE_A or SOURCE_B
	 * @return - The array of columnNames, separated by commas
	 */
	public String getColumnNamesCSV(int DATA_SOURCE)
	{

		if (DATA_SOURCE == MappingConfig.SOURCE_A)		
		{
			if (A_columnNamesCSV == null)
				A_columnNamesCSV = convertToCSV(A_columnNames);
			return A_columnNamesCSV;
		}

		if (DATA_SOURCE == MappingConfig.SOURCE_B)		
		{
			if (B_columnNamesCSV == null)
				B_columnNamesCSV = convertToCSV(B_columnNames);
			return B_columnNamesCSV;
		}

		err_invalidDataSource();
		return null;
	}

	/**
	 * Given some attribute, find the index of that item in the _Indices list
	 * for a particular data source (either SOURCE_A or SOURCE_B)
	 * @param attr - The attribute such as SSN, PHONE, etc...
	 * @param DATA_SOURC - Can be either MappingConfig.SOURCE_A or SOURCE_B
	 * @return - The index in _Indices where the attribute resides
	 */
	public int getIndexByAttribute(String attr, int DATA_SOURCE)
	{
		String err_message = "Could not find attribute" + attr + " in A_attributesToIndices\n" 
		+ "Program terminated.";

		if (DATA_SOURCE == MappingConfig.SOURCE_A)
		{
			if (A_attributesToIndices.containsKey(attr))
				return A_attributesToIndices.get(attr);
			MyUtilities.end(err_message);
		}

		if (DATA_SOURCE == MappingConfig.SOURCE_B)
		{
			if (B_attributesToIndices.containsKey(attr))
				return B_attributesToIndices.get(attr);
			MyUtilities.end(err_message);
		}

		err_invalidDataSource();
		return -1;
	}




	/**
	 * Given some SQL table columnName, find it's index in the array.
	 * O(n) search, but there shouldn't be too many columnNames to look through. 
	 * @param columnName - The columnName to search for.
	 * @param DATA_SOURCE - The data source to use when searching, either MappingConfig.SOURC_A or SOURCE_B
	 * @return - The index where columnName resides
	 */
	public int getIndexByColumnName(String columnName, int DATA_SOURCE)
	{

		ArrayList<String> searchList = null;

		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			searchList = this.A_columnNames;
		else if (DATA_SOURCE == MappingConfig.SOURCE_B)
			searchList = this.B_columnNames;
		else
			err_invalidDataSource();

		// Search the columnName array list to find an index for the specified columnName
		for (int i = 0; i < searchList.size(); i++)
		{
			String c = searchList.get(i);
			if (c.equalsIgnoreCase(columnName))
				return i;
		}

		MyUtilities.end("Could not locate column \"" + columnName + "\" in the table.\n"
				+ "\nPlease ensure table names are spelled correclty in the configuration file.\n"
				+ "Program Terminated.");

		return -1;

	}

	/**
	 * Get the indices for a particular data source.
	 * The data-source can be either MappingConfig.SOURCE_A or SOURCE_B
	 * @param DATA_SOURCE - The data source to get the indices for
	 * @return - The array of indices for a particular data source
	 */
	public ArrayList<Integer> getIndices(int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			return A_Indices;
		if (DATA_SOURCE == MappingConfig.SOURCE_B)
			return B_Indices;

		err_invalidDataSource();
		return null;
	}

	public MappedPair getMappedPairByColumnName(String colName)
	{
		MappedPair retMp = null;

		for (MappedPair mp : mappedPairs)
		{
			String colA = mp.getColA();
			String colB = mp.getColB();
			if (colA.equals(colName) || colB.equals(colName))
				retMp = mp;
		}

		return retMp;
	}


	/**
	 * Getter for the mappedPairs list
	 * @return ArrayList<MappedPair> mappedPairs
	 */
	public ArrayList<MappedPair> getMappedPairs()
	{
		return mappedPairs;
	}

	/**
	 * Get the number of mappedPairs that we have.
	 * This also the number of items we should have in the other arrays
	 * @return - The number of columnNames being stored in MappingConfig.
	 */
	public int getNumItems()
	{
		return mappedPairs.size();
	}

	/** 
	 * Get a table name based on the data_source given
	 * @param DATA_SOURCE - Either MappingConfig.SOURCE_A or SOURCE_B
	 * @return - The table name for the data source
	 */
	public String getTable(int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			return A_table;
		if (DATA_SOURCE == MappingConfig.SOURCE_B)
			return B_table;

		err_invalidDataSource();
		return "";
	}
	
	public String getURL(int DATA_SOURCE)
	{
		if (DATA_SOURCE == MappingConfig.SOURCE_A)
			return A_table;
		if (DATA_SOURCE == MappingConfig.SOURCE_B)
			return B_table;

		err_invalidDataSource();
		return "";
	}


	/**
	 * Convert an array of Strings to a single string with all the elements
	 * separated by Commas.
	 * Also ensure we don't place too many commas.
	 * @param stringArray - The array to convert
	 * @return - The CSV of the original array.
	 */
	private String convertToCSV(ArrayList<String> stringArray)
	{
		StringBuilder sb = new StringBuilder();

		String s = "";

		for (int i = 0; i < stringArray.size(); i++) 
		{
			s = stringArray.get(i);

			// Don't put empty strings.
			if (!s.equals(""))
			{
				sb.append(stringArray.get(i));

				// Handle the comma situation
				if (i != stringArray.size() -1 )
				{
					// Search stringArray forward for values, 
					// only place a comma if you find one.
					int j = i;
					boolean placeComma = false;
					while (placeComma == false && j < stringArray.size() - 1)
						if (!stringArray.get(++j).equals(""))
							placeComma = true;

					if (placeComma)
						sb.append(",");
				}


			}

		}

		return sb.toString();
	}

	/**
	 * When an invalid data source is specified, 
	 * end the program with an error message.
	 */
	private void err_invalidDataSource()
	{
		MyUtilities.end("Invalid data source specified. \nProgram Terminated");
	}

	/**
	 * Only keep those columnNames that appear in the MappedPairs list.
	 * There's no need to keep columnNames that won't be encrypted or written.
	 */
	private void removeExtraColumnNames()
	{
		// Once we have all the indices of tanObjecthe mapped pairs, remove the columnNames we don't need
		for (int i = 0; i < A_columnNames.size(); i++)
		{
			boolean keep = false;
			for (int j : A_Indices)
			{
				if (i == j) keep = true;
			}
			if (!keep)
				A_columnNames.set(i, "");	
		}

		for (int i = 0; i < B_columnNames.size(); i++)
		{
			boolean keep = false;
			for (int j : B_Indices)
			{
				if (i == j) keep = true;
			}
			if (!keep) {
				B_columnNames.set(i, "");
			}
		}
	}

	/**
	 * Initialize the class members that weren't set from the constructor's parameters
	 */
	private void init()
	{
		// Initialize all the maps & data structures
		A_Indices = new ArrayList<Integer>();
		B_Indices = new ArrayList<Integer>();

		A_attributesToIndices = new HashMap<String, Integer>();
		B_attributesToIndices = new HashMap<String, Integer>();

		A_IndicesToAttributes = new HashMap<Integer, String>();
		B_IndicesToAttributes = new HashMap<Integer, String>();
	}


	/**
	 * Setup the class members.  
	 * Retrieve the info from the MappedPairs List.
	 */
	private void setup()
	{
		int indexA;
		int indexB;
		String colA;
		String colB;

		// Extract data from the mapped pairs and put it in the hashMaps
		// And in the arrays lists
		for (MappedPair mp : mappedPairs)
		{
			colA = mp.getColA();
			colB = mp.getColB();

			indexA = getIndexByColumnName(colA, MappingConfig.SOURCE_A);
			indexB = getIndexByColumnName(colB, MappingConfig.SOURCE_B);	

			A_Indices.add(indexA);
			B_Indices.add(indexB);

			mp.setIndices(indexA, indexB);

			// Make the association between attributes and indices
			String attr = mp.getAttribute();
			if (!attr.equalsIgnoreCase(""))
			{
				A_attributesToIndices.put(attr, indexA);
				A_IndicesToAttributes.put(indexA, attr);

				B_attributesToIndices.put(attr, indexB);
				B_IndicesToAttributes.put(indexB, attr);
			}	

		}

		// Also setup the blockingPair
		if (blockingPair != null)
		{
			indexA = getIndexByColumnName(blockingPair.getColA(), MappingConfig.SOURCE_A);
			indexB = getIndexByColumnName(blockingPair.getColB(), MappingConfig.SOURCE_B);
			blockingPair.setIndices(indexA, indexB);
		}
		

		removeExtraColumnNames();

	}



}



