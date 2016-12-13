import java.io.*;
import java.sql.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.codec.language.*;
public class driver {
	public static void main(String[] args) throws Exception{
		ArrayList<Patient> pData = new ArrayList<Patient>();
		ArrayList<EncryptedPatient> encryptedData = new ArrayList<EncryptedPatient>();
		FileWriter fstream;
		fstream = new FileWriter("doc/fileA_5000_header" + "_encrypted");
		BufferedWriter out;
		out = new BufferedWriter(fstream);
		CSVParser csv = new CSVParser();
		csv.setFile("doc/fileA_5000_header");
		csv.setSeperator('|');
		pData = csv.parseFile();
		for(int j = 0; j < pData.size(); j++){
			encryptedData.add(pData.get(j).toEncryptPatient());
		}
		pData.clear();
		while(encryptedData.size() != 0){
			out.write(encryptedData.get(0) + "\n");
			encryptedData.remove(0);
		}
		out.close();
		System.out.print("");
	}
	private static ArrayList<String> toArrayList(String[] input){
		ArrayList<String> returnValue = new ArrayList<String>();
		for(int i = 0; i < input.length; i++){
			returnValue.add(input[i]);
		}
		return returnValue;
	}
//	private static ArrayList<String> toUpperCase(ArrayList<String> input){
//		for(int i = 0; i < input.size(); i++){
//			input.set(i, input.get(i).toUpperCase());
//		}
//		return input;
//	}
}
