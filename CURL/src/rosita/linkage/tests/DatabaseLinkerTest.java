package rosita.linkage.tests;

import java.util.ArrayList;

import rosita.linkage.DatabaseLinker;
import rosita.linkage.MappedPair;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.util.MappingConfig;

public class DatabaseLinkerTest 
{

	
	public static void main (String[] args)
	{
		XML_Reader xmlr = new XML_Reader("cfg/linkage_config.xml");
		
		int srcA = XML_Reader.SOURCEA;
		int srcB = XML_Reader.SOURCEB;
		
		DatabaseConnection sourceA = 
			xmlr.getDatabaseConnection(srcA);
		String tableA = xmlr.getTableName(srcA);
		String[] columnNamesA = sourceA.getColumnNames(tableA);
		
		DatabaseConnection sourceB = 
			xmlr.getDatabaseConnection(srcB);
		String tableB = xmlr.getTableName(srcB);
		String[] columnNamesB = sourceB.getColumnNames(tableB);
		
		System.out.println("table: " + tableA);
		for (String s : columnNamesA)
			System.out.print(s + ", ");
		System.out.println();

		System.out.println("---------");
		
		System.out.println("table: " + tableB);
		for (String s : columnNamesB)
			System.out.print(s + ", ");
		System.out.println();
		
		System.out.println("---------");
		
		
		ArrayList<MappedPair> mappedPairs = xmlr.getMappedPairs();
		for (MappedPair mp : mappedPairs) {
			System.out.println(mp);
		}
		
		MappingConfig mapConfig = new MappingConfig(tableA, columnNamesA, tableB, columnNamesB, mappedPairs);
		

		DatabaseLinker dbl = 
			new DatabaseLinker(sourceA, sourceB, mapConfig);
		

	
	}
	
}
