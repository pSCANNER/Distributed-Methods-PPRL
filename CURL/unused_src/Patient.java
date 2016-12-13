import java.util.ArrayList;
import java.util.HashMap;

/**
 * Patient.java
 * Purpose: This is a Patient class with the following attributes:
 * firstName, lastName, SSN DOB, gender, address, city, state, zip
 * and phone number. The access of these variables is by the
 * getInstance() method
 * 
 * @author Vijay Thurimella, Brandon Abbott
 * @version 2.0 8/31/10
 */
public class Patient 
{
	// Attributes of the patient to be encrypted
	private String firstName; 
	private String lastName;
	private String SSN;
	private String DOB;
	private String DOBHSH;
	private String gender;
	private String street;
	private String zip;
	private String city;
	private String state;
	private String phone; 
	// rest of the data given of Patient (NOT encrypted)
	private String extra;
	// Determines if member variables are all in the encrypted form
	private boolean isEncrypted = false;
	
	// Determines which item in a row of data belongs to which field in the DB
	private static HashMap<String, Integer> columnNameMap;

	// TODO: Work to eliminate the need for this member
	// TODO: Also remove getters/setters for "attributes" when needed.
	// An array list of all the attributes
	public ArrayList<BF> attributes = null;
	
	// TODO: add constructor definition here
	public Patient(String firstName, String lastName, String sSN, String dOB,
			String dOBHSH, String gender, String street, String zip,
			String city, String state, String phone, String extra,
			boolean isEncrypted) {
		this.firstName = firstName;
		this.lastName = lastName;
		SSN = sSN;
		DOB = dOB;
		DOBHSH = dOBHSH;
		this.gender = gender;
		this.street = street;
		this.zip = zip;
		this.city = city;
		this.state = state;
		this.phone = phone;
		this.extra = extra;
		this.isEncrypted = isEncrypted;
	}
	
	// TODO: add constructor definition here
	public Patient(String[] dataRow)
	{
		// Set member variables that will be encrypted
		this.firstName = dataRow[columnNameMap.get(MyUtilities.FIRSTNAME)];
		this.lastName = dataRow[columnNameMap.get(MyUtilities.LASTNAME)];
		this.SSN = dataRow[columnNameMap.get(MyUtilities.SSN)];
		this.DOB = dataRow[columnNameMap.get(MyUtilities.DOB)];
		this.gender = dataRow[columnNameMap.get(MyUtilities.SEX)];
		this.phone = dataRow[columnNameMap.get(MyUtilities.PHONE)];
		this.street = dataRow[columnNameMap.get(MyUtilities.ADDR)];
		this.city = dataRow[columnNameMap.get(MyUtilities.CITY)];
		//TODO: Add these two fields to the dataRow array
		this.state = "";
		this.zip = "";
		// Set member variables that will be encrypted to ""
		dataRow[columnNameMap.get(MyUtilities.FIRSTNAME)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.LASTNAME)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.SSN)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.DOB)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.SEX)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.PHONE)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.ADDR)] = "1#1";
		dataRow[columnNameMap.get(MyUtilities.CITY)] = "1#1";
		
		// Set member vars that will not be encrypted
		// That is, set member EXTRA
		int extraCount = 0;
		for (int i = 0; i < dataRow.length; i++)
			if (! dataRow[i].equals("1#1"))
				extraCount++;
		
		this.extra = ""; int count = 0;
		for (int i = 0; i < dataRow.length; i++)
		{
			if (! dataRow[i].equals("1#1"))
			{
				this.extra += dataRow[i];
				if (++count < extraCount)
					this.extra += "|";
			}
		}
	}

	/**
	 * Encrypt all member variables of the patient, set isEncrypted to 'true'
	 */
	public void toEncryptPatient() {
		try {
			BF temp = new BF(SSN);
			SSN = temp.toString();

			temp = new BF(firstName);
			firstName = temp.toString();

			temp = new BF(lastName);
			lastName = temp.toString();

			String s = noYear(DOB);
			DOBHSH = Encryption.SHA1(s);	
			//this.bDay = new Map(DOB, new BF(DOB));
			//System.out.println(s + ":" + DOBHSH);
			
			temp = new BF(DOB);
			DOB = temp.toString();

			temp = new BF(gender);
			gender = temp.toString();

			temp = new BF(street);
			street = temp.toString();
			
			temp = new BF(city);
			city = temp.toString();
			
			temp = new BF(state);
			state = temp.toString();
			
			temp = new BF(zip);
			zip = temp.toString();
			
			temp = new BF (phone);
			phone = temp.toString();
		} catch (Exception e) {
			System.err.println("Problem creating bloom filters for patient members");
			e.printStackTrace();
		}
		isEncrypted = true;
	}

	/**
	 * toString methods to print the patient data and be consistent with the 
	 * toString method of the address class
	 *
	 */
	public String toString()
	{
		if (!isEncrypted)
			return SSN + "," + firstName  + "," + lastName + "," + DOB 
				+ "," + gender + "," + street + "," + city + "," + state
				+ "," + zip + "," + phone + "," + extra;
		
		return  SSN + "," + firstName  + "," + lastName + "," + DOBHSH + 
				"," + DOB+ "," + gender + "," + street + "," + city + 
				"," + state	+ "," + zip + "," + phone + "," + extra;
	}
	
	// TODO: function description goes here (perhaps rename?)
	private String noYear(String input){
		int count = 0;
		String end = "";
		Character pos = new Character('0');
		for(int i = 0; i < input.length(); i++){
			if(count == 2){
				end = input.substring(0, i);
				break;
			}
			if(!pos.isDigit(input.charAt(i)))
				count++;
			
		}
		end = removeChar(end.toString());
		return end;
	}
	
	/**
	 * This function removes everything that is not a digit from a string
	 * @param input
	 * @return
	 */
	private String removeChar(String input){
		String end = "";
		Character pos = new Character('0');
		for(int i =0; i < input.length(); i++){
			if(pos.isDigit(input.charAt(i)))
				end += input.charAt(i);
		}
		return end.toString();
	}
	
	// ***************************************
	// Setters for patient attributes
	// ***************************************
	public static void setColumnNameMap(HashMap<String, Integer> columnNameMap) 
	{
		Patient.columnNameMap = columnNameMap;
	}
	
	public void setIsEncrypted(boolean val)
	{
		this.isEncrypted = val;
	}
	
	// ***************************************
	// Getters for patient attributes
	// ***************************************
	public ArrayList<BF> getAttributes() {
		try {
			if (attributes == null)
			{
				attributes =  new ArrayList<BF>();
				attributes.add(new BF(SSN));
				attributes.add(new BF(firstName));
				attributes.add(new BF(lastName));
				attributes.add(new BF(DOB));
				attributes.add(new BF(gender));
				attributes.add(new BF(street));
				attributes.add(new BF(city));
				attributes.add(new BF(state));
				attributes.add(new BF(zip));
				attributes.add(new BF(phone));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return attributes;
	}

	public static HashMap<String, Integer> getColumnNameMap() 
	{
		return columnNameMap;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getSSN() {
		return SSN;
	}

	public String getDOB() {
		return DOB;
	}

	public String getGender() {
		return gender;
	}

	public String getStreet() {
		return street;
	}

	public String getZip() {
		return zip;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getPhone() {
		return phone;
	}

	public String getExtra() {
		return extra;
	}

	public boolean isEncrypted() {
		return isEncrypted;
	}

	public String getDOBHSH() {
		return DOBHSH;
	}
}