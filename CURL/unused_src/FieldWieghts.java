/**
 * fieldWieghts.java
 * Purpose: Connect to a database of field weights
 * The field weights should be in a 10 column database
 * with two rows. The columns should be SSN, firstName,
 * lastName, DOB, gender, address, city, state, zip, phone
 * This values in the second row should be the matched values that
 * came from the FS algorithm. The values on the first row should 
 * be unmatched values that came from the same algorithm. This class 
 * is not Used in the Client half of this program and this class is 
 * not used at all as of version 2.0
 * 
 * @author Vijay Thurimella
 * @version 1.0 7/22/10
 */

import java.sql.*;
import java.util.*;

public class FieldWieghts {
	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private ArrayList<Integer> nm, m;
	
	//two local variables the nm and m
	//nm is an ArrayList of the firstRow, the non-match row
	//m is an ArrayList of the secondRow, the match row
	public FieldWieghts() throws Exception{
		nm = new ArrayList<Integer>();
		m = new ArrayList<Integer>();
		readTable();
	}
	//only a certain amount of connections can be made that is why
	//connecting to the database is done like this in this method
	private static Connection getConnection() throws Exception {
	    // load the Oracle JDBC Driver
	    Class.forName("org.gjt.mm.mysql.Driver");
	    // define database connection parameters
	    return DriverManager.getConnection("jdbc:mysql://10.34.36.153/vijay2", "user",
	        "password");
	}
	//method connects to the database and creates a new connection statment
	public void connect() throws Exception{
		conn = getConnection();
		stmt = conn.createStatement();
	}
	//reads value from the table. This method assumes that the creation of this database 
	//created such that the first row consists of unmatched field values
	//and the second row consists of matched field values
	public void readTable() throws Exception{
		connect();
		String entry;
		int count = 0;
		if (stmt.execute("SELECT * from fieldweights"))
			rs = stmt.getResultSet();
        else
        	System.err.println("select failed");
       /*while(rs.next()) {
        	for(int i = 1; i < 11; i++){
        		entry = rs.getString(i);
        		if(count == 0)
        			nm.add(new Integer(entry));
        		else
        			m.add(new Integer(entry));
        	}
        	count = 1;
        }*/
	}
	//return non-match value of a given field
	public int nonMatch(int i){
		return nm.get(i).intValue();
	}
	//returns match value of given field
	public int match(int i){
		return m.get(i).intValue();
	}
}