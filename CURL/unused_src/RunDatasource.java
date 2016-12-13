/* 
 * @deprecated
 *

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class RunDatasource {
	//true=file and false=database
	public boolean dataSource;
	private ArrayList<Patient> pData;
	private ArrayList<EncryptedPatient> encryptedData;
	public RunDatasource(){
		dataSource = false;
		pData = new ArrayList<Patient>();
		encryptedData = new ArrayList<EncryptedPatient>();
		
	}
	public RunDatasource(boolean setSource){
		dataSource = setSource;
		pData = new ArrayList<Patient>();
		encryptedData = new ArrayList<EncryptedPatient>();
	}
	public void setDatasource(boolean setSource){
		dataSource = setSource;
	}
	public void runDB(String table, String url, String driver, String user, String password){
		if(!dataSource){
			try {
				FileWriter fstream = new FileWriter("encrypted_data");
				BufferedWriter out = new BufferedWriter(fstream);
				pData = new ArrayList<Patient>();
				ArrayList<String> columnNameList = new ArrayList<String>();
				
				Patient temp = null;
				ResultSet rs;
				int rowSize = 0;
				ResultSetMetaData rsmd;
				DatabaseConnection dbc = new DatabaseConnection(table,
						url, driver, user,password);
				rs = dbc.readTable();
				while(rs.next()){
					rowSize++;
				}
				
				rs.first();
				rsmd = rs.getMetaData();
				int columnSize = rsmd.getColumnCount();
				String[] columnName = new String[columnSize];
				for(int i = 1; i <= columnSize; i++){
					columnName[i-1] = rsmd.getColumnName(i);
				}
				
				Organizer o = new Organizer();
				columnNameList = o.toArrayList(columnName);
				o.setActualOrder(columnNameList);
				for(int i = 0; i < (int)Math.ceil(rowSize / 35000.0); i++){
					for(int j = 1; j <= rowSize; j++){
						temp = new Patient();
						for(int k = 1; k <= columnSize; k++){
							temp.setInstance(o.getMap(columnNameList.get(k-1)), rs.getString(columnName[k-1]));
						}
						pData.add(temp);
						rs.next();
					}
					for(int j = 0; j < pData.size(); j++){
						encryptedData.add(pData.get(j).toEncryptPatient());
					}
					pData.clear();
					while(encryptedData.size() != 0)
						out.write(encryptedData.remove(0) + "\n");
				}
				out.close();
				dbc.close();
				System.exit(0);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Error accessing Result Set");
				e.printStackTrace();
			}
		}
	}
	public void runFile(String fileName, char delim){
		File file = new File(fileName);
		if(dataSource){
			try {
				int lineCount = 0;
				Date d1 = new Date();
				String save =  fileName;
				save = reverse(save);
				save = save.substring(save.indexOf(".") + 1);
				save = reverse(save);
				FileWriter fstream = new FileWriter(save + "_encrypted");
				BufferedWriter out = new BufferedWriter(fstream);

				Scanner in = new Scanner(file);
				CSVParser csv = new CSVParser();
				csv.setFile(fileName);
				csv.setSeperator(delim);
				while(in.hasNextLine()){
					lineCount++;
					in.nextLine();
				}

				for(int i = 0; i < (int)Math.ceil(lineCount / 35000.0); i++){
					pData = csv.parseFile();
					for(int j = 0; j < pData.size(); j++){
						encryptedData.add(pData.get(j).toEncryptPatient());
					}
					pData.clear();
					while(encryptedData.size() != 0){
						out.write(encryptedData.remove(0) + "\n");
					}
				}

				out.close();
				Date d2 = new Date();
				System.out.println("Finished in " + subtractTime(d1, d2));
				//JOptionPane.showMessageDialog(new JFrame(), "Finished in " + subtractTime(d1, d2));
				System.exit(0);
			}catch (Exception e) {
				JOptionPane.showMessageDialog(new JFrame(), "Errror Reading File");
				e.printStackTrace();

			}
		}
	}

	public String reverse(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);

		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}

	private String subtractTime(Date d1, Date d2){
		int hours = d2.getHours() - d1.getHours();
		int mins = d2.getMinutes() - d1.getMinutes();
		int secs = d2.getSeconds() - d1.getSeconds();
		return hours + ":" + mins + ":" + secs;
	}
}

*/
