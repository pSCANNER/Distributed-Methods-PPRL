import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import javax.swing.*;


/**
 * Class created to get the information of a LinkDataSource through
 * a GUI, being either a database or flat file
 * 
 * @author jegg
 *
 */

public class LinkDataSourceChooser implements ActionListener{
	
	// create this to return in chooseLinkDataSource()
	
	JTabbedPane tabs;
	JTextField file, delim, table, url, user, driver, save;
	JPasswordField passwd;
	JButton ok, cancel, choose_file;
	JDialog dialog;
	
	// variables to save for creating the return object
	String name;
	String type;
	String access;
	int id;
	
	public LinkDataSourceChooser(){
		showDialog();
	}
	
	private JTabbedPane getTabs(){
		tabs = new JTabbedPane();
		JPanel file = getDelimFilePanel();
		JPanel db = getDBPanel();
		
		tabs.addTab("File Datasource", file);
		tabs.addTab("DB Datasource", db);
		
		return tabs;
	}
	
	private JPanel getDBPanel(){
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.PAGE_AXIS));
		
		JPanel line;
		
		line = new JPanel();
		line.add(new JLabel("Database table name:"));
		table = new JTextField(15);
		line.add(table);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Database URL:"));
		url = new JTextField(15);
		url.setText("jdbc:mysql://");
		line.add(url);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("JDBC Driver:"));
		driver = new JTextField(15);
		driver.setText("org.gjt.mm.mysql.Driver");
		line.add(driver);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("User:"));
		user = new JTextField(15);
		user.setText("root");
		line.add(user);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Password:"));
		passwd = new JPasswordField(15);
		passwd.setEchoChar('*');
		line.add(passwd);
		ret.add(line);
		
		return ret;
	}
	
	private JPanel getDelimFilePanel(){
		JPanel ret = new JPanel();
		
		JPanel line;
		
		line = new JPanel();
		line.add(new JLabel("File:"));
		file = new JTextField(15);
		line.add(file);
		choose_file = new JButton("Choose file");
		choose_file.addActionListener(this);
		line.add(choose_file);
		ret.add(line);
		
		line = new JPanel();
		line.add(new JLabel("Delimiter:"));
		delim = new JTextField(3);
		delim.setText("|");
		line.add(delim);
		ret.add(line);
				
		return ret;
	}
	public void actionPerformed(ActionEvent ae){
		FileWriter fstream;
		BufferedWriter out;
		ArrayList<Patient> pData = new ArrayList<Patient>();
		ArrayList<EncryptedPatient> encryptedData = new ArrayList<EncryptedPatient>();
		if(ae.getSource() == choose_file){
			JFileChooser jfc = new JFileChooser();
			int ret = jfc.showOpenDialog(null);
			if(ret == JFileChooser.APPROVE_OPTION){
				File f = jfc.getSelectedFile();
				file.setText(f.getPath());
			}
		} else { 
			if(ae.getSource() == ok){
				if(tabs.getSelectedIndex() == 0){
					try {
						String save = file.getText();
						save = reverse(save);
						save = save.substring(save.indexOf(".") + 1);
						save = reverse(save);
						fstream = new FileWriter(save + "_encrypted");
						out = new BufferedWriter(fstream);
						CSVParser csv = new CSVParser();
						csv.setFile(file.getText());
						csv.setSeperator(delim.getText().charAt(0));
						pData = csv.parseFile();
						for(int j = 0; j < pData.size(); j++){
							encryptedData.add(pData.get(j).toEncryptPatient());
						}
						pData.clear();
						while(encryptedData.size() != 0){
							out.write(encryptedData.get(0) + "\n");
							encryptedData.remove(0);
						}
						out.close();
						System.exit(0);
					}catch (Exception e) {
						JOptionPane.showMessageDialog(new JFrame(), "No File in input");
						e.printStackTrace();
					}
				}
				else {
					try {
						fstream = new FileWriter("encrypted_data");
						out = new BufferedWriter(fstream);
						pData = new ArrayList<Patient>();
						List<String> columnName = new ArrayList<String>();
						
						Patient temp = null;
						ResultSet rs;
						int rowSize = 0;
						ResultSetMetaData rsmd;
						DatabaseConnection dbc = new DatabaseConnection(table.getText(),
								url.getText(), driver.getText(), user.getText(),
								new String(passwd.getPassword()));
						rs = dbc.readTable();
						while(rs.next()){
							rowSize++;
						}
						rs.first();
						rsmd = rs.getMetaData();
						int columnSize = rsmd.getColumnCount();
						for(int i = 1; i <= columnSize; i++){
							columnName.add(rsmd.getColumnName(i));
						}
						for(int j = 1; j <= rowSize; j++){
							temp = new Patient();
							for(int k = 1; k <= columnSize; k++){
								temp.setInstance(k-1, rs.getString(columnName.get(k-1)));
							}
							pData.add(temp);
							rs.next();
						}
						for(int j = 0; j < pData.size(); j++){
							encryptedData.add(pData.get(j).toEncryptPatient());
						}
						pData.clear();
						while(encryptedData.size() != 0){
							out.write(encryptedData.get(0) + "\n");
							encryptedData.remove(0);
						}
						out.close();
						dbc.close();
						System.exit(0);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(new JFrame(), "Eroor accessing Result Set");
						e.printStackTrace();
					}
				}
			} else {
				System.exit(0);
			}
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
	
	public void showDialog(){
		dialog = getDialog();
		dialog.setVisible(true);
	}
	public String reverse(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);

		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}
	private JDialog getDialog(){
		Frame f = null;
		JDialog dialog = new JDialog(f, "Define datasource", true);
		Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(getTabs(), BorderLayout.CENTER);
        
		ok = new JButton("Ok");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel bottom_panel = new JPanel();
		bottom_panel.add(ok);
		bottom_panel.add(cancel);
		
		contentPane.add(bottom_panel, BorderLayout.SOUTH);
        
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(null);
        
		return dialog;
	}
	public static void main(String args[] ){
		LinkDataSourceChooser ldsc = new LinkDataSourceChooser();
	}
}
