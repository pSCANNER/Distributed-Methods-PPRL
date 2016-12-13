import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Organizer {
	private ArrayList<String> desiredOrder;
	private ArrayList<String> actualOrder;
	private Map<Integer, Integer> indexMap;
	private ArrayList<String> returnValue;
	public Organizer(){
		String[] temp = PatientFileAttr.Correct;
		desiredOrder = toArrayList(temp);
		indexMap = new HashMap<Integer, Integer>();
	}
	public Organizer(ArrayList<String> input){
		String[] temp = PatientFileAttr.Correct;
		desiredOrder = toArrayList(temp);
		actualOrder = rename(toUpperCase(input));
		indexMap = new HashMap<Integer, Integer>();
		createMap();
	}
	public ArrayList<String> toArrayList(String[] input){
		returnValue = new ArrayList<String>();
		for(int i = 0; i < input.length; i++){
			returnValue.add(input[i]);
		}
		return toUpperCase(returnValue);
	}
	public ArrayList<String> accessDesired(){
		return desiredOrder;
	}
	public void setActualOrder(ArrayList<String> actualOrder) {
		this.actualOrder = rename(toUpperCase(actualOrder));
		createMap();
	}
	private void createMap(){
		for(int i = 0; i < actualOrder.size(); i++){
			if(desiredOrder.indexOf(actualOrder.get(i)) < 0)
				indexMap.put(i, actualOrder.size());
			else
				indexMap.put(i, desiredOrder.indexOf(actualOrder.get(i)));
		}
		System.out.print("");
	}
	private ArrayList<String> rename(ArrayList<String> input){
		for(int i =0; i < input.size(); i++){
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
			else if(returnValue.get(i).contains("DATE"))
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
	public ArrayList<String> organize(ArrayList<String> input){
		returnValue = (ArrayList<String>) desiredOrder.clone();
		for(int i = 0; i < input.size(); i++){
			if(indexMap.get(i) >= returnValue.size() - 1){
				returnValue.add(input.get(i));
			} else{
				returnValue.set(indexMap.get(i), input.get(i));
			}
		}
		return returnValue;
	}
	private ArrayList<String> toUpperCase(ArrayList<String> input){
		for(int i = 0; i < input.size(); i++){
			input.set(i, input.get(i).toUpperCase());
		}
		return input;
	}
}
