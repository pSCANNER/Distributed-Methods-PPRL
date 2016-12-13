package rosita.linkage.deprecated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rosita.linkage.filtering.BloomFilter;
import rosita.linkage.filtering.Cleaner;
import rosita.linkage.util.MyUtilities;

/**
 * Record class to keep track of Patient records.
 * Can handle patients with different sets (& number) of attributes
 * @author Brandon Abbott
 */
public class Record 
{
	// Maps to determine which values in attributes belong to which column
	// These should never be accessed directly; to access these members,
	//   use the getMap() function instead (even within this class)
	// Making these maps as non-static members requires too much memory
	public static HashMap<String, Integer> columnNameMap = null;

	// Since the purpose of this program is to compare two sets of records,
	// Keep another map for the linkedRecords
	private static HashMap<String, Integer> linkedColumnNameMap = null;

	// Keep track of the order in which we need to write values
	private static List<String> writeOrder = null;
	private static String writeOrderCSV = null;

	// The number of extra fields that are in "values"
	private static int extraFields = 0;
	private static int linkedExtraFields = 0;

	// This item is returned when a request is made for something in values, 
	// but it is not in the map.
	private static String notInMapValue = "";
	private static String encryptedNotInMapValue = null;

	// The encryptionFilter to use when encrypting Record values
	private static BloomFilter<String> encryptionFilter = null;
	private static int defaultBitSetSize = 100;
	private static int defaultExpectedNumberOElements = 11;

	// Also create an array of encryptionFilters to use for threadedEncryption()
	private static List<BloomFilter<String>> threadedEncrpytionFilters = null; 

	// The list of values belonging to the record 
	private String[] values = null;
	private String DOBHASH = null;

	// Dictates whether values have been encrypted
	boolean isEncrypted;

	// Dictates whether this records is to be used for linkage
	boolean isLinked;


	/** 
	 * Default constructor for Record should not be used.
	 */
	@SuppressWarnings("unused")
	private Record(){}


	/**
	 * Sets all of the the values of a record
	 * Convert null fields to ""
	 * @param values
	 */
	public Record (String values[], boolean isEncrypted, boolean isLinked)
	{	
		// Initialize the values, 
		this.isEncrypted = isEncrypted;
		this.isLinked = isLinked;
		this.values = new String[values.length];

		// TODO: perhaps throw an exception when length < 1

		// Convert null fields to ""
		for (int i = 0; i < values.length; i++)
			if (values[i].equals("null") || values[i] == null)
				this.values[i] = notInMapValue;
			else
				this.values[i] = values[i];

	}	


	/**
	 * Normalize data in "values" to some standardized form.
	 */
	public void cleanValues()
	{
		// Can't clean encrypted data!
		if (isEncrypted) return;

		String val;
		// TODO: We don't have a phone to parse yet.
		//	val = getValue(MyUtilities.PHONE);
		//	val = Cleaner.parsePhone(val);
		//	setValue(MyUtilities.PHONE, val);

		val = getValue(MyUtilities.DOB);
		//val = Cleaner.parseDate(val);
		val = Cleaner.normalizeDate(val);
		setValue(MyUtilities.DOB, val);

		val = getValue(MyUtilities.SSN);
		val = Cleaner.parseSSN(val);
		setValue(MyUtilities.SSN, val);

	}

	/**
	 * Using the Bloom Filter, encrypt all the values of this record.
	 * Also, create the DOBHASH.
	 */
	public void encryptValues()
	{
		// No need to encrypt if already encrypted
		if (isEncrypted) return;

		// Can't encrypt if the filter is not set up
		if (encryptionFilter == null) return;
		//TODO: It might be better to throw an exception here

		String s = "";

		// First, Create the DOBHASH
		String dob = getValue(MyUtilities.DOB);
		if (!dob.equals(""))
			s = dob.substring(0, 4);
		DOBHASH = MyUtilities.SHA1(s);

		// Iterate through all values that need to be written.
		for (String key : writeOrder)
		{
			// Only perform encryption on items for which we have data
			if (getHashMap().containsKey(key))
			{
				s = getValue(key);

				// No need to encrypt empty strings
				if ( s.equals(notInMapValue))
				{
					setValue(key, encryptedNotInMapValue);
				} else {
					encryptionFilter.clear();
					encryptionFilter.addAsBigrams(getValue(key));
					setValue(key, encryptionFilter.toString());
				}
			}
		}

		// Finally set this record's encryption state to true.
		isEncrypted = true;

	}

