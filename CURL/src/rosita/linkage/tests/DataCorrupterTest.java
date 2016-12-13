package rosita.linkage.tests;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

import rosita.linkage.main;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.tools.DataCorrupter;

public class DataCorrupterTest {
	
	public static class CorruptedResult{
		String strCorruptedString;
		int intType;
		
		public CorruptedResult(String parStr, int parType){
			strCorruptedString = parStr;
			intType = parType;
		}
	}

	private static DataCorrupter  corrupter = new DataCorrupter();

	public static String toProperCase(String parStr){
		String result = parStr;
		
		if(parStr.length()>0){
			result = parStr.substring(0,1).toUpperCase()+parStr.substring(1,parStr.length()).toLowerCase();
		}
		return result;
	}
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		executeCorruption("tz.source14k_a",10);
		executeCorruption("tz.source14k_b",20);
		
		//executeCorruption("tz3.a_111",5);
		//executeCorruption("tz3.a_112",5);
		//executeCorruption("tz3.a_113",5);
		/*executeCorruption("tz3.a_121",5);
		executeCorruption("tz3.a_122",5);
		executeCorruption("tz3.a_123",5);
		executeCorruption("tz3.a_131",5);
		executeCorruption("tz3.a_132",5);
		executeCorruption("tz3.a_133",5);*/

		/*executeCorruption("tz3.a_211",10);
		executeCorruption("tz3.a_212",10);
		executeCorruption("tz3.a_213",10);
		/*executeCorruption("tz3.a_221",10);
		executeCorruption("tz3.a_222",10);
		executeCorruption("tz3.a_223",10);
		executeCorruption("tz3.a_231",10);
		executeCorruption("tz3.a_232",10);
		executeCorruption("tz3.a_233",10);*/
		
		//executeCorruption("tz3.a_311",20);
		//executeCorruption("tz3.a_312",20);
		//executeCorruption("tz3.a_313",20);
		/*executeCorruption("tz3.a_321",20);
		executeCorruption("tz3.a_322",20);
		executeCorruption("tz3.a_323",20);
		executeCorruption("tz3.a_331",20);
		executeCorruption("tz3.a_332",20);
		executeCorruption("tz3.a_333",20);
		
		executeCorruption("tz3.b_111",5);
		executeCorruption("tz3.b_112",5);
		executeCorruption("tz3.b_113",5);
		executeCorruption("tz3.b_121",5);
		executeCorruption("tz3.b_122",5);
		executeCorruption("tz3.b_123",5);
		executeCorruption("tz3.b_131",5);
		executeCorruption("tz3.b_132",5);
		executeCorruption("tz3.b_133",5);
		
		executeCorruption("tz3.b_211",10);
		executeCorruption("tz3.b_212",10);
		executeCorruption("tz3.b_213",10);
		executeCorruption("tz3.b_221",10);
		executeCorruption("tz3.b_222",10);
		executeCorruption("tz3.b_223",10);
		executeCorruption("tz3.b_231",10);
		executeCorruption("tz3.b_232",10);
		executeCorruption("tz3.b_233",10);*/

