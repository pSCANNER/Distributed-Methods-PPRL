/**
 * Patient.java
 * Purpose: This is a Patient class with the following attributes:
 * firstName, lastName, SSN DOB, gender, address, city, state, zip
 * and phone number. The access of these variables is by the
 * getInstance() method
 * 
 * @author Vijay Thurimella
 * @version 1.0 7/22/10
 */
public class Patient {
	private String[] allAttr;
//	private String firstName;
//	private String lastName;
//	private String SSN;
//	private String DOB;
//	private String gender;
	private Address address;
//	private String phone;
//	private String rest;
	
	//default constructor
	public Patient() {
//		firstName = "";
//		lastName = "";
//		SSN = "";
//		DOB = "";
//		gender = "";
		address = new Address();
//		phone = "";
//		rest = "";
	}
	//constructor based on the database
	public Patient(String SSN, String first, String last, String DOB, String gender,
			String address, String city, String state, String zip, String phone){
		this.SSN = SSN;
		firstName = first;
		lastName = last;
		this.DOB = DOB;
		this.gender = gender;
		this.address = new Address(address, city, state, zip);
		this.phone = phone;
		
		rest = "";
	}
	//toString methods to print the patient data
	public String toString(){
		return "{" + SSN + " , " + firstName  + " , " + lastName + " , " + DOB 
			+ " , " + gender + " , " + address + " , " + phone + rest + "}";
		
	}
	public void setInstance(int i, String value){
		switch (i){
		case 0: this.SSN = checkNullity(removeChar(value, '-')).toUpperCase(); break;
		case 1: this.firstName = checkNullity(value).toUpperCase(); break;
		case 2:	this.lastName = checkNullity(value).toUpperCase(); break;
		case 3:	this.DOB = checkNullity(removeChar(value, '/')).toUpperCase();break;
		case 4:	this.gender = checkNullity(value).toUpperCase();break;
		case 5:	address.setStreet(checkNullity(value).toUpperCase());break;
		case 6:	address.setCity(checkNullity(value).toUpperCase());break;
		case 7:	address.setState(checkNullity(value).toUpperCase());break;
		case 8:	address.setZip(checkNullity(value).toUpperCase());break;
		case 9:	this.phone = checkNullity(removeChar(value, '-')).toUpperCase();break;
		default: rest += "," + value;
		}
	}
	//getInstance returns a specfic field based on the para
	//if 0 it returns SSN, 1 returns firstName, 2 returns lastName
	//3 returns DOB... 9 returns phone
	//This mapping is important because it is used in many other methods
	public String getInstance(int i){
		String returner = "";
		switch (i){
		case 0: returner = "" + SSN; break;
		case 1: returner = firstName ;break;
    	case 2:	returner = lastName;break;
    	case 3:	returner = "" + DOB;break;
    	case 4:	returner = "" + gender;break;
    	case 5:	returner = address.getStreet();break;
    	case 6:	returner = address.getCity();break;
    	case 7:	returner = address.getState();break;
    	case 8:	returner = "" + address.getZip();break;
    	case 9:	returner = "" + phone;break;
		}
		return returner; 
	}
	public EncryptedPatient toEncryptPatient() throws Exception {
		return new EncryptedPatient(SSN, firstName, lastName, DOB, gender, 
				address.getStreet(), address.getCity(), address.getState(), address.getZip(), phone, rest);
	}
	private String removeChar(String input, char c){
		StringBuilder sb = new StringBuilder("");
		for(int i = 0; i < input.length(); i++)
			if(!(input.charAt(i) == c))
				sb.append(input.charAt(i));
		return sb.toString();
	}
	private String checkNullity(String value){
		Organizer o = new Organizer();
		if(o.accessDesired().contains(value))
			return "";
		return value;
	}
}
