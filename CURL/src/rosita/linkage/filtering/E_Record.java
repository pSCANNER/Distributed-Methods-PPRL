package rosita.linkage.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rosita.linkage.MappedPair;
import rosita.linkage.util.MappingConfig;
import rosita.linkage.util.MyUtilities;

public class E_Record 
{
	// This map will dictate how to access the fields of the records
	private static MappingConfig mapConfig;

	// Set up members dealing with the encryption filters
	private static BloomFilter<String> encryptionFilter = null;
	private static final int defaultBitSetSize = 1000;
	//private static final int defaultExpectedNumberOfElements = 11;
	private static final int defaultExpectedNumberOfElements = 20;
	private static List<BloomFilter<String>> threadedEncryptionFilters = null;

	// The string to use instead when null values are found.
	private static final String nullValue = "";
	private static String encryptedNullValue = null;

	// Access control members
	private String[] values;
	private String[] types;
	private static int length;
	private String blockingValue;
	private static MappedPair blockingPair = null;

	private static HashMap<String , Integer> attributesToIndicies;
	private static HashMap<Integer, Integer> A_indexToNewIndex;
	private static ArrayList<Integer> writeOrder;

	// Record status members
	private static boolean isAttrMapSetup = false;
	private static boolean isFilterSetup = false;
	private static boolean setupComplete = false;
	private static boolean isIndexMapSetup = false;
	private static boolean isWriteOrderSetup = false;
	private boolean isEncrypted = false;

	/**
	 * Class constructor.
	 * Takes a row of SQL data & keeps track of only those inputs that 
	 * we will need to eventually normalize/encrypt.
	 * @param inputValues - A tuple from the SQL database.
	 */
	public E_Record(String inputValues[], String inputTypes[])
	{
		if (!setupComplete)
			MyUtilities.end("Please call E_Record.setup() prior to creating any records" +
			"\nProgram Terminated.");

		this.values = new String[length];
		this.types = new String[length];

		// There's no need to store values that won't be encrypted.
		// Use the indexMap to put values in
		for (int aIndex : A_indexToNewIndex.keySet())
		{
			int newIndex = A_indexToNewIndex.get(aIndex);
			// set the values, parsing nulls.
			if (inputValues[aIndex].equals("null") || inputValues[aIndex] == null)
				this.values[newIndex] = nullValue;
			else
				this.values[newIndex] = inputValues[aIndex];
			
			//System.out.format("values[%d] = input[%d] = %s%n", newIndex, aIndex, inputValues[aIndex]);
		}
		
		this.types = inputTypes;
		
		

		// Also, setup the blockingValue
		blockingValue = inputValues[blockingPair.getIndexA()];

	}



	/**
	 * Using the Bloom Filter, encrypt all the values of this record.
	 * Also, create the DOBHASH.
	 */
	public void encryptValues()
	{
		// No need to encrypt if already encrypted
		if (isEncrypted) return;

		// First, Create the DOBHASH
		String s = blockingValue;
		if (!s.equals(""))
			s = s.substring(0, 4);
		//blockingValue = MyUtilities.SHA1(s);
		blockingValue = MyUtilities.SHA512(s);

		// Encrypt all of this record's values.
		for (int i = 0; i < values.length; i++)
		{

			s = values[i];
			if ( s.equals(""))
				values[i] = encryptedNullValue;
			else
			{
				encryptionFilter.clear();
				encryptionFilter.addAsBigrams(s);
				encryptionFilter.toString();
			}

		}

		// Finally set this record's encryption state to true.
		isEncrypted = true;

	}

	/**
	 * Using the MappingConfig, determine which values need to be normalized.
	 * Use the Cleaner() to normalize the data.
	 */
	public void normalizeValues()
	{
		String val;
		//if(values[4].equals("943-00-2426")){
		//	int oo = 1;
		//	oo++;
			
		//}
		for( String key : attributesToIndicies.keySet())
		{
			int index = attributesToIndicies.get(key);
			val = values[index];

			if (key.equalsIgnoreCase("SSN"))
				values[index] = Cleaner.parseSSN(val);

			if (key.equalsIgnoreCase("PHONE"))
				values[index] = Cleaner.parsePhone(val);

			if (key.equalsIgnoreCase("SEX"))
				values[index] = Cleaner.parseSex(val);
		}

		// For now, assume the blocking value to be a date
		// returns the date in YYYYMMDD format
		blockingValue = Cleaner.normalizeDate(blockingValue);

	}

	/**
	 * Assuming the user is using threads to perform the encryption,
	 * Use one (threadNum) of the BloomFilters in threadedEncryptionFilters
	 * so that BloomFilters do not fill up with the wrong values.
	 * @param threadNum - The threadNumber handling this function, 
	 *   also used to access the correct BloomFilter.
	 */
	public void threadedEncryptValues(int threadNum)
	{
		// No need to encrypt if already encrypted
		if (isEncrypted) return;

		if (threadedEncryptionFilters == null)
			MyUtilities.end("Thread " + threadNum + " terminating due to " +
					"encryption filters not being setup yet.  Make a call to " +
			"E_Record.setup() to fix this problem.");

		// First, Create the DOBHASH
		String s = blockingValue;
		if (!s.equals("")){
			s = s.substring(0, s.length()>3?4:s.length());
		}
		//blockingValue = MyUtilities.SHA1(s);
		blockingValue = MyUtilities.SHA512(s);
		
		//if(values[7]=="34644") {
		//	int tempp = 0 ;
		//	tempp++;
		//}
		// Encrypt all of this record's values.
		for (int i = 0; i < values.length; i++)
		{
			s = values[i];
			
			
			
			if(types[i].equals("yes")){values[i] = s;}
			 else if (s == null){
				 values[i] = encryptedNullValue;}
			 else if (s.equals("")){
				 values[i] = encryptedNullValue;}
			 else if (types[i].equals("hash-only")){
				 values[i] = MyUtilities.SHA512(s);}
			 else
				{
					// Use the threadNum to determine which bloom filter to use.
					// That way, there won't be any overlap when running things in parallel.
					threadedEncryptionFilters.get(threadNum).clear();
					threadedEncryptionFilters.get(threadNum).addAsBigrams(values[i]);
					values[i] = threadedEncryptionFilters.get(threadNum).toString();
				}
			}
	}

