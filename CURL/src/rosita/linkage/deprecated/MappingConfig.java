package rosita.linkage.deprecated;

import java.util.HashMap;

public class MappingConfig 
{
	private String[] columnNames;
	
	private HashMap<String, String> columnNamesToTags = null;
	private HashMap<String, String> tagsToColumnNames = null;
	private HashMap<String, Integer> tagsToIndeces = null;

	
	/**
	 * Class Constructor
	 * @param columnNames - An array of SQL table columnNames
	 */
	public MappingConfig(String[] columnNames)
	{
		this.columnNames = columnNames;

		setUpMaps(columnNames);
	}
	

	/**
	 * Getter for the columNamesToTags HashMap
	 * @return - HashMap<String, String> columnNamesToTags
	 */
	public HashMap<String, String> getMapColumnNamesToTags()
	{
		return columnNamesToTags; 
	}

	/**
	 * Getter for the tagsToColumnNames HashMap
	 * @return - HashMap<String, String> tagsToColunNames
	 */
	public HashMap<String, String> getMapTagsToColumnNames()
	{
		return tagsToColumnNames;

	}

	/**
	 * Getter for the tagsToIndeces HashMap
	 * @return - HashMap<String, Integer> tagsToIndeces
	 */
	public HashMap<String, Integer> getMapTagsToIndeces()
	{
		return tagsToIndeces;
	}
	
	
	/** 
	 * Get the index of some tag in the SQL column name array
	 * @param tag - The tag to look for
	 * @return - The index of of the tag in the column name array, -1 on error
	 */
	public int getIndexByTag(String tag)
	{
		if (tagsToIndeces.containsKey(tag))
			return tagsToIndeces.get(tag);
		
		System.err.println("Could find index for " + tag + ", returning -1 as index." );
		return -1;
	}
	
	/**
	 * Get the index of some column name in the SQL columnName array
	 * @param columnName - 
	 * @return
	 */
	public int getIndexByColumnName(String columnName)
	{
		if (columnNamesToTags.containsKey(columnName))
			return getIndexByTag(columnNamesToTags.get(columnName));
		
		System.err.println("Could find index for " + columnName + ", returning -1 as index." );
		return -1;
	}
	
	/**
	 * Get the SQL column name from some tag
	 * @param tag - The generalized "tag" to use in fetching the original columnName
	 * @return - The SQL column name.
	 */
	public String getColumnNameByTag(String tag)
	{
		if (tagsToColumnNames.containsKey(tag))
			return tagsToColumnNames.get(tag);
		
		System.err.println("Tag " + tag + " not found, returning empty string");
		return "";
	}
	

	/** 
	 * Given the columnNames of some SQL Table,
	 * Identify what each column name is storing.
	 * That is, a column containing SSN information may be named "PE_SSN," "SOCIAL," etc...
	 * This function aims to map columnNames to generalized "tags"
	 * @param columnNames - The columnNames of some SQL Table
	 */
	private void setUpMaps(String[] columnNames)
	{

		columnNamesToTags = new HashMap<String, String>();
		tagsToColumnNames = new HashMap<String, String>();
		tagsToIndeces = new HashMap<String, Integer>();	

		int extraCount = 0;

		for (int i = 0; i < columnNames.length; i++)
		{
			String col = columnNames[i];

			if (isSSN(col))
				addElementToMaps(col, "SSN", i);
			else if (isLastName(col))
				addElementToMaps(col, "LASTNAME", i);
			else if (isFirstName(col))
				addElementToMaps(col, "FIRSTNAME", i);
			else if (isDOB(col))
				addElementToMaps(col, "DOB", i);
			else if (isDOBHASH(col))
				addElementToMaps(col, "DOBHASH", i);
			else if (isSex(col))
				addElementToMaps(col, "SEX", i);
			else if (isAddr1(col))
				addElementToMaps(col, "ADDR_1", i);
			else if (isCity(col))
				addElementToMaps(col, "CITY", i);
			else if (isState(col))
				addElementToMaps(col, "STATE", i);
			else if (isZip(col))
				addElementToMaps(col, "ZIP", i);
			else if (isPhone(col)) 
				addElementToMaps(col, "PHONE", i);
			else
				// If the element was not identified, mark it as EXTRA__
				// Where ___ is how many extra fields have been encountered. 
				addElementToMaps(col, "EXTRA" + extraCount++, i);
		}
	}

	/**
	 * Once the tag for a columnName has been identified,
	 * create the association between the columnName, the tag, and the index of the columnName
	 * @param columnName - A column name of some SQL table
	 * @param tag - The tag associated with the column name
	 * @param index - The index of the columnName in the string array columnNames
	 */
	private void addElementToMaps(String columnName, String tag, Integer index)
	{
		columnNamesToTags.put(columnName, tag);
		tagsToColumnNames.put(tag, columnName);
		tagsToIndeces.put(tag, index);
	}

	private boolean isSSN(String columnName)
	{
		if (columnName.equalsIgnoreCase("Person_SSN") || 
				columnName.equalsIgnoreCase("SSN") || 
				columnName.equalsIgnoreCase("PE_SSN"))
			return true;
		return false;
	}

	private boolean isLastName(String columnName)
	{
		if (columnName.equalsIgnoreCase("LASTNAME") ||
				columnName.equalsIgnoreCase("PE_LAST") ||
				columnName.equalsIgnoreCase("LN") ||
				columnName.equalsIgnoreCase("Person_First"))
			return true;
		return false;
	}

	private boolean isFirstName(String columnName)
	{
		if (columnName.equalsIgnoreCase("Person_First") || 
				columnName.equalsIgnoreCase("FIRSTNAME") || 
				columnName.equalsIgnoreCase("PE_First") ||
				columnName.equalsIgnoreCase("FN"))
			return true;
		return false;
	}

	private boolean isDOB(String columnName)
	{
		if ( columnName.equalsIgnoreCase("Date_of_Birth") || 
				columnName.equalsIgnoreCase("DOB") ||
				columnName.equalsIgnoreCase("BDATE") ||
				columnName.equalsIgnoreCase("BIRTH_DATE"))
			return true;
		return false;
	}

	private boolean isDOBHASH(String columnName)
	{
		if (columnName.equalsIgnoreCase("DOBHASH"))
			return true;
		return false;
	}

	private boolean isSex(String columnName)
	{
		if ( columnName.equalsIgnoreCase("SOURCE_GENDER_CODE") || 
				columnName.equalsIgnoreCase("SEX") || 
				columnName.equalsIgnoreCase("PE_GENDER_CODE"))
			return true;
		return false;
	}

	private boolean isAddr1(String columnName)
	{
		if ( columnName.equalsIgnoreCase("Person_Address_1") || 
				columnName.equalsIgnoreCase("ADDR") || 
				columnName.equalsIgnoreCase("PE_Address_1") ||
				columnName.equalsIgnoreCase("ADD_LINE_1"))
			return true;
		return false;
	}

	private boolean isCity(String columnName)
	{
		if (columnName.equalsIgnoreCase("Person_City") || 
				columnName.equalsIgnoreCase("CITY") || 
				columnName.equalsIgnoreCase("PE_CITY"))
			return true;
		return false;
	}

	private boolean isState(String columnName)
	{
		if (columnName.equalsIgnoreCase("STATE"))
			return true;
		return false;
	}

	private boolean isZip(String columnName)
	{
		if (columnName.equalsIgnoreCase("ZIP"))
			return true;
		return false;
	}

	private boolean isPhone(String columnName)
	{
		if (columnName.equalsIgnoreCase("PHONE"))
			return true;
		return false;
	}

}



