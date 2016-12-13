/* Class has been
 * @Deprecated
 * */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 * Class created to get the information of a LinkDataSource through
 * a GUI, being either a database or flat file
 * 
 * @author jegg
 * @modified Vijay Thurimella
 * @version 2.0 8/31/10
 *
 */

public class LinkDataSourceChooser implements ActionListener{
	
	// create this to return in chooseLinkDataSource()
	
	JTabbedPane tabs; //tabs to choose from
	JTextField file, delim, table, url, user, driver;// text fields
	JPasswordField passwd;//password for the database tab
	JButton ok, cancel, choose_file;//Buttons on the GUI
	JDialog dialog;
	
	// variables to save for creating the return object
	String name;
	String type;
	String access;
	int id;
	/**
	 * Constructor that shows the Window
	 */
	public LinkDataSourceChooser(){
		showDialog();
	}
	/**
	 * Creates the TabbedPane
	 * @return two tabs "File" and "DB"
	 */
	private JTabbedPane getTabs(){
		tabs = new JTabbedPane();
		JPanel file = getDelimFilePanel();
		JPanel db = getDBPanel();
		
		tabs.addTab("File Datasource", file);
		tabs.addTab("DB Datasource", db);
		
		return tabs;
	}
	/**
	 * Creates the Database Panel for the Database input
	 * @return the DB Panel
	 */
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
	/**
	 * Creates the File Panel for the File input
	 * @return the File Panel
	 */
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
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
	}
	
	/**
	 * Checks to see if the user inputted a File or a 
	 * Database. All of the File Processing is done in this method.
	 * This could be split up for cleaner code. 
	 * tabs.getSelectedIndex() == 0 does a file input and
	 * the else clauses does a database input
	 * 
	 * @deprecated
	 
	public void actionPerformed(ActionEvent ae){
		RunDatasource rd = new RunDatasource();
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
						rd.setDatasource(true);
						rd.runFile(file.getText(), delim.getText().charAt(0));
				}
				else {
						rd.setDatasource(false);
						rd.runDB(table.getText(), url.getText(), driver.getText(), 
								user.getText(), new String(passwd.getPassword()));
				}
			} else {
				System.exit(0);
			}
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
	*/
	
	/**
	 * Shows the Gui
	 */
	public void showDialog(){
		dialog = getDialog();
		dialog.setVisible(true);
	}
	/**
	 * Reverses string to remove file extension
	 * @param source string to reverse
	 * @return reverse string
	 */
	public String reverse(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);

		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}
	/**
	 * a Dialog Box
	 * @return a the new Dialog Box 
	 */
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
	/**
	 * main method that runs the Client half of the program completely. 
	 * 
	 * @param args not used in this Program
	 */
	@SuppressWarnings("unused")
	public static void main(String args[] ){
		LinkDataSourceChooser ldsc = new LinkDataSourceChooser();
	}


//		  public File[] splitFile(String fileNameString) throws Exception {
//		    FileInputStream fis = new FileInputStream(args[0]);
//		    int size = 1024;
//		    byte buffer[] = new byte[size];
//
//		    int count = 0;
//		    while (true) {
//		      int i = fis.read(buffer, 0, size);
//		      if (i == -1)
//		        break;
//
//		      String filename = fileNameString + count;
//		      FileOutputStream fos = new FileOutputStream(filename);
//		      fos.write(buffer, 0, i);
//		      fos.flush();
//		      fos.close();
//
//		      ++count;
//		    }
//		  }

}
