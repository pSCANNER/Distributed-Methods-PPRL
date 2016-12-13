package rosita.linkage.tools;

import java.sql.ResultSet;
import java.util.ArrayList;

import rosita.linkage.MappedPair;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import cdc.components.AbstractDistance;
import cdc.datamodel.DataCell;
import cdc.datamodel.DataColumnDefinition;

public class ImputationRuleGenerator {
	
	private DatabaseConnection dbConnA;
	private DatabaseConnection dbConnB;
	private DatabaseConnection dbConnSaver;
	
	private String strFieldListA = "";
	private String strFieldListB = "";
	
	private String str01FieldListA = "";
	private String str01FieldListB = "";
	
	private String strFieldListTemp = "";
	private String strFieldListImputation = "";
	
	private String strWhereNOTNULLListA = "";
	private String strWhereNOTNULLListB = "";
	
	private String strWhereNULLListA = "";
	private String strWhereNULLListB = "";
	
	private AbstractDistance[] distances;
	
	private String strCmd ="";
	
	private ArrayList<String[][]> sourceADataFull = new ArrayList<String[][]>();
	private ArrayList<String[][]> sourceBDataFull = new ArrayList<String[][]>();
	
	private ArrayList<String[][]> sourceADataMissing = new ArrayList<String[][]>();
	private ArrayList<String[][]> sourceBDataMissing = new ArrayList<String[][]>();
	
	private ArrayList<MappedPair> mappedPairs;
	private XML_Reader xml_reader;
	
	private double LocalThreshold = 100;
	
	private double TotalDataWeights;
	private double TotalMissingWeights;
	
	private String[][] dataRowA;
	private String[][] dataRowB;
	
	private ArrayList<ImputationRule> ImputationRuleSet =new ArrayList<ImputationRule>();
	
	private double[] weights;
	
	public ImputationRuleGenerator(ArrayList<MappedPair> MappedPairs, XML_Reader Xml_reader, AbstractDistance[] Distances, double[] Weights){
		
		distances = Distances;
		mappedPairs = MappedPairs;
		xml_reader = Xml_reader;
		weights = Weights;
	}
	