		/*executeCorruption("tz3.b_311",20);
		executeCorruption("tz3.b_312",20);
		executeCorruption("tz3.b_313",20);
		executeCorruption("tz3.b_321",20);
		executeCorruption("tz3.b_322",20);
		executeCorruption("tz3.b_323",20);
		executeCorruption("tz3.b_331",20);
		executeCorruption("tz3.b_332",20);
		executeCorruption("tz3.b_333",20);*/
	}
	public static void executeCorruption(String tblSourceTable, int rate){
	
		//String tblSourceTable ="tz3.a_111";
		
		//Connect to the database
		
//		DatabaseConnection dbcRead = new DatabaseConnection("jdbc:mysql://140.226.182.109:3306/", main.mysqlDriver, "pprl", "ongt", "toor3");
//		DatabaseConnection dbcWrite = new DatabaseConnection("jdbc:mysql://140.226.182.109:3306/", main.mysqlDriver, "pprl", "ongt", "toor3");

		DatabaseConnection dbcRead = new DatabaseConnection("jdbc:postgresql://localhost:5432/", main.mysqlDriver, "rosita", "rosita", "password");
		DatabaseConnection dbcWrite = new DatabaseConnection("jdbc:postgresql://localhost:5432/", main.mysqlDriver, "rosita", "rosita", "password");
		ResultSet sqlResults = dbcRead.getTableQuery("SELECT * FROM "+tblSourceTable);
		
		int intCount= 0;
		String strCache ="";
		
		String[][] dataRow; int count = 0;
		while ((dataRow = dbcRead.getNextResultWithColName(sqlResults)) != null) {
			
			String strUpdateCmd = "UPDATE "+tblSourceTable+" SET ";
			String strSnn_pk="";
			
			for(int i=0;i<dataRow[0].length;i++){
				if(dataRow[0][i]!=null){
					if(dataRow[0][i].equals("id")){
						strSnn_pk = dataRow[1][i];
						if(strSnn_pk.equals("7162")){
							int jj = 0 ;
							jj++;
						}
					}
				}
			}
			//for(int i=0;i<dataRow[0].length;i++){
			for(int i=0;i<16;i++){
				String strValue = "";
				int intType = 0;
				int intProp = rate;
				Boolean shouldbeCorrupted = false;
				
				if(dataRow[0][i].equals("first") || dataRow[0][i].equals("last") || dataRow[0][i].equals("city") || dataRow[0][i].equals("county") || dataRow[0][i].equals("street")||dataRow[0][i].equals("email")){
					shouldbeCorrupted = true;
				}else if(dataRow[0][i].equals("zip") || dataRow[0][i].equals("ssn") ||dataRow[0][i].equals("phone") ||  dataRow[0][i].equals("acct") || dataRow[0][i].equals("ssn4")  || dataRow[0][i].equals("expr_date")){
					intType = 1;
					shouldbeCorrupted = true;
				}else if(dataRow[0][i].equals("mi") || dataRow[0][i].equals("country") || dataRow[0][i].equals("card")){
					intType = 3;
					//intProp = 5;
					shouldbeCorrupted = true;
				}else if(dataRow[0][i].equals("sex")){
					intType = 2;
					//intProp = 5;
					shouldbeCorrupted = true;
				}else if(dataRow[0][i].equals("bdate")){
					intType = 4;
					//intProp = 5;
					shouldbeCorrupted = true;
				}else if (dataRow[0][i].equals("s_mob")||dataRow[0][i].equals("s_dob")||dataRow[0][i].equals("s_yob")){
					intType = 1;
					shouldbeCorrupted = true;
				}
				else if(dataRow[0][i].equals("id")){
					shouldbeCorrupted = false;
				}
				
				if(shouldbeCorrupted==true){
					strValue = corrupt(dataRow[1][i], intType, intProp).strCorruptedString;
					
					String strWasCorrupted = "FALSE";
					
					if(!dataRow[1][i].toUpperCase().equals(strValue.toUpperCase())){
						strWasCorrupted = "TRUE";
					}
					
					//strUpdateCmd += dataRow[0][i]+"='"+validateString(strValue)+"',  "+dataRow[0][i]+"_corrupt="+strWasCorrupted+",";
					strUpdateCmd += dataRow[0][i]+"='"+validateString(strValue)+"',";
				}
			}
			
			strUpdateCmd = strUpdateCmd.substring(0, strUpdateCmd.length()-1);
			
			strUpdateCmd += " WHERE id='"+strSnn_pk+"';";
			
			if(intCount<1000){
				intCount++;
				strCache += strUpdateCmd;
			}else{
				dbcWrite.executeQuery(strCache);
				intCount = 0;
				strCache ="";
			}
		}
		
		if(intCount>0){
			dbcWrite.executeQuery(strCache);
		}
	}
	
	static String validateString(String parStr){
		String result = parStr;
		
		result = result.replace("'", "''").trim();
		
		return result;
	}
	
	/**
	 * 
	 * @param parStr - The string to corrupt
	 * @param intType - There are two types 0: normal, 1: ssn
	 * @param parProp - the probability that the string is corrupted
	 * @return
	 */
	private static CorruptedResult corrupt(String parStr, int intType, int parProp){
		CorruptedResult result = new CorruptedResult(parStr, 0);
		
		Random rnd = new Random();
		int x = rnd.nextInt(100);
		if( x < parProp){
			//Type = 0: Text
			//Type = 1: Number 
			//Type = 2: Gender
			//Type = 3: Substitute only 
			if(intType == 0){
				int y = rnd.nextInt(4);
				if(y==0){ result.strCorruptedString = corrupter.createTypoInsertError(parStr); result.intType = y + 1; }
				else if (y==1){ result.strCorruptedString = corrupter.createTypoDeleteError(parStr); result.intType = y + 1;}
				else if (y==2){ result.strCorruptedString = corrupter.createTypoSubstituteError(parStr); result.intType = y + 1;}
				else if (y==3){ result.strCorruptedString = corrupter.createTypoTransposeError(parStr); result.intType = y + 1;}
			}else if(intType == 1){
				result.strCorruptedString = corrupter.createNumberError(parStr);
				result.intType = 1;
			}else if(intType == 2){
				result.strCorruptedString = corrupter.createGenderError(parStr);
			}else if(intType ==3){
				result.strCorruptedString = corrupter.createTypoSubstituteError(parStr);
			}else if(intType ==4){
				
				boolean isCorrectDate = false;
				
				while(!isCorrectDate){
					result.strCorruptedString = corrupter.createNumberError(parStr);
					try {
						DateFormat testDate = new SimpleDateFormat("yyyy-MM-dd");
						testDate.setLenient(false);
						testDate.parse(result.strCorruptedString);
						//testDate.parse("1999-36-13");
						isCorrectDate = true;
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
		}
		
		return result;
	}
	
	private void runSingleTests(){
		String testString= "love";
		String testSSNString = "111-11-1111";
		System.out.println("Insert     : " + corrupter.createTypoInsertError(testString));
		System.out.println("Delete     : " + corrupter.createTypoDeleteError(testString));
		System.out.println("Substitute : " + corrupter.createTypoSubstituteError(testString));
		System.out.println("Transpose  : " + corrupter.createTypoTransposeError(testString));
		System.out.println("SSN        : " + corrupter.createNumberError(testSSNString));
	}

}
