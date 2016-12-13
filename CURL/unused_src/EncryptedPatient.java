import java.util.*;
/**
 * Patient.java
 * Purpose: This is a EncryptedPatient class with the following attributes:
 * firstName, lastName, SSN DOB, gender, address, city, state, zip
 * and phone number. Each object contains a BF for each field that is being
 * encrypted. The rest of the fields are left in clear text.
 * 
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 * 
 */
public class EncryptedPatient {
	private ArrayList<BF> attributes;
	//private BF attributes[]; //list of BF that are the encrypted attributes
	private Map DOB;
	//Use for multiple blocking variables
	//private Map lastName;
	private String rest; //string of rest of the fields
	/**
	 * Default constructor
	 */
	public EncryptedPatient(){
		//if last name is added change the size of the array to 8
		attributes =  new ArrayList<BF>();
	}
	/**
	 * This is a constructor that encrypts each field and places them in the attributes array
	 * @param SSN SSN of Patient
	 * @param firstName firstName of Patient
	 * @param lastName lastName of Patient
	 * @param DOB DOB of Patient
	 * @param gender gender of Patient
	 * @param street street of Patient
	 * @param city city of Patient
	 * @param state state of Patient
	 * @param zip of Patient
	 * @param phone phone of Patient
	 * @param rest rest of Patient info
	 * @throws Exception if an error in encrypting occurs
	 */
	public EncryptedPatient(String SSN, String firstName, String lastName,
			String DOB, String gender, String street, String city, String state, String zip, String phone, String rest) throws Exception {
		attributes =  new ArrayList<BF>();
		attributes.add(new BF(SSN));
		attributes.add(new BF(firstName));
		//change the array index to of 3-8 to 2-7
		//use the input line this.lastName = new Map(lastName
		attributes.add(new BF(lastName));
		this.DOB = new Map(DOB, new BF(DOB));
		attributes.add(new BF(gender));
		attributes.add(new BF(street));
		attributes.add(new BF(city));
		attributes.add(new BF(state));
		attributes.add(new BF(zip));
		attributes.add(new BF(phone));
		this.rest = rest;
	}
	/**
	 * This method finds the Sensitivity of this encryptedPatient and 
	 * with an input encryptedPatient
	 * @param input compare to this object
	 * @return sensitivity of the comparison
	 * @throws Exception
	 */
	public double getSen(EncryptedPatient input) throws Exception{
		BF compare, compareTo;
		double value = 0;
		FieldWieghts weights = new FieldWieghts();
		for(int j = 0; j < attributes.size(); j++)
			value += this.findDice(input, j)*weights.match(j) + (1-this.findDice(input, j))*weights.nonMatch(j);
		return value;
	}
    /**
     * Returns the Paitent attribute that is now a Bloom Filter
     *
     * @param int i if in the range of the attributes.size()
     * @return a BF of the corresponding attributes
     */
	public BF getInstance(int i){
		if(i < 0 || i > 9)
			return null;
		return attributes.get(i);
	}
	public String getRest(){
		return rest;
	}
	/**
	 * This method finds the Dice Coefficient of two independent BloomFilters
	 * only looks for the intersection of 1's and uses the totally of ones
	 * @param p patient to compare with 
	 * @param i field to compare
	 * @return the dice coefficient of the comparison
	 */
	public double findDice(EncryptedPatient p, int i){
		int hits = 0;
		int inCount = 0;
		int count = 0;
		BF check = p.getInstance(i);
		for (int x = 0; x < check.size(); x++){
			if(check.getBit(x) && this.getInstance(i).getBit(x))
            	hits++;
            if(check.getBit(x))
            	inCount++;
            if(this.getInstance(i).getBit(x))
            	count++;
		}
        return 2.0*((double)hits/(count + inCount));
	}
	/**
	 * toString() method which is used to write to file
	 */
	public String toString(){
		String returner = "";
		int count = 0;
		for(BF filter : attributes){
			//this if state is here to make it so the output is in the same order
			//the input
			if(count == 3)
				returner += this.DOB;
			if(count == attributes.size() - 1)
				returner += attributes.get(count);
			else
				returner += attributes.get(count) + ",";
			count++;
		}
		returner += rest;
		return returner;
	}
}
