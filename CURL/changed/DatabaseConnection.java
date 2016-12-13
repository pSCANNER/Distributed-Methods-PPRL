import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class DatabaseConnection {
	private String table;
	private Connection conn = null;
	private Statement stmt;
	private ResultSet rs;
	public DatabaseConnection(String table, String URL, String driver, String user, String passwd){
		this.table = table;
		try{
			Class.forName(driver);
			conn = DriverManager.getConnection(URL, user,passwd);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Error Connecting to Database"); 
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Error Finding JDBC Driver"); 
			e.printStackTrace();
		}
	}
	public ResultSet readTable(){
		String entry;
		int count = 0;
		try {
			if (stmt.execute("SELECT * from " + table))
				rs = stmt.getResultSet();
			else
				System.err.println("select failed");
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Error Reading Database");
			e.printStackTrace();
		}
		return rs;
	}
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(new JFrame(), "Could not close connection"); 
			e.printStackTrace();
		}
	}
}
