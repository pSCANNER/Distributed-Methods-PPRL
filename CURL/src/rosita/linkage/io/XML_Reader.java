package rosita.linkage.io;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rosita.linkage.MappedPair;
import rosita.linkage.main;
import rosita.linkage.analysis.DBMS;
import rosita.linkage.analysis.SamplingMethod;
import rosita.linkage.tools.JoinMethod;
import rosita.linkage.tools.WeightIdentifier;
import rosita.linkage.util.MyUtilities;


public class XML_Reader 
{
	// TODO: Consider changing to an enumeration
	public static final int READER = 0;
	public static final int WRITER = 1;
	public static final int SOURCEA = 2;
	public static final int SOURCEB = 3;
	public static final int SAVER = 4;

	private Document doc = null;

	/**
	 * Class Constructor.
	 * Attempt to create a normalize an XML document from file.
	 * @param filename - The path to the xml file.
	 */
	public XML_Reader(String filename)
	{
		try {
			File xmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
		} catch (Exception e){
			e.printStackTrace();
			MyUtilities.end("Program Terminated.");
		}

	}
	
	/**
	 * 
	 * @return The the acceptance level
	 * 
	 */
	
	public String getAcceptanceLevel(){
		
		String result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			result = getTagValue("acceptancelevel", e);
		}
		return result;
	}
	
	/**
	 * 
	 * @return The the PPRL Threshold
	 * 
	 */
	
	public String getPPRLThreshold(){
		
		String result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			result = getTagValue("pprlthreshold", e);
		}
		return result;
	}
	
	/**
	 * 
	 * @return The the PPRL Threshold
	 * 
	 */
	
	public String getRandomSampleSize(){
		
		String result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			result = getTagValue("emrandomsamplesize", e);
		}
		return result;
	}
	
	/**
	 * 
	 * @return The Bloom filter size
	 * 
	 */
	
	public String getBloomFilterSize(){
		
		String result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			result = getTagValue("bloom_filter_size", e);
		}
		return result;
	}
	
	/**
	 * 
	 * @return The sorting order of the sorted neighbor method
	 * 
	 */
	
	public String[] getSNMKeys(){
		
		String[] result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			String strResult = getTagValue("snmkeys", e);
			result = strResult.split("[|]");
		}
		return result;
	}
	
	/**
	 * 
	 * @return The window size of the sorted neighbor method
	 * 
	 */
	
	public int getSNMWindowsSize(){
		
		int result = -1;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			String strResult = getTagValue("snmwindowsize", e);
			result = Integer.parseInt(strResult);
		}
		return result;
	}
	
	
	/**
	 * 
	 * @return The blocking method 
	 * 
	 */
	
	public JoinMethod getJoinMethod(){
		JoinMethod result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			String strResult = getTagValue("joinmethod", e);
			if(strResult.equals("NESTED_LOOP_JOIN")){
				result = JoinMethod.NESTED_LOOP_JOIN;
			}else if(strResult.toUpperCase().equals("SORTED_NEIGHBOURHOOD")){
				result = JoinMethod.SORTED_NEIGHBOURHOOD;
			}else if(strResult.toUpperCase().equals("BLOCKING_SEARCH")){
				result = JoinMethod.BLOCKING_SEARCH;
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return The method used to identify the weights of the fields. 
	 * There are two weight identifiers: EM (Expectation Maximization) and Manual
	 */
	
	public WeightIdentifier getWeightIdentifier(){
		WeightIdentifier result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			String strResult = getTagValue("weightidenfifier", e);
			if(strResult.equals("EM")){
				result = WeightIdentifier.EM;
			}else if(strResult.toUpperCase().equals("MANUAL")){
				result = WeightIdentifier.MANUAL;
			}
		}
		return result;
	}
	
	/**
	 * This methods returns the Sampling method. If there is no sampling method specified, this method will return the All method
	 * @return The method used to identify the weights of the fields. 
	 * There are two weight identifiers: EM (Expectation Maximization) and Manual
	 */
	
	public SamplingMethod getSamplingMethod(){
		SamplingMethod result = new SamplingMethod(SamplingMethod.ALL, -1);
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;	
			String strResult = getTagValue("samplingmethod", e);
			if(strResult!=null){
				if(strResult.toUpperCase().trim().startsWith("TOP")){
					int N = extractN(strResult);
					result = new SamplingMethod(SamplingMethod.TOP, N);
				}else if(strResult.toUpperCase().startsWith("RANDOM")){
					int N = extractN(strResult);
					if(N<1 || N>100){
						MyUtilities.end("Random sample: value of N must be between 1% and 100%");
					}else{
						result = new SamplingMethod(SamplingMethod.RANDOM, N);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * This method extracts N from the sampling method string
	 * @return N
	 * 
	 */
	
	private int extractN (String parStr){
		int result = -1;
		String tempStr = parStr.trim();
		String strNumber = null; 
		//remove the all the double-space in the string
		while(tempStr.contains("  ")){
			tempStr= tempStr.replace("  ", " ");
		}
		
		if(tempStr.contains(" ")){
			strNumber = tempStr.split(" ")[1];
			try{
				result = Integer.parseInt(strNumber);
			}catch(Exception ex){
				
			}
		}
		
		if(result == -1){
			MyUtilities.end(parStr+" is an invalid value of a sampling method.");
		}
		
		return result;
	}
	
	/**
	 * Max CPU
	 * 	Maximum number of CPUs utilized by the tool
	 * @return max number of CPUs
	 * 
	 */
	
	public String getMaxCPU(){
		String result = "999";
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;
			String strResult = getTagValue("max_cpu", e);
			try{
				int testresult = Integer.parseInt(strResult);
				result = strResult;
			}catch(NumberFormatException ex){
			}
		}
		return result;
	}
	
	/**
	 * Missing data value
	 * 	- Number: 0-100 -> matching score
	 *  - IGNORE: use attribute-deduction method
	 *  - BACKUP: use backup quasi-identifier method
	 * @return The method used to identify the method to handle missing value
	 * 
	 */
	
	public String getMissingValue(){
		String result = null;
		
		NodeList nl = doc.getElementsByTagName("LinkageConfig");
		Node n = null;
		
		if(nl.getLength()>0){
			n = nl.item(0);
			Element e = (Element) n;
			String strResult = getTagValue("missingdatavalue", e);
			result = strResult;
		}
		return result;
	}
	
	/**
	 * 
	 * @param databaseType -  One of the types from the enumeration
	 * @return the password
	 * @author TOAN
	 */
	
	public String getPassword(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("password", e);

		return result;
	}
	
	/**
	 * 
	 * @param databaseType -  One of the types from the enumeration
	 * @return the user
	 * @author TOAN
	 */
	
	public String getUser(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("user", e);

		return result;
	}

	/**
	 * 
	 * @param databaseType -  One of the types from the enumeration
	 * @return the DSN Name
	 * @author TOAN
	 */
	
	public String getDSNName(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = null;

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("dsnname", e);

		return result;
	}
	
	/**
	 * Get the URL of the database 
	 * @param databaseType - One of the types from the enumeration
	 * @return - The URL
	 * @author TOAN
	 */

	public String getURL(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("url", e);

		return result;
	}
	
	/**
	 * Get the type of the database management system (DBMS) 
	 * @return - The name of the DBMS
	 * @author TOAN
	 */
	
	public DBMS getDBMS(){
		
		NodeList nl = doc.getElementsByTagName("DBMS");
		DBMS result;
		Node n = nl.item(0);
		Element e = (Element) n;
		
		String strDBMSName = e.getTextContent();
		
		if(strDBMSName.equals("MySQL")){
			result = DBMS.MySQL;
		}else if(strDBMSName.equals("PostgreSQL")){
			result = DBMS.PostgreSQL;
		}else if(strDBMSName.equals("SQLServer")){
			result = DBMS.SQLServer;
		}else{
			result = null;
		}
		return result;
	}
	
	/**
	 * Get the name of the required database 
	 * @param databaseType - One of the types from the enumeration
	 * @return - The name of the database
	 * @author TOAN
	 */
	
	public String getDatabaseName(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("database", e);

		return result;
	}
	
	/**
	 * Get the name of the schema 
	 * @param databaseType - One of the types from the enumeration
	 * @return - The name of the schema
	 * @author TOAN
	 */
	
	public String getSchema(int databaseType){
		
		NodeList nl = getDatabaseNodeList(databaseType);
		String result = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		result = getTagValue("schema", e);

		return result;
	}
	
	/**
	 * Get a database connection from the child nodes of a certain type of
	 * DatabaseConnection in the XML file.
	 * @param databaseType - One of the types from the enumeration
	 * @return - An initialized DatabaseConnection object 
	 */
	public DatabaseConnection getDatabaseConnection(int databaseType)
	{
		DatabaseConnection dbc = null;
		NodeList nl = getDatabaseNodeList(databaseType);			

		Node n = null;
		String database = "";
		String schema = "";
		String url = "";
		String user = "";
		String password = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		n = nl.item(0);
		
		if(this.getDBMS().equals(DBMS.MySQL)){
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				database = getTagValue("database", e);
				url = getTagValue("url", e);
				user = getTagValue("user", e);
				password = getTagValue("password", e);				
			}
	
	
			//System.out.format("database:%s, table:%s, url:%s, user:%s, password:%s %n", database,
			//		table, url, user, password);
			try{
				
					dbc = new DatabaseConnection(url, main.mysqlDriver, database, user, password);
			}catch(Exception ex){
				MyUtilities.end("Cannot create database connection. Recheck database connection parameters.");
			}
		}else{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				database = getTagValue("database", e);
				schema = getTagValue("schema", e);
				url = getTagValue("url", e);
				user = getTagValue("user", e);
				password = getTagValue("password", e);				
			}
	
	
			//System.out.format("database:%s, table:%s, url:%s, user:%s, password:%s %n", database,
			//		table, url, user, password);
			try{
				if(user=="NO_USER_VA")
					dbc = new DatabaseConnection(url, database, schema);
				else
					dbc = new DatabaseConnection(url, main.mysqlDriver, database, schema, user, password);
			}catch(Exception ex){
				MyUtilities.end("Cannot create database connection. Recheck database connection parameters.");
			}
		}
		return dbc;
	}
	

	/**
	 * Reads from the XML file & asks for the table name of some database
	 * @param databaseType - One of the types from the enumeration
	 * @return - The name of the table from the DatabaseConnection object in the XML file.
	 */
	public String getTableName(int databaseType)
	{
		NodeList nl = getDatabaseNodeList(databaseType);
		String tableName = "";

		// Only get the first item.  We don't really care if the user
		// has defined more "DatbaseConnection" items in the XML file
		Node n = nl.item(0);
		Element e = (Element) n;
		tableName = getTagValue("table", e);

		return tableName;
	}

	/**
	 * Get all the data from the child nodes of all the MappedPair nodes 
	 * in the XML file.  Also retrieve the attributes of each MappedPair node.
	 * Put the data into a list MappedPair objects & return
	 * @param tagA - The name of the XML tag to retrieve (e.g. read_col, source_a, etc..)
	 * @param tagB - see desc of tagA
	 * @return - An initialized ArrayList of MappedPair objects
	 */
	public ArrayList<MappedPair> getMappedPairs(String tagA, String tagB)
	{
		ArrayList<MappedPair> mps = new ArrayList<MappedPair>();
		NodeList nl = doc.getElementsByTagName("MappedPair");
		Node n = null;

		// Iterate across all MappedPair nodes
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(i);
			String source_a = "";
			String source_b = "";
			
			String dteFormatA = null;
			String dteFormatB = null;

			// Get the children elements for this MappedPair node
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				source_a = getTagValue(tagA, e);
				source_b = getTagValue(tagB, e);

				//System.out.format("r: %s\tw: %s%n", read_col, write_col);
			}

			// Get this nodes attribute
			NamedNodeMap nnm = n.getAttributes();
			Node m = null;
			String attr_val = "";
			String algorithm_val = "";
			double weight_val = 0;
			String attr_name ="";
			String attr_type = "";
			
			for (int j = 0; j < nnm.getLength(); j++)
			{
				m = nnm.item(j);
				if (m.getNodeType() == Node.ATTRIBUTE_NODE) 
				{
			
					attr_name = m.getNodeName();
					if (attr_name.equals("algorithm"))
						algorithm_val = m.getNodeValue();
					else if (attr_name.equals("attr"))
						attr_val = m.getNodeValue();
					else if (attr_name.equals("weight"))
						weight_val = Double.parseDouble(m.getNodeValue());
					else if (attr_name.equals("clear-text"))
						attr_type = m.getNodeValue().toString().trim();
					else if (attr_name.equals("formatRead"))
						dteFormatA = m.getNodeValue().toString().trim();
					else if (attr_name.equals("formatWrite"))
						dteFormatB = m.getNodeValue().toString().trim();
					
					//System.out.println("name: " + attr_name + " val:" + attr_val);
			
				}
			}

			// Add the mapped pair to the list
			mps.add(new MappedPair(source_a, source_b, dteFormatA, dteFormatB, attr_val, algorithm_val, weight_val, attr_type));
		}
		
		return mps;
	}
	
	/**
	 * Get all the data from the child nodes of all the Backup MappedPair nodes 
	 * The backup mapped pairs are used to solve the problem with null value
	 * in the XML file.  Also retrieve the attributes of each MappedPair node.
	 * Put the data into a list MappedPair objects & return
	 * @param tagA - The name of the XML tag to retrieve (e.g. read_col, source_a, etc..)
	 * @param tagB - see desc of tagA
	 * @return - An initialized ArrayList of MappedPair objects
	 */
	public ArrayList<MappedPair> getBackupMappedPairs(String tagA, String tagB)
	{
		ArrayList<MappedPair> mps = new ArrayList<MappedPair>();
		NodeList nl = doc.getElementsByTagName("BackupMappedPair");
		Node n = null;

		// Iterate across all MappedPair nodes
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(i);
			String source_a = "";
			String source_b = "";
			
			String dteFormatA = null;
			String dteFormatB = null;

			// Get the children elements for this MappedPair node
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				source_a = getTagValue(tagA, e);
				source_b = getTagValue(tagB, e);

				//System.out.format("r: %s\tw: %s%n", read_col, write_col);
			}

			// Get this nodes attribute
			NamedNodeMap nnm = n.getAttributes();
			Node m = null;
			String attr_val = "";
			String algorithm_val = "";
			double weight_val = 0;
			String attr_name ="";
			String attr_type = "";
			
			for (int j = 0; j < nnm.getLength(); j++)
			{
				m = nnm.item(j);
				if (m.getNodeType() == Node.ATTRIBUTE_NODE) 
				{
			
					attr_name = m.getNodeName();
					if (attr_name.equals("algorithm"))
						algorithm_val = m.getNodeValue();
					else if (attr_name.equals("attr"))
						attr_val = m.getNodeValue();
					else if (attr_name.equals("weight"))
						weight_val = Double.parseDouble(m.getNodeValue());
					else if (attr_name.equals("clear-text"))
						attr_type = m.getNodeValue().toString().trim();
					else if (attr_name.equals("formatRead"))
						dteFormatA = m.getNodeValue().toString().trim();
					else if (attr_name.equals("formatWrite"))
						dteFormatB = m.getNodeValue().toString().trim();
					
					//System.out.println("name: " + attr_name + " val:" + attr_val);
			
				}
			}

			// Add the mapped pair to the list
			mps.add(new MappedPair(source_a, source_b, dteFormatA, dteFormatB, attr_val, algorithm_val, weight_val, attr_type));
		}
		
		return mps;
	}

	/**
	 * If no tags are specified, assume the user wants the default case of 
	 * "read_col" and "write_col" which is used for the encryption procedure.
	 * @return -  An initialized ArrayList of MappedPair objects
	 */
	public ArrayList<MappedPair> getMappedPairs()
	{
		return this.getMappedPairs("read_col", "write_col");
	}
	
	/**
	 * If no tags are specified, assume the user wants the default case of 
	 * "read_col" and "write_col" which is used for the encryption procedure.
	 * @return -  An initialized ArrayList of MappedPair objects
	 */
	public ArrayList<MappedPair> getBackupMappedPairs()
	{
		return this.getBackupMappedPairs("read_col", "write_col");
	}
	
	/**
	 * Get the blocking variable as a Mapped Pair
	 * Should extract elements from <Blocking> in the XML config file
	 * @return - A mapped pair containing the blockingVariable name
	 */
	public MappedPair getBlockingPair(String tagA, String tagB)
	{
		MappedPair mp = null;
		
		NodeList nl = doc.getElementsByTagName("Blocking");
		Node n = null;
		
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(0);
			
			n = nl.item(i);
			String source_a = "";
			String source_b = "";
			
			// Get the children elements for this MappedPair node
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				source_a = getTagValue(tagA, e);
				source_b = getTagValue(tagB, e);
				//System.out.format("r: %s\tw: %s%n", read_col, write_col);
			}
			mp = new MappedPair(source_a, source_b, "");
		}
		
		return mp;
	}
	
	/**
	 * Get the linking variable as a Mapped Pair
	 * Should extract elements from <Linking> in the XML config file
	 * @return - A mapped pair containing the blockingVariable name
	 */
	
	public MappedPair getLinkingPair(String tagA, String tagB)
	{
		MappedPair mp = null;
		
		NodeList nl = doc.getElementsByTagName("Linking");
		Node n = null;
		
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(0);
			
			n = nl.item(i);
			String source_a = "";
			String source_b = "";
			
			// Get the children elements for this MappedPair node
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				source_a = getTagValue(tagA, e);
				source_b = getTagValue(tagB, e);
				//System.out.format("r: %s\tw: %s%n", read_col, write_col);
			}
			mp = new MappedPair(source_a, source_b, "");
		}
		
		return mp;
	}
	
	/**
	 * Get the EM blocking variable as a Mapped Pair
	 * Should extract elements from <EMBlocking> in the XML config file
	 * @return - A mapped pair containing the blockingVariable name
	 */
	public MappedPair getEMBlockingPair(String tagA, String tagB)
	{
		MappedPair mp = null;
		
		NodeList nl = doc.getElementsByTagName("EMBlocking");
		Node n = null;
		
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(0);
			
			n = nl.item(i);
			String source_a = "";
			String source_b = "";
			
			// Get the children elements for this MappedPair node
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element e = (Element) n;
				source_a = getTagValue(tagA, e);
				source_b = getTagValue(tagB, e);
				//System.out.format("r: %s\tw: %s%n", read_col, write_col);
			}
			mp = new MappedPair(source_a, source_b, "");
		}
		
		return mp;
	}
	
	public String getDestinationType(){
		String result = null;
		
		NodeList nl = null;
		nl = doc.getElementsByTagName("ResultSaver");
		
		if(nl==null){
			Node n = nl.item(0);
			Element e = (Element) n;
			result = getTagValue("DestinationType", e);

		}else{
			result = "CSV";
		}
		return result;
	}
	
	
	
	public MappedPair getEMBlockingPair()
	{
		return this.getEMBlockingPair("read_col", "write_col");
	}
	
	public MappedPair getBlockingPair()
	{
		return this.getBlockingPair("read_col", "write_col");
	}
	
	public MappedPair getLinkingPair()
	{
		return this.getLinkingPair("left_pk", "right_pk");
	}
	
	private NodeList getDatabaseNodeList(int databaseType)
	{
		NodeList nl = null;
		if (databaseType == READER)
			nl = doc.getElementsByTagName("DatabaseReaderConnection");
		else if (databaseType == WRITER)
			nl = doc.getElementsByTagName("DatabaseWriterConnection");
		else if (databaseType == SOURCEA)
			nl = doc.getElementsByTagName("DatabaseSourceA");
		else if (databaseType == SOURCEB)
			nl = doc.getElementsByTagName("DatabaseSourceB");
		else if (databaseType == SAVER)
			nl = doc.getElementsByTagName("DatabaseSaver");
		else
			XML_Reader.invalidDatabaseTypeError();

		return nl;
	}
	
	private static String getDateFormat(String tag, Element e)
	{
		String result = null;
		NodeList nodeList = e.getElementsByTagName(tag).item(0).getChildNodes();
		Node n = (Node) nodeList.item(0);
		NamedNodeMap nnm = n.getAttributes();
		
		if(nnm!=null){
			Node m = null;
			String attr_val = "";
			String algorithm_val = "";
			String attr_name ="";
			String attr_type = "";
			
			for (int j = 0; j < nnm.getLength(); j++)
			{
				m = nnm.item(j);
				if (m.getNodeType() == Node.ATTRIBUTE_NODE) 
				{
			
					attr_name = m.getNodeName();
					if (attr_name.equals("format")){
						result = m.getNodeValue().trim();
					}
				}
			}
		}
		
		return result;
	}
	/**
	 * Given some tag value, get it from element e
	 * @param tag - The tag of the value that you want
	 * @param e - The element containing the tags
	 * @return - The value within the tag
	 */
	private static String getTagValue(String tag, Element e) 
	{
		if(e.getElementsByTagName(tag).item(0)!=null){
			NodeList nodeList = null;
			nodeList = e.getElementsByTagName(tag).item(0).getChildNodes();
			Node n = (Node) nodeList.item(0);
			return n.getNodeValue();
		}else{
			return null;
		}
	}

	/**
	 * Call this function when an invalid database type has been selected.
	 * This will provide the user with helpful error information so he
	 * can get things setup properly.
	 */
	private static void invalidDatabaseTypeError()
	{
		MyUtilities.end("Invalid database type specfied.\n"
				+ "Ensure that you are using the enumartion in XML_Reader.\n" +
				"Also ensure that encryption_config.xml is correct.\n" +
		"Program Terminated.\n");
	}


}