	/**
	 * Assuming the user is using threads to perform the encryption,
	 * Use one of the BloomFilters in threadedEncryptionFilters
	 * so that BloomFilters do not fill up with the wrong values.
	 * @param threadNum - The threadNumber handling this function, 
	 *   also used to access the correct BloomFilter.
	 */
	public void threadedEncryptValues(int threadNum)
	{
		// No need to encrypt, if already encrypted
		if (isEncrypted) return;

		// Ensure that the filters are initialized.
		//TODO: might be better to throw an un-initialized exception here.
		if (threadedEncrpytionFilters == null)
			return;

		// First, Create the DOBHASH
		String s = "";
		String dob = getValue(MyUtilities.DOB);
		if (!dob.equals(notInMapValue))
			s = dob.substring(0, 4);
		DOBHASH = MyUtilities.SHA1(s);

		// Iterate through all values in writeOrder
		for (String key : writeOrder)
		{
			// Only perform encryption on items for which we have data
			if (getHashMap().containsKey(key))
			{
				s = getValue(key);

				if (s.equals(notInMapValue))
				{
					// no need to encrypt empty strings
					setValue(key, encryptedNotInMapValue);
				} else {
					// Use the threadNum to determine which bloom filter to use.
					// That way, there won't be any overlap when running things in parallel.
					threadedEncrpytionFilters.get(threadNum).clear();
					threadedEncrpytionFilters.get(threadNum).addAsBigrams(getValue(key));
					setValue(key, threadedEncrpytionFilters.get(threadNum).toString());
				}
			}
		}

		// Finally set this record's encryption state to true.
		isEncrypted = true;

	}

	/**
	 * Return one of the values of this recored based on 
	 * the key.  Uses the appropriate HashMap to retrieve values
	 * @param key - The name of the attribute to retrieve
	 * @return The value of the attribute requested for this record
	 */
	public String getValue(String key)
	{
		HashMap<String, Integer> myMap = getHashMap();
		
		// If there is no column map, return null
		// TODO: it might be better to throw a null exception here
		if (myMap == null) return null;

		// If the key is the DOBHASH, & it's initialized, get it!
		if (key.equals(MyUtilities.DOBHASH) )
			if (this.DOBHASH != null)
				return this.DOBHASH;
			else if ( myMap.containsKey(MyUtilities.DOBHASH) )
				return values[myMap.get(key)];


		// If the key requested is not in the map, return the appropriate notInMapValue
		if (! myMap.containsKey(key))
		{
			if (isEncrypted)
				return encryptedNotInMapValue;
			else
				return notInMapValue;
		}

		// Otherwise, the key is in the map, just get the value 
		return values[myMap.get(key)];
	}

	/**
	 * Set one of the values of this record based on the key.
	 * Uses the HashMap to set the values
	 * @param key - The name of the attribute/value to set
	 * @param val - The new value to be set for this record
	 */
	public void setValue (String key, String val)
	{
		// If there is no column name map, do nothing
		// TODO: it could be better to throw an exception here
		if (getHashMap() == null) return;

		// If the key requested is not in the map, do nothing
		// TODO: it might be better to throw an exception of sorts here
		if (! getHashMap().containsKey(key)) return;

		values[getHashMap().get(key)] = val;

	}


	// ******************************************************************************
	// ** toString() Variants and Helper Functions
	// ******************************************************************************

	/**
	 * @return - A comma separated version of this Record's values.
	 * Only get those values that exist in "writeOrder"
	 */
	public String toString()
	{
		String s = "";

		// Don't even try if the write order is not yet set.
		// TODO: perhaps create a custom exception saying that writeOrder needs to be set first.
		if (writeOrder == null)
			return s;

		for (String key : writeOrder)
		{
			// If the value we're working with is the DOB, write the DOBHASH first
			if (key.equals(MyUtilities.DOB))
			{
				// Ensure hash exists
				if (isEncrypted && DOBHASH != null)
					s += DOBHASH + ",";
				// if we read in this record for linking, then the hash is in one of the values.
				else if (isEncrypted && isLinked)
					s += this.getValue(MyUtilities.DOBHASH) + ",";

			}

			// get all values that are in the ColumnNameReferences list
			// Put extra values at the end.
			if (! key.contains(MyUtilities.KEYWORD_EXTRA))
				s += getValue(key) + ",";
		}

		// Tag on the extra values.
		s += getExtra();

		return s;
	}

	/**
	 * @return - A comma separated version of this Record, with each element
	 * surrounded by quotes.
	 * Only get those values that are exist in "writeOrder"
	 */
	public String toQuotedString()
	{
		
		String s = "";
		for (int i = 0; i < writeOrder.size(); i++)
		{
			String key = writeOrder.get(i);

			// get all values that are in the ColumnNameReferences list
			s += "\"" + getValue(key) + "\",";
		}
		
		s += "\"" + getExtra() + "\"";

		return s;
	}

