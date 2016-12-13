package rosita.linkage.tools;

import rosita.linkage.io.DatabaseConnection;

public class ResultReporter {
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
	public int calculateTN(String parResultTable, String parSourceATable, String parSourceBTable, String parLeftField, String parRightField, DatabaseConnection parDC){
		String strCmd = "";
		int result = -1;

		//Make sure that all the views are dropped
		if(parDC.checkTableExists("AllNegative")){
			strCmd = "Drop View AllNegative";
			parDC.executeActionQuery(strCmd);
		}
		if(parDC.checkTableExists("ClassifiedNegative")){
			strCmd = "Drop View ClassifiedNegative";
			parDC.executeActionQuery(strCmd);
		}
		
		//Create all negative view 
		strCmd = "Create view AllNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parSourceBTable+" ON "+parSourceATable+"."+parLeftField+"="+parSourceBTable+"."+parLeftField+" WHERE ISNULL("+parSourceBTable+"."+parLeftField+")";
		parDC.executeActionQuery(strCmd);
		
		//Create classified negative view
		//strCmd = "Create view ClassifiedNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parResultTable+" ON "+parSourceATable+"."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ISNULL("+parResultTable+".left_"+parLeftField+")";
		//parDC.executeActionQuery(strCmd);
		
		//strCmd = "Select Count(*) From ClassifiedNegative INNER JOIN AllNegative ON ClassifiedNegative."+parLeftField+"=AllNegative."+parLeftField;
		//result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());

	  	strCmd = "Select Count(*) FROM AllNegative LEFT JOIN "+parResultTable+" ON AllNegative."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ISNULL("+parResultTable+".left_"+parLeftField+")";
		result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());
		
	
		strCmd = "Drop View AllNegative";
		parDC.executeActionQuery(strCmd);

		//strCmd = "Drop View ClassifiedNegative";
		//parDC.executeActionQuery(strCmd);
		
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
	public int calculateFN(String parResultTable, String parSourceATable, String parSourceBTable, String parLeftField, String parRightField, DatabaseConnection parDC){
		String strCmd = "";
		int result = -1;

		//Make sure that all the views are dropped
		if(parDC.checkTableExists("AllPositive")){
			strCmd = "Drop View AllPositive";
			parDC.executeActionQuery(strCmd);
		}
		/*if(parDC.checkTableExists("ClassifiedNegative")){
			strCmd = "Drop View ClassifiedNegative";
			parDC.executeActionQuery(strCmd);
		}
		
		//Create all negative view 
		/*strCmd = "Create view AllNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parSourceBTable+" ON "+parSourceATable+"."+parLeftField+"="+parSourceBTable+"."+parLeftField+" WHERE ISNULL("+parSourceBTable+"."+parLeftField+")";
		parDC.executeActionQuery(strCmd);
		
		//Create classified negative view
		strCmd = "Create view ClassifiedNegative as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" LEFT JOIN "+parResultTable+" ON "+parSourceATable+"."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ISNULL("+parResultTable+".left_"+parLeftField+")";
		parDC.executeActionQuery(strCmd);
		
		strCmd = "Select Count(*) From ClassifiedNegative LEFT JOIN AllNegative ON ClassifiedNegative."+parLeftField+"=AllNegative."+parLeftField+" WHERE ISNULL(AllNegative."+parLeftField+")";
		result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());
		
		strCmd = "Drop View AllNegative";
		parDC.executeActionQuery(strCmd);

		strCmd = "Drop View ClassifiedNegative";
		parDC.executeActionQuery(strCmd);*/
		
		
		strCmd = "Create view AllPositive as select "+parSourceATable+"."+parLeftField+" FROM "+parSourceATable+" INNER JOIN "+parSourceBTable+" ON "+parSourceATable+"."+parLeftField+"="+parSourceBTable+"."+parLeftField;
		parDC.executeActionQuery(strCmd);
		
    	strCmd = "Select Count(*) FROM AllPositive LEFT JOIN "+parResultTable+" ON AllPositive."+parLeftField+"="+parResultTable+".left_"+parLeftField+" WHERE ISNULL("+parResultTable+".left_"+parLeftField+")";
		result = Integer.parseInt(parDC.getSingleValueSQL(strCmd).toString());
		
		strCmd = "Drop View AllPositive";
		parDC.executeActionQuery(strCmd);

		
		return result;
	}
}
