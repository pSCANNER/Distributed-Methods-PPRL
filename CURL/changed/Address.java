/**
 * Address.java
 * Purpose: Standardization of the Patient class
 * Contains informations about a Paitent address
 * The information is Street, City, State, and Zip code
 * 
 * @author Vijay Thurimella
 * @version 1.0 7/22/10
 */

public class Address {

	private String street;
	private String zip;
	private String city;
	private String state;

	//default constructor
	public Address(){
		street = "";
		zip = "";
		city = "";
		state = "";
	}
	//constructor
	public Address(String street, String city, String state, String zip){
		this.street = street;
		this.zip = zip;
		this.city = city;
		this.state = state;
	}
	//constructor based on one String input that should be
	//in street,city,state format if any contain spaces please follow the string in quotes
	//e.g. "9655 E Balancing Ct.",Bishop,WI or "4591 E Tax Ave.","Yuba City",WV
	//comma delimited
	public Address(String address){
		System.out.println(address);
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
	//returns the street on the obj
	public String getStreet() {
		return street;
	}
	//setter method based on one String input that should be
	//in street,city,state format if any contain spaces please follow the string in quotes
	//e.g. "9655 E Balancing Ct.",Bishop,WI or "4591 E Tax Ave.","Yuba City",WV
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
	//set only the street obj
	public void setStreet(String street) {
		this.street = street;
	}
	//returns the zip of the obj
	public String getZip() {
		return zip;
	}
	//set only the zip obj notice the String input
	public void setZip(String zip) {
		this.zip = zip;
	}
	//returns the city of the obj
	public String getCity() {
		return city;
	}
	//set only the city obj
	public void setCity(String city) {
		this.city = city;
	}
	//returns the state of the obj
	public String getState() {
		return state;
	}
	//set only the city obj
	public void setState(String state) {
		this.state = state;
	}
	//toString method for print in address
	public String toString() {
		return street + " , " + city + " , " + state + " , " + zip;
	}

}