	// ******************************************************************************
	// **  Getters for Record members 
	// ******************************************************************************

	/**
	 * Gets the extra fields in this record's list of values.
	 * @return The string of values marked EXTRA in the map, delimited by a '|'
	 */
	public String getExtra()
	{
		if (extraFields == 0) return null;

		int e = extraFields;
		if (isLinked) e = linkedExtraFields;

		String s = "";
		for (int i = 0; i < e; i++)
		{
			// See MyUtilities.returnMap() to see why this is happening.
			s += getValue(MyUtilities.KEYWORD_EXTRA + i);
			if (i != e - 1)
				s += "|";
		}

		return s;
	}

	/**
	 * Get the HashMap for this set of Records
	 * This function should be used even within this class 
	 * instead of accessing the two HashMaps directly
	 * 
	 * @return - The columnNameMap or the linkedColumnNameMap, 
	 * depending on the type of record.
	 */
	public HashMap<String, Integer> getHashMap()
	{
		if (isLinked)
			return linkedColumnNameMap;

		return columnNameMap;
	}

	/**
	 * Getter for boolean isEncrpyted.
	 * @return - True if the record is in an encrypted form, False for clear-text.
	 */
	public boolean getIsEncrypted()
	{
		return this.isEncrypted;
	}

	/**
	 * Getter for boolean isLinked.
	 * @return - True if the record is a "linked" record. False otherwise
	 */
	public boolean getIsLinked()
	{
		return this.isLinked;
	}

	/**
	 * Getter for encryptedNotInMapValue
	 * @return - The value used for encrypted records when asking for keys not in the map.
	 */
	public static String getEncryptedNotInMapValue()
	{
		// TODO: maybe throw an un-initialized exception?
		return encryptedNotInMapValue;
	}

	/**
	 * Getter for the member writeOrder.
	 * See the Setter for a more detailed description.
	 * @return The writeOrder for this record.
	 */
	public static List<String> getWriteOrder()
	{
		return writeOrder;
	}

	/** 
	 * Getter for the member writeOrderCSV
	 * @return - A comma separated version of the writeOrder
	 */
	public static String getWriteOrderCSV()
	{
		return writeOrderCSV;
	}

	/**
	 * Determine if the encryption filters have been initialized.
	 * @return true if threadedEncryptionFilters are initialized, false otherwise.
	 */
	public static boolean getThreadedEncryptionFilters()
	{
		if (threadedEncrpytionFilters == null) 
			return false; 
		return true;
	}


	// ******************************************************************************
	// **  Setters for Record members 
	// ******************************************************************************

	/**
	 * Sets the encryption filter as a BloomFilter
	 * @param bitSetSize - The bitSetSize to use on the BloomFilter constructor
	 * @param expectedNumberOElements - The expectedNumberOElements to use on the BloomFilter constructor
	 */
	public static void setEncryptionFilter(int bitSetSize, int expectedNumberOElements)
	{
		encryptionFilter = new BloomFilter<String>(bitSetSize, expectedNumberOElements);
	}

	/**
	 * Overloads setter for encryptionFilter member. 
	 * @param bf - An initialized BloomFilter
	 */
	public static void setEncryptionFilter(BloomFilter<String> bf)
	{
		if (bf == null) return;
		encryptionFilter = bf;
	}

	/**
	 * Initialize the threadedEncrypytion Filters
	 * For now, just use the default values
	 */
	public static void setThreadedEncryptionFilters(int numThreads)
	{
		threadedEncrpytionFilters = new ArrayList<BloomFilter<String>>(numThreads);
		for (int i = 0; i < numThreads; i++)
			threadedEncrpytionFilters.add(new BloomFilter<String>(defaultBitSetSize, defaultExpectedNumberOElements));
	}

	/**
	 * This sets the encryptedNotInMap value.
	 * First checks to see if the encryptionFilter is available for use.
	 */
	public static void setEncryptedNotInMapValue()
	{

		if (encryptionFilter == null)
			setEncryptionFilter(defaultBitSetSize, defaultExpectedNumberOElements);
		encryptionFilter.clear();

		encryptedNotInMapValue = encryptionFilter.toString();
	}

