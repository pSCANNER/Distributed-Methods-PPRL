package rosita.linkage.tools;

import java.util.Random;

/**
 * This class is a very simple data corrupted created for the purpose of testing clear-text record linkage
 * 
 * @author TOAN
 *
 */
public class DataCorrupter {
	
	private Random rand;
	private RandomString stringGenerator;
	
	public DataCorrupter(){
		rand = new Random();
		stringGenerator = new RandomString(1);
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return the corrupted string with an extra random character somewhere in the string
	 *  
	 */
	public String createTypoInsertError(String parStr){
		String result = "";
		//Identify the location in the string where the insertion occurs
		int pos = rand.nextInt(parStr.length()) - 1;
		if(pos<0){
			pos = 0;
		}
		result = parStr.substring(0,pos) + stringGenerator.nextString() + parStr.substring(pos, parStr.length());	
		return result;
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return the corrupted string with a missing character
	 */
	public String createTypoDeleteError(String parStr){
		String result = "";
		
		if(parStr.length()>2){
			//Identify the location in the string where the deletion occurs
			int pos = rand.nextInt(parStr.length()) - 1;
			if(pos<0){
				pos = 0;
			}
			result = parStr.substring(0,pos)+parStr.substring(pos + 1, parStr.length());
		}else{
			result = parStr;
		}
		return result;
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return the corrupted string with an extra random character somewhere in the string
	 */
	public String createTypoSubstituteError(String parStr){
		String result = "";
		//Identify the location in the string where the substitution occurs
		if(parStr.length()>2){
			int pos = rand.nextInt(parStr.length()) - 1;
			if(pos<0){
				pos = 0;
			}
			result = parStr.substring(0,pos) + stringGenerator.nextString() + parStr.substring(pos + 1, parStr.length());
		}else{
			result = parStr;
		}
		return result;
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return The string with two adjacent characters being transposed
	 */
	public String createTypoTransposeError(String parStr){
		String result = "";

		if(parStr.length()>=4){
			//Identify the location in the string where the transposition occurs
			int pos = rand.nextInt(parStr.length()) - 2;
			if(pos<0){
				pos = 0;
			}
			String tempStr = parStr.substring(pos,pos+2);
			tempStr = tempStr.substring(1, 2) + tempStr.substring(0, 1);
			result = parStr.substring(0,pos)+tempStr+parStr.substring(pos + 2, parStr.length());
		}else{
			result = parStr;
		}
		return result;
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return a SSN with a number different number at a random position
	 */
	public String createNumberError(String parStr){
		String result = "";
		String strNumberList = "0123456789";
		
		//Identify the location in the string where the transposition occurs
		//generate random position
		
		int pos = -1;
		while (pos==-1){
			pos = rand.nextInt(parStr.length());
			if(!strNumberList.contains(parStr.substring(pos,pos+1))){
				pos = -1;
			}
		}
		
		int intCurrentNum = Integer.parseInt(parStr.substring(pos,pos+1)); 
		int intNewNum = intCurrentNum;
		
		while (intNewNum == intCurrentNum){
			intNewNum = rand.nextInt(10);
		}
		if(!strNumberList.contains(String.valueOf(intNewNum))){
			int k = 0;
		}
		
		result = parStr.substring(0,pos) + String.valueOf(intNewNum) + parStr.substring(pos + 1, parStr.length());
		
		return result;
	}
	
	/**
	 * 
	 * @param parStr - the input string. Input string should be trimmed before inserted.
	 * @return a SSN with a number different number at a random position
	 */
	public String createGenderError(String parStr){
		if(parStr.equals("M")){
			return "F";
		}else{
			return "M";
		}
	}	
}
