package rosita.linkage.tools.ir;

import java.sql.ResultSet;
import java.util.ArrayList;

import rosita.linkage.MappedPair;
import rosita.linkage.analysis.SamplingMethod;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.tools.ImputationRule;
import cdc.components.AbstractDistance;
import cdc.configuration.ConfiguredSystem;

public class IRGenerator {
	
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
	
	private SamplingMethod sample;
	
	private double LocalThreshold = 0.9;
	
	private double TotalDataWeights;
	private double TotalMissingWeights;
	
	private String[][] dataRowA;
	private String[][] dataRowB;
	
	private ArrayList<ImputationRule> ImputationRuleSet =new ArrayList<ImputationRule>();
	
	private double[] weights;
	
	private ConfiguredSystem system;
	
	public IRGenerator(ConfiguredSystem parSystem,ArrayList<MappedPair> MappedPairs, XML_Reader Xml_reader, AbstractDistance[] Distances, double[] Weights){
		system = parSystem;
		distances = Distances;
		mappedPairs = MappedPairs;
		xml_reader = Xml_reader;
		weights = Weights;
	}
	
	public ArrayList<ImputationRule> generate(){
		
		//Use rows with data to generate the rules
		for (int i=0; i<mappedPairs.size(); i++){
			
			
			strFieldListA += mappedPairs.get(i).getColA().trim() + ",";
			//str01FieldListA += "IF("+mappedPairs.get(i).getColA().trim() + " IS NULL,'0','1'),";
			str01FieldListA += "CASE WHEN "+mappedPairs.get(i).getColA().trim() + " IS NULL THEN '0' ELSE '1' END,";
			strWhereNOTNULLListA += mappedPairs.get(i).getColA().trim() + " Is not null AND ";
			strWhereNULLListA += mappedPairs.get(i).getColA().trim() + " Is null OR ";
			
			strFieldListB += mappedPairs.get(i).getColB().trim() + ",";
			//str01FieldListB += "IF("+mappedPairs.get(i).getColB().trim() + " IS NULL,'0','1'),";
			str01FieldListB += "CASE WHEN "+mappedPairs.get(i).getColB().trim() + " IS NULL THEN '0' ELSE '1' END,";
			strWhereNOTNULLListB += mappedPairs.get(i).getColB().trim() + " Is not null AND ";
			strWhereNULLListB += mappedPairs.get(i).getColB().trim() + " Is null OR ";
			
			strFieldListTemp += "o_" + mappedPairs.get(i).getColA().trim() +" BOOLEAN ,";
			
			strFieldListImputation += "o_" + mappedPairs.get(i).getColA().trim() +" ,";
		}
		
		//str01FieldListA = "CONCAT("+str01FieldListA.substring(0, str01FieldListA.length()-1)+")";
		//str01FieldListB = "CONCAT("+str01FieldListB.substring(0, str01FieldListB.length()-1)+")";
		String str01TempA = str01FieldListA.substring(0, str01FieldListA.length()-1).replace(",","+");
		String str01TempB = str01FieldListB.substring(0, str01FieldListB.length()-1).replace(",","+");
		
		str01FieldListA = str01TempA;
		str01FieldListB = str01TempB;

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
		
		strCmd = "SELECT "+str01FieldListA+" FROM "+xml_reader.getSchema(xml_reader.SOURCEA)+"."+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNULLListA+" GROUP BY "+str01FieldListA;
		ResultSet sourceARowsMissing =  dbConnA.getTableQuery("SELECT "+str01FieldListA+" FROM "+xml_reader.getSchema(xml_reader.SOURCEA)+"."+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNULLListA+" GROUP BY "+str01FieldListA);
		strCmd = "SELECT "+str01FieldListB+" FROM "+xml_reader.getSchema(xml_reader.SOURCEB)+"."+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNULLListB+" GROUP BY "+str01FieldListB;
		ResultSet sourceBRowsMissing =  dbConnB.getTableQuery("SELECT "+str01FieldListB+" FROM "+xml_reader.getSchema(xml_reader.SOURCEB)+"."+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNULLListB+" GROUP BY "+str01FieldListB);

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

		//Read the XML to decide which method to use
		sample = xml_reader.getSamplingMethod();
		
		if(sample.Method==SamplingMethod.ALL){
			//Start the IR Control which will identify the value of empty rules
			
			IRControl IRControl = new IRControl(system, ImputationRuleSet);
			IRControl.start();
			
			try {
				IRControl.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ImputationRuleSet = IRControl.getImputationRuleSet();
			
			System.out.println("==============================================");
			
			for (int t=0;t<ImputationRuleSet.size();t++){
				if(ImputationRuleSet.get(t).ImputedValue==-1){
					if((double)ImputationRuleSet.get(t).intUp/ImputationRuleSet.get(t).intDown>0.80){
						ImputationRuleSet.get(t).ImputedValue = 100;
					}else{
						ImputationRuleSet.get(t).ImputedValue = 0;
					}
				}
				ImputationRuleSet.get(t).printContent();
			}
		}else{
			IRSampleThread sampleThread = new IRSampleThread(system.getSourceA(), system.getSourceB(), system.getJoin().getJoinCondition(), -1, sample, ImputationRuleSet);
			sampleThread.start();

			try {
				sampleThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ImputationRuleSet = sampleThread.getImputationRules();
			
			for (int t=0;t<ImputationRuleSet.size();t++){
				if(ImputationRuleSet.get(t).ImputedValue==-1){
					if((double)ImputationRuleSet.get(t).intUp/ImputationRuleSet.get(t).intDown>0.8){
						ImputationRuleSet.get(t).ImputedValue = 100;
					}else{
						ImputationRuleSet.get(t).ImputedValue = 0;
					}
				}
				ImputationRuleSet.get(t).printContent();
			}
		}
		
		System.out.println("Run-time: "+(System.currentTimeMillis()-t1));
		
		return ImputationRuleSet;
	}
	
	public static Boolean compareImputationRules(ImputationRule rule1, ImputationRule rule2){
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
