package rosita.linkage.tests;

import rosita.linkage.MappedPair;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.util.MappingConfig;

public class XML_LinkageTest 
{
	
	public static void main(String args[])
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
		
		
/*
		System.out.println(bDBC);
		System.out.println(aDBC);

		System.out.println("Table A:" + aTable);
		System.out.println("Table B:" + bTable);

		ArrayList<MappedPair> mappedPairs = xmlr.getMappedPairs("source_a", "source_b");
		for (MappedPair mp : mappedPairs)
			System.out.println(mp);

		System.out.println(blocking);

*/
		
	}

}
