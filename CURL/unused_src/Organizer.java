import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Organizer.java
 * Purpose: Organizes the way a file or database is inputted 
 * because of how the Patient class is created. This allows 
 * for the fields to be in any order when it is being read in 
 * 
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 */
// TODO: Re-factor and Simplify this class
public class Organizer {
	private ArrayList<String> desiredOrder; // Correct order of fields
	private ArrayList<String> actualOrder; // The way it is in the input
	private Map<Integer, Integer> indexMap;//Map to map the file in the correct order
	private ArrayList<String> returnValue;//global variable to find the number of fields being inputed
	/**
	 * default constructor
	 */
	public Organizer(){
		String[] temp = {"SSN", "firstName", "lastName", "DOB", "sex", "addr", "city", "state", "zip", "phone" };
		desiredOrder = toArrayList(temp);
		indexMap = new HashMap<Integer, Integer>();
	}
	/**
	 * String[] parameter for the constructor
	 * @param input String[] to find size of so returnValue is correct size
	 */
	public Organizer(String[] input){
		String[] temp = {"SSN", "firstName", "lastName", "DOB", "sex", "addr", "city", "state", "zip", "phone" };
		desiredOrder = toArrayList(temp);
		actualOrder = rename(toUpperCase(toArrayList(input)));
		indexMap = new HashMap<Integer, Integer>();
		createMap();
	}
	/**
	 * returns the mapping of specific field
	 * @param input field
	 * @return where to be mapped
	 */
	public int getMap(String input){
		return indexMap.get(actualOrder.indexOf(input)).intValue();
	}
	/**
	 * converts an Array to an arrayList and this method
	 * also gives the global variable returnValue the number
	 * of fields that are being inputed
	 * @param input string array to be converted
	 * @return an ArrayList that contains the same info as the parameter
	 */
	public ArrayList<String> toArrayList(String[] input){
		returnValue = new ArrayList<String>();
		for(int i = 0; i < input.length; i++){
			returnValue.add(input[i]);
		}
		return toUpperCase(returnValue);
	}
	/**
	 * gives the desired order array get method
	 * @return arraylist of the desired order
	 */
	public ArrayList<String> accessDesired(){
		return desiredOrder;
	}
	/**
	 * setter of the actual order of the input
	 * @param actualOrder header of fields in Arraylist
	 */
	public void setActualOrder(ArrayList<String> actualOrder) {
		this.actualOrder = rename(actualOrder);
		createMap();
	}
	/**
	 * creates a map to organize the fields in the correct order
	 */
	private void createMap(){
		for(int i = 0; i < actualOrder.size(); i++){
			if(desiredOrder.indexOf(actualOrder.get(i)) < 0)
				indexMap.put(i, actualOrder.size() + 1);
			else
				indexMap.put(i, desiredOrder.indexOf(actualOrder.get(i)));
		}
	}
	/**
	 * Renames the name of the field for usability. This is 
	 * inefficient and should be changed. This allows for other 
	 * variations of the same name from the input
	 * @param input names to be changed
	 * @return a list of the correct names
	 */
	private ArrayList<String> rename(ArrayList<String> input){
		for(int i = 0; i < input.size(); i++){
			if(returnValue.get(i).contains("SSN"))	
				returnValue.set(i, "SSN");
			else if(returnValue.get(i).contains("SOCIAL"))
				returnValue.set(i, "SSN");
			else if(returnValue.get(i).contains("FIRST"))
				returnValue.set(i, "FIRSTNAME");
			else if(returnValue.get(i).contains("FN"))
				returnValue.set(i, "FIRSTNAME");
			else if(returnValue.get(i).contains("LAST"))
				returnValue.set(i, "LASTNAME");
			else if(returnValue.get(i).contains("LN"))
				returnValue.set(i, "LASTNAME");
			else if(returnValue.get(i).contains("SIR"))
				returnValue.set(i, "LASTNAME");
			else if(returnValue.get(i).contains("BIRTH"))
				returnValue.set(i, "DOB");
			else if(returnValue.get(i).contains("DOB"))
				returnValue.set(i, "DOB");
			else if(returnValue.get(i).contains("DATE") && returnValue.get(i).contains("B"))
				returnValue.set(i, "DOB");
			else if(returnValue.get(i).contains("SEX"))
				returnValue.set(i, "SEX");
			else if(returnValue.get(i).contains("GEN"))
				returnValue.set(i, "SEX");
			else if(returnValue.get(i).contains("ADD"))	
				returnValue.set(i, "ADDR");
			else if(returnValue.get(i).contains("ST"))
				returnValue.set(i, "STATE");
			else if(returnValue.get(i).contains("Z"))	
				returnValue.set(i, "ZIP");
			else if(returnValue.get(i).contains("PHO"))
				returnValue.set(i, "PHONE");
		}
		return returnValue;
	}
	/**
	 * Organizes the fields in the correct order
	 * @param input ArrayList that contains all of the info of a Patient
	 * @return return ArrayList with correct order of attributes
	 */
	public ArrayList<String> organize(ArrayList<String> input){
		try{
			returnValue = (ArrayList<String>) desiredOrder.clone();
			for(int i = 0; i < input.size(); i++){
				if(indexMap.get(i) >= returnValue.size()){
					returnValue.add(input.get(i));
				} else {
					returnValue.set(indexMap.get(i), input.get(i));
				}
			}
			return returnValue;
		}catch(Exception e){
			return new ArrayList<String>();
		}
	}
	/**
	 * converts all fields to Upper case
	 * @param input patient fields to convert
	 * @return ArrayList with all the items in upper case
	 */
	private ArrayList<String> toUpperCase(ArrayList<String> input){
		for(int i = 0; i < input.size(); i++){
			input.set(i, input.get(i).toUpperCase());
		}
		return input;
	}
	
	public void setReturnValue(ArrayList<String> input)
	{
		returnValue = input;
	}
}
