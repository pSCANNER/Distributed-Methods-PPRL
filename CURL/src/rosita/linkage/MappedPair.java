package rosita.linkage;

import rosita.linkage.analysis.Algorithm;
import rosita.linkage.util.MyUtilities;

public class MappedPair 
{
	private String colA;
	private String colB;
	
	private int indexA;
	private int indexB;
	
	private String dateFormatA = null;
	private String dateFormatB = null;
	
	private double weight;
	
	public String encryptionType;
	
	private String attribute;
	
	private Algorithm algorithm = null;
		
	/**
	 * Class constructor
	 * @param colA - The name of the first SQL column.
	 * @param colB - The name of the second SQL column.
	 * @param attribute - This column's attribute, e.g. SSN, PHONE, etc..
	 */
	public MappedPair(String colA, String colB, 
			String attribute)
	{
		this.colA = colA;
		this.colB = colB;
		this.attribute = attribute;
		
		// Set indexA & indexB to -1 to signify that they have not yet been initialized
		// 0 may be a valid index.
		indexA = -1;
		indexB = -1;
	}
	
	public MappedPair(String colA, String colB, String parDateFormatA, String parDateFormatB,
			String attribute, String algorithm, double weight, String encryptiontype)
	{
		this.colA = colA;
		this.colB = colB;
		
		this.dateFormatA = parDateFormatA;
		this.dateFormatB = parDateFormatB;
		this.attribute = attribute;
		
		
		if (algorithm.equalsIgnoreCase("PPRL")) this.algorithm = Algorithm.PPRL;
		else if (algorithm.equalsIgnoreCase("EQUAL_FIELDS_BOOLEAN_DISTANCE")) this.algorithm = Algorithm.EQUAL_FIELDS_BOOLEAN_DISTANCE;
		else if (algorithm.equalsIgnoreCase("NONE")) this.algorithm = Algorithm.NONE;
		else if (algorithm.equalsIgnoreCase("EDIT_DISTANCE")) this.algorithm = Algorithm.EDIT_DISTANCE;
		else if (algorithm.equalsIgnoreCase("JARO_WINKLER")) this.algorithm = Algorithm.JARO_WINKLER;
		else if (algorithm.equalsIgnoreCase("LEV_DISTANCE")) this.algorithm = Algorithm.LEV_DISTANCE;
		else if (algorithm.equalsIgnoreCase("ADDRESS_DISTANCE")) this.algorithm = Algorithm.ADDRESS_DISTANCE;
		else if (algorithm.equalsIgnoreCase("DATE_DISTANCE")) this.algorithm = Algorithm.DATE_DISTANCE;
		else if (algorithm.equalsIgnoreCase("NUMERIC_DISTANCE")) this.algorithm = Algorithm.NUMERIC_DISTANCE;
		else if (algorithm.equalsIgnoreCase("QGRAM_DISTANCE")) this.algorithm = Algorithm.QGRAM_DISTANCE;
		else if (algorithm.equalsIgnoreCase("SOUNDEX_DISTANCE")) this.algorithm = Algorithm.SOUNDEX_DISTANCE;
		else{
			MyUtilities.end("Distance measure not supported. Map pair with error: Left - "+this.colA+", Right - "+this.colB);
		}
		
		this.weight = weight;
		
		this.encryptionType = encryptiontype;
		// Set indexA & indexB to -1 to signify that they have not yet been initialized
		// 0 may be a valid index.
		indexA = -1;
		indexB = -1;
	}
	
	public MappedPair(int indexA, int indexB, Algorithm algorithm)
	{
		this.indexA = indexA;
		this.indexB = indexB;
		this.algorithm = algorithm;
		this.attribute = "";
	}
	
	public MappedPair(String colA, int indexA, String colB, int indexB, Algorithm algorithm)
	{
		this.colA = colA;
		this.colB = colB;
		this.indexA = indexA;
		this.indexB = indexB;
		this.algorithm = algorithm;
		this.attribute = "";
	}
	
	/**
	 * Convenient override for toString().  
	 */
	public String toString()
	{
		String s = "A[" + indexA + "]: " + colA + "\tB[" + indexB + 
		"]:" + colB +"\tattr:" + attribute;
		
		if (algorithm == null) return s;
		
		return s + "\talgorithm: " + algorithm;
		
	}
	
	
	// ******************************************************************************
	// **  Getters for MappedPair members
	// ******************************************************************************

	public Algorithm getAlgorithm()
	{
		return algorithm;
	}
	
	public double getWeight()
	{
		return weight;
	}
	
	public String getAttribute()
	{
		return attribute;
	}
	
	public String getColA()
	{
		return colA;
	}
	
	public String getColB()
	{
		return colB;
	}
	
	public String getDateFormatA()
	{
		return dateFormatA;
	}
	
	public String getDateFormatB()
	{
		return dateFormatB;
	}
	
	public int getIndexA()	
	{
		return indexA;
	}
	
	public int getIndexB()
	{
		return indexB;
	}
		
	// ******************************************************************************
	// **  Setters for MappedPair members
	// ******************************************************************************
	
	public void setIndices(int indexA, int indexB)
	{
		this.indexA = indexA;
		this.indexB = indexB;
	}
	
	public void setIndexA(int n)
	{
		indexA = n;
	}

	public void setIndexB(int n)
	{
		indexB = n;
	}
			
}
