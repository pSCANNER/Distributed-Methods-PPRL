import java.io.IOException;
import java.util.ArrayList;
/**
 * ConvertToChar.java
 * This class has one static method that is used
 * in the BF class only
 * 
 * @author Vijay Thurimella
 * @version 2.0 8/31/10
 */

public class ConvertToChar {
    /**
     * Read on string and return an ArrayList of all the bigrams of the String
     * 
     * @param input String to convert to bigrams
     * @return an ArrayList of all the bigrams of a String
     */
	public static ArrayList<char[]> convert(String input){
		ArrayList<char[]> charList = new ArrayList<char[]>();
		char left;
		char right;
		input = " " + input + " ";
		for(int i = 0; i < input.length()-1; i++){
			left  = input.charAt(i);
			right = input.charAt(i+1);
			char[] temp = {left, right };
			charList.add(temp);
		}
		return charList;
	}
}
