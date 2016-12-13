package rosita.linkage.tests;

import rosita.linkage.DatabaseLinker;
import rosita.linkage.MappedPair;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.util.MappingConfig;

public class XML_DatabaseLinkerTest 
{
	public static void main(String[] args)
	{
		XML_Reader xmlr = new XML_Reader("cfg/linkage_config_frill.xml");

		DatabaseConnection aDBC = xmlr.getDatabaseConnection(XML_Reader.SOURCEA);
		String aTable = xmlr.getTableName(XML_Reader.SOURCEA);
		String[] aColumnNames = aDBC.getColumnNames(aTable);

		DatabaseConnection bDBC = xmlr.getDatabaseConnection(XML_Reader.SOURCEB);
		String bTable = xmlr.getTableName(XML_Reader.SOURCEB);
		String[] bColumnNames = bDBC.getColumnNames(bTable);

		MappedPair blocking = xmlr.getBlockingPair("source_a", "source_b");

		MappingConfig mapConfig = new MappingConfig(aTable, aColumnNames, bTable, bColumnNames,
				xmlr.getMappedPairs("source_a", "source_b"), xmlr.getBlockingPair("source_a", "source_b"));
		
		// -----------------------------------------------------------------
		// Above code copied from XML_LinkageTest
		// Code below written to motivate DatabaseLInker development
		// -----------------------------------------------------------------
		
		DatabaseLinker dbl = new DatabaseLinker(aDBC, bDBC, mapConfig);
		
		System.out.println("Success.");
		
	}
}
