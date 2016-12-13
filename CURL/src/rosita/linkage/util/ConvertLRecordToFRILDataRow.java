package rosita.linkage.util;

import rosita.linkage.L_Record;
import cdc.datamodel.DataRow;

// ON HOLD: This class may be not needed because data can be read directly from SQL Server to   

// This class converts L_Record to FRIL datarow.

public class ConvertLRecordToFRILDataRow {
	public ConvertLRecordToFRILDataRow(){
	
	}
	
	/** 
	 * 
	 * 
	 * This function does the conversion from L_Record to FRIL datarow
	 * @param parRecord - The input L_Record
	 * @param parColumnNames - Names of the columns 
	 * @return result - The output FRIL datarow
	 * @author Toan Ong
	 **/
	
	DataRow convert(L_Record parRecord,String[] parColumnNames){
		DataRow result = null;
		
		
			
		return result;
	}
}
