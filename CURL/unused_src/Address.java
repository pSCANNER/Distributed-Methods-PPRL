/**
 * Address.java
 * Purpose: Standardization of the Patient class
 * Contains informations about a Paitent address
 * The information is Street, City, State, and Zip code
 * 
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 */

public class Address {

	private String street;
	private String zip;
	private String city;
	private String state;

	/**
	 * default constructor
	 */
	public Address(){
		street = "";
		zip = "";
		city = "";
		state = "";
	}
	/**
	 * constructor
	 * @param street street of Patient
	 * @param city city of Patient
	 * @param state state of Patient
	 * @param zip zip of Patient
	 */
	public Address(String street, String city, String state, String zip){
		this.street = street;
		this.zip = zip;
		this.city = city;
		this.state = state;
	}
	/**
	 * constructor based on one String input that should be
	 * in street,city,state format if any contain spaces please follow the string in quotes
	 * e.g. "9655 E Balancing Ct.",Bishop,WI or "4591 E Tax Ave.","Yuba City",WV comma delimited
	 * @param address address to be Set
	 * @deprecated
	 */
	public Address(String address){
		setAddress(address);
	}
	/**
	 * returns the street on the obj
	 * @returns street
	 */
	public String getStreet() {
		return street;
	}
	/**
	 * setter method based on one String input that should be
	 * in street,city,state format if any contain spaces please follow the string in quotes
	 * e.g. "9655 E Balancing Ct.",Bishop,WI or "4591 E Tax Ave.","Yuba City",WV comma delimited
	 * @param address address to be Set
	 * @deprecated
	 */
	public void setAddress(String address){
		if(address.length() <= 0){
			address = "";
		}else{
			String all = address;
			street = all.substring(1, address.indexOf(',') - 1);
			all = all.substring(address.indexOf(',')+1);
			city = all.substring(all.indexOf('"')+ 1, all.indexOf(','));
			state = all.substring(all.indexOf(',')+ 1);
		}
	}
	/**
	 * set only the street of the Object
	 * @param street to be set in Object
	 */
	public void setStreet(String street) {
		this.street = street;
	}
	/**
	 * getZip
	 * @return zip of Object
	 */
	public String getZip() {
		return zip;
	}
	/**
	 * set only the zip of the Object
	 * @param zip to be set in Object
	 */
	public void setZip(String zip) {
		this.zip = zip;
	}
	/**
	 * getCity
	 * @return city of Object
	 */
	public String getCity() {
		return city;
	}
	/**
	 * set only the city of the Object
	 * @param city to be set in Object
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * getState
	 * @return state of Object
	 */
	public String getState() {
		return state;
	}
	/**
	 * set only the state of the Object
	 * @param state to be set in Object
	 */
	public void setState(String state) {
		this.state = state;
	}
	/**
	 * toString method. Be sure to keep the delimiter consistent with the Patient class
	 * The Patient and this class are both using a comma delimiter currently
	 */
	public String toString() {
		return street + "," + city + "," + state + "," + zip;
	}

}
