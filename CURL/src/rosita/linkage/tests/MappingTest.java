package rosita.linkage.tests;

import java.util.HashMap;
import java.util.Properties;

import rosita.linkage.main;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.util.MyUtilities;

public class MappingTest 
{
	public static void main(String[] args)
	{
		String encryption_config = "cfg/encryption.properties";
		
		Properties p = MyUtilities.readProperties(encryption_config);
		String r_url = p.getProperty("R_URL");
		String r_database = p.getProperty("R_DATABASE");
		String r_table = p.getProperty("R_TABLE");
		String r_user = p.getProperty("R_USER");
		String r_password = p.getProperty("R_PASSWORD");
		
		DatabaseConnection readDBC = new DatabaseConnection(r_url, 
				main.mysqlDriver, r_database, r_user, r_password);
		
		String[] colNames = readDBC.getColumnNames(r_table);
		
		System.out.println("Column Names:");
		for (String s: colNames)
			System.out.print(s + ", ");
		System.out.println();
		
		HashMap<String, Integer> hm = MyUtilities.returnMap(colNames);

		
		for (int i = 0; i < colNames.length; i++)
		{
			
			String s = "" + i;
			s += "\t" + colNames[i];
			
			for (String key : hm.keySet())
				if (hm.get(key) == i)
					s += "\t" + key;
						
			System.out.println(s);
		}
		
	}
}
