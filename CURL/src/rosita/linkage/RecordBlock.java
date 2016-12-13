package rosita.linkage;

import java.util.ArrayList;
import java.util.List;

import rosita.linkage.filtering.E_Record;
import rosita.linkage.filtering.ThreadHelper;

public class RecordBlock 
{
	private List<E_Record> recordBlock = null;
	private int capacity;
	private int totalProcessed = 0;	

	
	/**
	 * Class Constructor.  Nothing needs to be done for the default case.
	 */
	public RecordBlock()
	{
		
	}
	
	/**
	 * Class Constructor. 
	 * @param capacity - Let's the record block know ahead of time how many records to expect.
	 */
	public RecordBlock(int capacity)
	{
		this.capacity = capacity;
	}
	

	/**
	 * Add a record to the block.  Appends it to an ArrayList
	 * @param r - The record to add to the block.
	 */
	public void loadRecord(E_Record r)
	{
		if (recordBlock == null)
			recordBlock = new ArrayList<E_Record>(capacity);

		recordBlock.add(r);
	}

	/**
	 * The default case for processRecords(clean, encrypt)
	 * Calling this function will do both.
	 */
	public void processRecords()
	{
		
		processRecords(true, true);
	}

	/**
	 * Process all the records in this block using the ThreadHelper.
	 * @param clean - Flag to determine if records will be cleaned
	 * @param encrypt - Flag to determine if records should be encrypted.
	 */
	public void processRecords(boolean clean, boolean encrypt)
	{
		// Clean & Encrypt the records.public String getValue(String key)
		// Since recordBlock contains references the records,
		// We can pass the reference to recordBlock by value
		ThreadHelper.processRecords(recordBlock, clean, encrypt);

		totalProcessed += recordBlock.size(); 
	}

	/**
	 * Empty this record block of values.  Clears & nullfies the ArrayList.
	 */
	public void clear()
	{
		if (recordBlock != null) {
			recordBlock.clear();
			recordBlock = null;
		}
	}

	/**
	 * Build a string composed of all the records ready for the VALUES
	 * section of a MySQL insert statement
	 * @return - A string of all the records in (attr1, attr2, attr3, ... ),\n format
	 */
	public String getRecordsAsMySQLTuples()
	{
		StringBuilder s = new StringBuilder();
		E_Record r;
		while (recordBlock.size() != 0)
		{
			r = recordBlock.remove(0);
			s.append( "(" + r.toQuotedString() + ")" );

			if (recordBlock.size() == 0)
				s.append("\n");
			else 
				s.append(",\n");
		}
		
		return s.toString();
	}
	
	/**
	 * Build a string composed of all the records ready for the VALUES
	 * section of a Postgres insert statement
	 * @return - A string of all the records in (attr1, attr2, attr3, ... ),\n format
	 */
	public String getRecordsAsPostgresTuples()
	{
		StringBuilder s = new StringBuilder();
		E_Record r;
		while (recordBlock.size() != 0)
		{
			r = recordBlock.remove(0);
			s.append( "(" + r.toSingleQuotedString() + ")" );

			if (recordBlock.size() == 0)
				s.append("\n");
			else 
				s.append(",\n");
		}
		
		return s.toString();
	}

	/**
	 * Getter for the total number of records procesed by this block
	 * @return - total number of records procssed by this block so far.
	 */
	public int getTotalProcessed()
	{
		return totalProcessed;
	}

	/**
	 * Getter for the number of records currently in this block.
	 * @return - The number of records in the recordBlock array list
	 */
	public int getNumRecords()
	{
		if (recordBlock == null)
			return 0;
		return recordBlock.size();
	}

}