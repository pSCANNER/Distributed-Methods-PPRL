package rosita.linkage.tools;

import rosita.linkage.io.DatabaseConnection;

public class PostgreSQLResultReporter {
	/**
	 * 
	 * @param parResultTable Name of the table which contains the linkage result
	 * @param parLeftField Name of the linking field on the left side
	 * @param parRightField Name of the linking field on the right side
	 * @param parDC Data connection
	 * @return Quantity of true positive cases
	 */
	public int calculateTP(String parResultTable, String parLeftField, String parRightField, DatabaseConnection parDC){
		return Integer.parseInt(parDC.getSingleValueSQL("Select Count(*) FROM "+parResultTable+" WHERE left_"+parLeftField + "= right_"+parRightField).toString());
	}
	
	/**
	 * 
	 * @param parResultTable Name of the table which contains the linkage result
	 * @param parLeftField Name of the linking field on the left side
	 * @param parRightField Name of the linking field on the right side
	 * @param parDC Data connection
	 * @return Quantity of false positive cases
	 */
	public int calculateFP(String parResultTable, String parLeftField, String parRightField, DatabaseConnection parDC){
		
		String strCmd = "Select Count(*) FROM "+parResultTable+" WHERE left_"+parLeftField + "<>right_"+parRightField;
		return Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());
	}
	
	/**
	 * 
	 * @param parResultTable Name of the table which contains the linkage result
	 * @param parSourceATable Name of source A table
	 * @param parLeftField Name of the linking field on the left side
	 * @param parDC Data connection
	 * @return Quantity of true negative cases
	 */
	public int calculateTN(String parResultTable, String parSourceATable, String parSourceBTable, String parLeftField, String parRightField, DatabaseConnection parDC, String parTargetSchema){
		String strCmd = "";
		int result = -1;

		//Make sure that all the views are dropped
		if(parDC.checkPostgreTableExists("AllNegative", parTargetSchema)){
			strCmd = "Drop View AllNegative";
			parDC.executeActionQuery(strCmd);
		}
		if(parDC.checkPostgreTableExists("ClassifiedNegative", parTargetSchema)){
			strCmd = "Drop View ClassifiedNegative";
			parDC.executeActionQuery(strCmd);
		}
		
		//Create all negative view 
		strCmd = "Create view "+parTargetSchema+".AllNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parSourceBTable+" ON "+parSourceATable+"."+parLeftField+"="+parSourceBTable+"."+parLeftField+" WHERE "+parSourceBTable+"."+parLeftField+" IS NULL";
		parDC.executeActionQuery(strCmd);
		
		//Create classified negative view
		strCmd = "Create view "+parTargetSchema+".ClassifiedNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parResultTable+" ON "+parSourceATable+"."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ("+parResultTable+".left_"+parLeftField+") IS NULL";
		parDC.executeActionQuery(strCmd);
		
		strCmd = "Select Count(*) From "+parTargetSchema+".ClassifiedNegative INNER JOIN "+parTargetSchema+".AllNegative ON "+parTargetSchema+".ClassifiedNegative."+parLeftField+"="+parTargetSchema+".AllNegative."+parLeftField;
		result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());

	
		strCmd = "Drop View "+parTargetSchema+".AllNegative";
		parDC.executeActionQuery(strCmd);

		strCmd = "Drop View "+parTargetSchema+".ClassifiedNegative";
		parDC.executeActionQuery(strCmd);
		
		return result;
	}
	
	/**
	 * 
	 * @param parResultTable Name of the table which contains the linkage result
	 * @param parSourceATable Name of source A table
	 * @param parLeftField Name of the linking field on the left side
	 * @param parDC Data connection
	 * @return Quantity of true negative cases
	 */
	public int calculateFN(String parResultTable, String parSourceATable, String parSourceBTable, String parLeftField, String parRightField, DatabaseConnection parDC, String parTargetSchema){
		String strCmd = "";
		int result = -1;

		//Make sure that all the views are dropped
		if(parDC.checkPostgreViewExists("AllNegative", parTargetSchema)){
			strCmd = "Drop View "+parTargetSchema+".AllNegative";
			parDC.executeActionQuery(strCmd);
		}
		if(parDC.checkPostgreViewExists("ClassifiedNegative", parTargetSchema)){
			strCmd = "Drop View "+parTargetSchema+".ClassifiedNegative";
			parDC.executeActionQuery(strCmd);
		}
		
		//Create all negative view 
		strCmd = "Create view "+parTargetSchema+".AllNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parSourceBTable+" ON "+parSourceATable+"."+parLeftField+"="+parSourceBTable+"."+parLeftField+" WHERE ("+parSourceBTable+"."+parLeftField+") IS NULL";
		parDC.executeActionQuery(strCmd);
		
		//Create classified negative view
		strCmd = "Create view "+parTargetSchema+".ClassifiedNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parResultTable+" ON "+parSourceATable+"."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ("+parResultTable+".left_"+parLeftField+") IS NULL";
		parDC.executeActionQuery(strCmd);
		
		strCmd = "Select Count(*) From "+parTargetSchema+".ClassifiedNegative LEFT JOIN "+parTargetSchema+".AllNegative ON "+parTargetSchema+".ClassifiedNegative."+parLeftField+"="+parTargetSchema+".AllNegative."+parLeftField+" WHERE ("+parTargetSchema+".AllNegative."+parLeftField+") IS NULL";
		result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());
		
		strCmd = "Drop View "+parTargetSchema+".AllNegative";
		parDC.executeActionQuery(strCmd);

		strCmd = "Drop View "+parTargetSchema+".ClassifiedNegative";
		parDC.executeActionQuery(strCmd);
		
		return result;
	}
}