	/**
	 * Get all of the values and return them as a CSV.
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < writeOrder.size(); i++)
		{
			int index = writeOrder.get(i);
			sb.append(values[index] + ",");
		}

		sb.append(blockingValue);

		return sb.toString();
	}

	/**
	 * Get all the values and return them as a CSV, but
	 * with each value surrounded by quotes.
	 */
	public String toQuotedString()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < writeOrder.size(); i++)
		{
			int index = writeOrder.get(i);

			sb.append("\"" + values[index] + "\",");

		}
		sb.append("\"" + blockingValue + "\"");
		return sb.toString();
	}
	
	public String toSingleQuotedString()
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < writeOrder.size(); i++)
		{
			int index = writeOrder.get(i);
			if(values[index]==null || values[index].length()==0)
				sb.append(values[index] + ",");
			else
				sb.append("'" + values[index] + "',");

		}
		sb.append("'" + blockingValue + "'");
		return sb.toString();
	}


	/**
	 * This function needs to be called prior to creating any 
	 * E_record objects.  Program will exit if setup is not complete and
	 * A record is created.
	 * @param mapConfig - A properly setup MappingConfig
	 * @param numFilters - The number of filters to use when encrypting
	 * (Also the number of threads to use if things are done in parallel)
	 */
	public static void setup(MappingConfig mapConfig, int numFilters)
	{
		E_Record.mapConfig = mapConfig;

		length = mapConfig.getNumItems();

		if (!isIndexMapSetup)
			setupIndexMap();

		if (!isAttrMapSetup)
			setupAttributeMap();

		if (!isFilterSetup)
			setupFilter(numFilters);

		if (blockingPair == null)
			blockingPair = mapConfig.getBlockingPair();

		if (!isWriteOrderSetup)
			setupWriteOrder();

		setupComplete = true;;
	}



	/**
	 * Setup the attribute map so that E_Record will know where
	 * the values that can be normalized are.
	 */
	private static void setupAttributeMap()
	{
		attributesToIndicies = new HashMap<String, Integer>();
		ArrayList<Integer> indices = mapConfig.getIndices(MappingConfig.SOURCE_B);

		int i = 0;

		for (int index : indices)
		{	
			// Set up the HashMap for values that will be cleaned
			String attr = mapConfig.getAttributeByIndex(index, MappingConfig.SOURCE_B);
			if (!attr.equals(""))
				attributesToIndicies.put(attr, i++);
		}
		isAttrMapSetup = true;
	}


	/**
	 * Since we don't need to store all the values given to a particular 
	 * E_Record, determine which ones we -do- need to use.  THis function
	 * keeps track of where they are in the original array (SQL tuple) and 
	 * where they are in this records values[] array.
	 */
	private static void setupIndexMap()
	{
		A_indexToNewIndex = new HashMap<Integer, Integer>();

		// Keep track of indices of the original and where they are in the values[]
		int i = 0;
		for (int index : mapConfig.getIndices(MappingConfig.SOURCE_A))
			A_indexToNewIndex.put(index, i++);

		isIndexMapSetup = true;
	}


	/**
	 * This will initialize all the filters, and the members associated with
	 * the encryption procedure
	 * @param numFilters - The number of filters to be initialized.
	 */
	private static void setupFilter(int numFilters)
	{
		// Setup up the filter & threaded filters
		encryptionFilter = new BloomFilter<String>(defaultBitSetSize, defaultExpectedNumberOfElements);
		threadedEncryptionFilters = new ArrayList<BloomFilter<String>>(numFilters);
		for (int i = 0; i < numFilters; i++)
			threadedEncryptionFilters.add(new BloomFilter<String>(defaultBitSetSize, defaultExpectedNumberOfElements));

		// Set up the
		// Note: make it null just for experimental purpose
		//encryptionFilter.clear();
		//E_Record.encryptedNullValue = encryptionFilter.toString();

		isFilterSetup = true;
	}

	/**
	 * This function gets the columnNames for SOURCE_B from the mapping config.
	 * Then it looks for the corresponding index in SOURCE_A.
	 * Using this index in A, figures out where it resides in this record's 
	 * values[]. In short, it finds (and tracks) the association between columns 
	 * in B, and this records values[].
	 */
	private static void setupWriteOrder()
	{
		writeOrder = new ArrayList<Integer>(length);

		// Iterate over B columnNames to determine the proper write order
		for (String s : mapConfig.getColumnNames(MappingConfig.SOURCE_B))
		{
			if (!s.equals(""))
			{
				// Find the mapped pair with the desired columnName
				MappedPair mp = mapConfig.getMappedPairByColumnName(s);

				// Get the index in A for the columnName in B
				int aIndex = mp.getIndexA();

				// Look up what index in this.values[] aIndex corresponds to
				int newIndex = A_indexToNewIndex.get(aIndex);

				// Put the newIndex into the writeOrder array
				writeOrder.add(newIndex);
			}
		}


		isWriteOrderSetup = true;
	}

}
