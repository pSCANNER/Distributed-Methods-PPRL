package rosita.linkage.tools;

import java.sql.ResultSet;
import java.util.Random;

import rosita.linkage.main;
import rosita.linkage.io.DatabaseConnection;



public class MissingDataCreator {
	
	public MissingDataCreator(){
		
	}
	
	static String validateString(String parStr){
		String result = parStr;
		
		result = result.replace("'", "''").trim();
		
		return result;
	}
	

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		/*executeMissing("tz3.a_111",5);
		executeMissing("tz3.a_112",5);
		executeMissing("tz3.a_113",5);
		executeMissing("tz3.a_211",5);
		executeMissing("tz3.a_212",5);
		executeMissing("tz3.a_213",5);
		executeMissing("tz3.a_311",5);
		executeMissing("tz3.a_312",5);
		executeMissing("tz3.a_313",5);

		executeMissing("tz3.a_121",10);
		executeMissing("tz3.a_122",10);
		executeMissing("tz3.a_123",10);
		executeMissing("tz3.a_221",10);
		executeMissing("tz3.a_222",10);
		executeMissing("tz3.a_223",10);
		executeMissing("tz3.a_321",10);
		executeMissing("tz3.a_322",10);
		executeMissing("tz3.a_323",10);

		executeMissing("tz3.a_131",20);
		executeMissing("tz3.a_132",20);
		executeMissing("tz3.a_133",20);
		executeMissing("tz3.a_231",20);
		executeMissing("tz3.a_232",20);
		executeMissing("tz3.a_233",20);
		executeMissing("tz3.a_331",20);
		executeMissing("tz3.a_332",20);
		executeMissing("tz3.a_333",20);*/
		
		executeMissing("tz3.b_111",5);
		executeMissing("tz3.b_112",5);
		executeMissing("tz3.b_113",5);
		executeMissing("tz3.b_211",5);
		executeMissing("tz3.b_212",5);
		executeMissing("tz3.b_213",5);
		executeMissing("tz3.b_311",5);
		executeMissing("tz3.b_312",5);
		executeMissing("tz3.b_313",5);

		executeMissing("tz3.b_121",10);
		executeMissing("tz3.b_122",10);
		executeMissing("tz3.b_123",10);
		executeMissing("tz3.b_221",10);
		executeMissing("tz3.b_222",10);
		executeMissing("tz3.b_223",10);
		executeMissing("tz3.b_321",10);
		executeMissing("tz3.b_322",10);
		executeMissing("tz3.b_323",10);

		executeMissing("tz3.b_131",20);
		executeMissing("tz3.b_132",20);
		executeMissing("tz3.b_133",20);
		executeMissing("tz3.b_231",20);
		executeMissing("tz3.b_232",20);
		executeMissing("tz3.b_233",20);
		executeMissing("tz3.b_331",20);
		executeMissing("tz3.b_332",20);
		executeMissing("tz3.b_333",20);
	
	}
	
	public static void executeMissing(String tblSourceTable, int rate){	
		Random rand = new Random();
		//String tblSourceTable ="tz.sourceb5K_15_missing";
		
		//Connect to the database
		
		DatabaseConnection dbcRead = new DatabaseConnection("jdbc:postgresql://localhost:5432/", main.mysqlDriver, "rosita", "rosita", "rosita@2012");
		DatabaseConnection dbcWrite = new DatabaseConnection("jdbc:postgresql://localhost:5432/", main.mysqlDriver, "rosita", "rosita", "rosita@2012");

		ResultSet sqlResults = dbcRead.getTableQuery("SELECT * FROM "+tblSourceTable);

		String[][] dataRow; int count = 0;
		String strUpdateCmd ="";
		
		System.out.println(tblSourceTable+" - Start" );
		
		while ((dataRow = dbcRead.getNextResultWithColName(sqlResults)) != null){
			
			strUpdateCmd += "UPDATE "+tblSourceTable+" SET ";
			String strSnn_pk="";
			
			for(int i=0;i<16;i++){
				if(dataRow[0][i].equals("id")){
					strSnn_pk = dataRow[1][i];
				}
			}
			for(int i=0;i<16;i++){
				if(!dataRow[0][i].equals("id")){
					int rndNum = rand.nextInt(100);
					
					String strValue = "";
					if(rndNum<rate){
						strValue = "NULL ";
					}else{
						strValue = "'"+validateString(dataRow[1][i])+"'";
					}
					
					strUpdateCmd += dataRow[0][i]+"="+strValue+",";
				}
			}
			
			strUpdateCmd = strUpdateCmd.substring(0, strUpdateCmd.length()-1);
			
			strUpdateCmd += " WHERE id='"+strSnn_pk+"';";
			count++;
			
			if(count ==1000){
				dbcWrite.executeQuery(strUpdateCmd);
				count = 0;
				System.out.print(".");
			}
		}
		
		if(count>0){
			dbcWrite.executeQuery(strUpdateCmd);
			count = 0;
		}
		
		System.out.println(tblSourceTable+" - Done" );
	}
}