	/**
	 * Given a HashMap of Strings (columnNames) mapped to Integers(indices)
	 * Set the columnNameMap for the "Records"
	 * Also compute the number of extraFields, along with the encryptedNotInMapValue
	 * @param columnNameMap - The initialized columnNameMap to set
	 */
	public static void setColumnNameMap(HashMap<String, Integer> columnNameMap) 
	{

		// Initialize the columnNameMap
		Record.columnNameMap = columnNameMap;

		// Get the extra count now that we have the keys
		extraFields = 0;
		for (String key : Record.columnNameMap.keySet())
		{
			if (key.startsWith(MyUtilities.KEYWORD_EXTRA))
				extraFields++;
		}

		// If need be, also set encryptedNotInMapValue
		if (encryptedNotInMapValue == null)
			setEncryptedNotInMapValue();

	}

	/**
	 * Given a HashMap of Strings (columnNames) mapped to Integers(indices)
	 * Set the linkedColumnNameMap for the "Records"
	 * Also compute the number of linkedExtraFields, 
	 * along with the encryptedNotInMapValue
	 * @param columnNameMap - The initialized columnNameMap to set
	 */
	public static void setLinkedColumnNameMap(HashMap<String, Integer> columnNameMap) 
	{

		// Initialize the columnNameMap
		Record.linkedColumnNameMap = columnNameMap;

		// Get the extra count now that we have the keys
		linkedExtraFields = 0;
		for (String key : Record.linkedColumnNameMap.keySet())
		{
			if (key.startsWith(MyUtilities.KEYWORD_EXTRA))
				linkedExtraFields++;
		}

		// If need be, also set encryptedNotInMapValue
		if (encryptedNotInMapValue == null)
			setEncryptedNotInMapValue();

	}

	/**
	 * Essentially matches the ETL columnNames to values.  Similar to returnMap, but
	 * Only the attributes that need to be encrypted are put into the ArrayList.
	 * Also creates comma-separated version, useful for MySQL 
	 * @param writeColumnNames
	 */
	public static void setWriteOrder(String[] writeColumnNames)
	{
		writeOrder = new ArrayList<String>();

		// Add items to the writeOrder array list
		for (String col: writeColumnNames)
		{
			// Handle Column Names for Person_Encrypted
			if ( col.equalsIgnoreCase("PE_Last") )
				writeOrder.add(MyUtilities.LASTNAME);
			else if (col.equalsIgnoreCase("PE_First") )
				writeOrder.add(MyUtilities.FIRSTNAME);
			else if (col.equalsIgnoreCase("PE_SSN") )
				writeOrder.add(MyUtilities.SSN);
			else if ( col.equalsIgnoreCase("PE_DOBHASH") )
				writeOrder.add(MyUtilities.DOBHASH);
			else if (col.equalsIgnoreCase("PE_GENDER_CODE"))
				writeOrder.add(MyUtilities.GENDER);
			else if (col.equalsIgnoreCase("PE_Address_1"))
				writeOrder.add(MyUtilities.ADDR_1);
			else if (col.equalsIgnoreCase("PE_City"))
				writeOrder.add(MyUtilities.CITY);
			
			// Handle Columns for filea_5000_encrypted
			else if (col.equalsIgnoreCase("SSN")) 
				writeOrder.add(MyUtilities.SSN);
			else if (col.equalsIgnoreCase("FIRSTNAME")) 
				writeOrder.add(MyUtilities.FIRSTNAME);
			else if (col.equalsIgnoreCase("LASTNAME")) 
				writeOrder.add(MyUtilities.LASTNAME);
			else if (col.equalsIgnoreCase("DOBHASH")) 
				writeOrder.add(MyUtilities.DOBHASH);
			else if (col.equalsIgnoreCase("SEX")) 
				writeOrder.add(MyUtilities.GENDER);
			else if (col.equalsIgnoreCase("ADDR")) 
				writeOrder.add(MyUtilities.ADDR_1);
			else if (col.equalsIgnoreCase("CITY")) 
				writeOrder.add(MyUtilities.CITY);
			else if (col.equalsIgnoreCase("STATE")) 
				writeOrder.add(MyUtilities.STATE);
			else if (col.equalsIgnoreCase("ZIP")) 
				writeOrder.add(MyUtilities.ZIP);
			else if (col.equalsIgnoreCase("PHONE")) 
				writeOrder.add(MyUtilities.PHONE);
			
		}

		// Also generate a comma separated version of the ArrayList
		StringBuilder myWriteOrderCSV = new StringBuilder();
		int n = writeOrder.size();
		for (int i = 0; i < n; i++)
		{
			myWriteOrderCSV.append(writeOrder.get(i));
			if ( i != n - 1 )
				myWriteOrderCSV.append(",");
		}
		writeOrderCSV = myWriteOrderCSV.toString();
	}

}