	public ArrayList<ImputationRule> generate(){
		
		//Use rows with data to generate the rules
		for (int i=0; i<mappedPairs.size(); i++){
			
			
			strFieldListA += mappedPairs.get(i).getColA().trim() + ",";
			str01FieldListA += "IF("+mappedPairs.get(i).getColA().trim() + " IS NULL,'0','1'),";
			strWhereNOTNULLListA += mappedPairs.get(i).getColA().trim() + " Is not null AND ";
			strWhereNULLListA += mappedPairs.get(i).getColA().trim() + " Is null OR ";
			
			strFieldListB += mappedPairs.get(i).getColB().trim() + ",";
			str01FieldListB += "IF("+mappedPairs.get(i).getColB().trim() + " IS NULL,'0','1'),";
			strWhereNOTNULLListB += mappedPairs.get(i).getColB().trim() + " Is not null AND ";
			strWhereNULLListB += mappedPairs.get(i).getColB().trim() + " Is null OR ";
			
			strFieldListTemp += "o_" + mappedPairs.get(i).getColA().trim() +" BOOLEAN ,";
			
			strFieldListImputation += "o_" + mappedPairs.get(i).getColA().trim() +" ,";
		}
		
		str01FieldListA = "CONCAT("+str01FieldListA.substring(0, str01FieldListA.length()-1)+")";
		str01FieldListB = "CONCAT("+str01FieldListB.substring(0, str01FieldListB.length()-1)+")";
		
		strFieldListA = strFieldListA.substring(0, strFieldListA.length()-1);
		strFieldListB = strFieldListB.substring(0, strFieldListB.length()-1);
		
		strWhereNOTNULLListA = strWhereNOTNULLListA.substring(0, strWhereNOTNULLListA.length()-4);
		strWhereNOTNULLListB = strWhereNOTNULLListB.substring(0, strWhereNOTNULLListB.length()-4);
		
		strWhereNULLListA = strWhereNULLListA.substring(0, strWhereNULLListA.length()-3);
		strWhereNULLListB = strWhereNULLListB.substring(0, strWhereNULLListB.length()-3);
		
		strFieldListTemp = strFieldListTemp.substring(0, strFieldListTemp.length()-1);
		strFieldListImputation = strFieldListImputation.substring(0, strFieldListImputation.length()-1);
		
		dbConnA = xml_reader.getDatabaseConnection(xml_reader.SOURCEA);
		dbConnB = xml_reader.getDatabaseConnection(xml_reader.SOURCEB);
		dbConnSaver = xml_reader.getDatabaseConnection(xml_reader.SAVER);
		
		long t1 = System.currentTimeMillis();
		//=================================================================================
		// Setup empty rules
		//=================================================================================
		
		strCmd = "SELECT "+str01FieldListA+" FROM "+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNULLListA+" GROUP BY "+str01FieldListA;
		ResultSet sourceARowsMissing =  dbConnA.getTableQuery("SELECT "+str01FieldListA+" FROM "+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNULLListA+" GROUP BY "+str01FieldListA);
		strCmd = "SELECT "+str01FieldListB+" FROM "+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNULLListB+" GROUP BY "+str01FieldListB;
		ResultSet sourceBRowsMissing =  dbConnB.getTableQuery("SELECT "+str01FieldListB+" FROM "+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNULLListB+" GROUP BY "+str01FieldListB);

		while ((dataRowA = dbConnA.getNextResultWithColName(sourceARowsMissing)) != null) {
			sourceADataMissing.add(dataRowA);
		}
		
		while ((dataRowB = dbConnB.getNextResultWithColName(sourceBRowsMissing)) != null) {
			sourceBDataMissing.add(dataRowB);
		}
		
		for (int i1=0; i1<sourceADataMissing.size();i1++){
			for(int i2=0;i2<sourceBDataMissing.size();i2++){
				ArrayList<Integer> FieldsWithData = new ArrayList<Integer>();
				ArrayList<Integer> FieldsWithMissingData = new ArrayList<Integer>();
				TotalDataWeights = 0;
				TotalMissingWeights = 0;
				
				//build condition list
				String strConditionFull = "";
				String strConditionWithData = "";
				
				for(int j = 0;j<sourceADataMissing.get(i1)[1][0].length();j++){
					if(!sourceADataMissing.get(i1)[1][0].subSequence(j, j+1).equals("0") && !sourceBDataMissing.get(i2)[1][0].toString().substring(j, j+1).equals("0")){
						FieldsWithData.add(j);
						TotalDataWeights += weights[j];
						strConditionWithData += "o_"+mappedPairs.get(j).getColA()+"= TRUE AND ";
						strConditionFull += "o_"+mappedPairs.get(j).getColA()+"= TRUE AND ";
					}else{
						FieldsWithMissingData.add(j);
						TotalMissingWeights += weights[j];
					}
				}
				
				if(strConditionWithData.trim().length()>0)
				{
					strConditionWithData = strConditionWithData.substring(0, strConditionWithData.length()-4);
					
					if(FieldsWithMissingData.size()>0){
						for (int j=0; j<FieldsWithMissingData.size();j++){
							
							strConditionFull = strConditionWithData+" AND ";
							
							
							ImputationRule myRule = new ImputationRule();
							myRule.FieldsWithData = FieldsWithData;
							myRule.FieldWithMissingData = FieldsWithMissingData.get(j);
							
							Boolean isExisting = false;
							for (int k=0;k<ImputationRuleSet.size();k++){
								if(compareImputationRules(myRule, ImputationRuleSet.get(k))){
									isExisting = true;
								}
							}
							
							if(isExisting==false){
								if(TotalMissingWeights<TotalDataWeights){
									myRule.ImputedValue = -1;
								}else{
									myRule.ImputedValue = 0;
								}
								ImputationRuleSet.add(myRule);
								myRule.printContent();
							}
						}
					}
				}
			}
		}

		ResultSet sourceARowsFull =  dbConnA.getTableQuery("SELECT "+strFieldListA+" FROM "+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNOTNULLListA);
		ResultSet sourceBRowsFull =  dbConnB.getTableQuery("SELECT "+strFieldListB+" FROM "+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNOTNULLListB);
		
		//The size of each chunk should be 1,000,000 pairs or less
		//Set different chunk
		
		while ((dataRowA = dbConnA.getNextResultWithColName(sourceARowsFull)) != null) {
			sourceADataFull.add(dataRowA);
		}
		while ((dataRowB = dbConnB.getNextResultWithColName(sourceBRowsFull)) != null) {
			sourceBDataFull.add(dataRowB);
		}
		
		
		
		//Make sure that all the views are dropped
		/*if(dbConnSaver.checkTableExists("TblImputation_temp")){
			strCmd = "Drop Table TblImputation_temp";
			dbConnSaver.executeActionQuery(strCmd);
		}
		
		//Create temp table
		strCmd = "Create table TblImputation_temp("+strFieldListTemp+")";
		dbConnSaver.executeActionQuery(strCmd);*/
		
		
		
		for (int i1=0; i1<sourceADataFull.size();i1++){
			for(int i2=0;i2<sourceBDataFull.size();i2++){
				
				String strValues ="";
				
				double EligibleMissing = 0;
				double EligibleData = 0;
				
				String strMatch = "";
				String strNonMatch = "";
				
				for(int k=0;k<sourceADataFull.get(i1)[1].length;k++){
					
					DataCell dtaCellA = new DataCell(DataColumnDefinition.TYPE_STRING, sourceADataFull.get(i1)[1][k]);
					DataCell dtaCellB = new DataCell(DataColumnDefinition.TYPE_STRING, sourceBDataFull.get(i2)[1][k]);
					
					if (dtaCellA.getValue().toString().equals("1992-02-05")&&dtaCellB.getValue().toString().equals("1992-04-03")){
						int hhhh= 0;
						hhhh++;
					}
					ImputationRule  tempRule= new ImputationRule();

					if(distances[k].distance(dtaCellA, dtaCellB) > LocalThreshold){
						strMatch += k + "|";
						//If two records have at least one matching field, their distance will be saved
						EligibleData += weights[k];
					}else{
						strNonMatch += k + "|";
						EligibleMissing +=weights[k];
					}
				}
				
				//Store all matching status of the fields into a table
				if(EligibleData>EligibleMissing){
					for (int t=0;t<ImputationRuleSet.size();t++){
						if(ImputationRuleSet.get(t).ImputedValue==-1){
							boolean isQualified = true;
							for(int w=0; w<ImputationRuleSet.get(t).FieldsWithData.size();w++){
								if(!strMatch.contains(ImputationRuleSet.get(t).FieldsWithData.get(w)+"|")){
									isQualified = false;
								}
							}
							
							if(isQualified==true){
								if(strMatch.contains(ImputationRuleSet.get(t).FieldWithMissingData+"|")){
									ImputationRuleSet.get(t).intUp++;
								}
								ImputationRuleSet.get(t).intDown++;
							}
						}
					}
				}
			}
		}
		
		System.out.println("==============================================");
		
		for (int t=0;t<ImputationRuleSet.size();t++){
			if(ImputationRuleSet.get(t).ImputedValue==-1){
				if((double)ImputationRuleSet.get(t).intUp/ImputationRuleSet.get(t).intDown>0.99){
					ImputationRuleSet.get(t).ImputedValue = 100;
				}else{
					ImputationRuleSet.get(t).ImputedValue = 0;
				}
			}
			ImputationRuleSet.get(t).printContent();
		}
		
		System.out.println("Run-time: "+(System.currentTimeMillis()-t1));
		
		return ImputationRuleSet;
	}
	
	public Boolean compareImputationRules(ImputationRule rule1, ImputationRule rule2){
		Boolean result = true;
		
		for (int i:rule2.FieldsWithData){
			if(!rule1.FieldsWithData.contains(i)){
				result = false;
			}
		}

		for (int i:rule1.FieldsWithData){
			if(!rule2.FieldsWithData.contains(i)){
				result = false;
			}
		}
		
		if(rule1.FieldWithMissingData!=rule2.FieldWithMissingData){
			result = false;
		}

		return result;
	}
}
