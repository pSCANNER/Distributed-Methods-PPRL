package rosita.linkage.tools.cic;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.commons.collections.map.HashedMap;

import rosita.linkage.MappedPair;
import rosita.linkage.analysis.SamplingMethod;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.tools.ImputationRule;
import cdc.components.AbstractDistance;
import cdc.configuration.ConfiguredSystem;

public class CICGenerator {
	
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
	
	private double LocalThreshold = 0.8;
	
	private double TotalDataWeights;
	private double TotalMissingWeights;
	
	private String[][] dataRowA;
	private String[][] dataRowB;
	
	private ArrayList<ImputationRule> ImputationRuleSet =new ArrayList<ImputationRule>();
	
	private double[] weights;
	
	private ConfiguredSystem system;
	
	private ArrayList<MappedPair> mappedPairs_backup;
	
	public CICGenerator(ConfiguredSystem parSystem,ArrayList<MappedPair> MappedPairs, ArrayList<MappedPair> parMappedPairs_backup,XML_Reader Xml_reader){
		system = parSystem;
		//distances = Distances;
		mappedPairs = MappedPairs;
		xml_reader = Xml_reader;
		//weights = Weights;
		mappedPairs_backup = parMappedPairs_backup;
	}
	
	public BackupField[] generate(){
		
		HashedMap result= null;
		
		//Use rows with data to generate the rules
		String strFieldListA = "";
		int MinQuasiSetSize = mappedPairs.size();
		
		for (int i=0; i<mappedPairs.size(); i++){
			strFieldListA += mappedPairs.get(i).getColA().trim() + ",";
			strWhereNULLListA += mappedPairs.get(i).getColA().trim() + " Is null OR ";
		}

		for (int i=0; i<mappedPairs_backup.size(); i++){
			strFieldListA += mappedPairs_backup.get(i).getColA().trim() + ",";
			//strWhereNULLListA += mappedPairs_backup.get(i).getColA().trim() + " Is null OR ";
		}
		
		strFieldListA = strFieldListA.substring(0, strFieldListA.length()-1);
		strWhereNULLListA = strWhereNULLListA.substring(0, strWhereNULLListA.length()-3);
		
		//Create field list
		String strFieldListB = "";
		for (int i=0; i<mappedPairs.size(); i++){
			strFieldListB += mappedPairs.get(i).getColB().trim() + ",";
			strWhereNULLListB += mappedPairs.get(i).getColB().trim() + " Is null OR ";
		}

		for (int i=0; i<mappedPairs_backup.size(); i++){
			strFieldListB += mappedPairs_backup.get(i).getColB().trim() + ",";
			//strWhereNULLListB += mappedPairs_backup.get(i).getColB().trim() + " Is null OR ";
		}

		strFieldListB = strFieldListB.substring(0, strFieldListB.length()-1);
		strWhereNULLListB = strWhereNULLListB.substring(0, strWhereNULLListB.length()-3);
		
		DatabaseConnection dbcA = xml_reader.getDatabaseConnection(xml_reader.SOURCEA);
		DatabaseConnection dbcB = xml_reader.getDatabaseConnection(xml_reader.SOURCEB);
		
		ResultSet sourceARows =  dbcA.getTableQuery("SELECT "+strFieldListA+" FROM "+xml_reader.getSchema(xml_reader.SOURCEA)+"."+xml_reader.getTableName(xml_reader.SOURCEA)+" WHERE "+strWhereNULLListA);
		ResultSet sourceBRows =  dbcB.getTableQuery("SELECT "+strFieldListB+" FROM "+xml_reader.getSchema(xml_reader.SOURCEB)+"."+xml_reader.getTableName(xml_reader.SOURCEB)+" WHERE "+strWhereNULLListB);
		
		
		CICControl myCICControl = new CICControl(null, sourceARows, dbcA, mappedPairs, mappedPairs_backup);
		myCICControl.start();

		try {
			myCICControl.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = myCICControl.getNumbers();
		HashedMap combinedBackupFields = (HashedMap) result.clone();
		BackupField[] SortedAResult = sort(result);
		
		myCICControl = new CICControl(null, sourceBRows, dbcB, mappedPairs, mappedPairs_backup);
		myCICControl.start();

		try {
			myCICControl.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = myCICControl.getNumbers();
		BackupField[] SortedBResult = sort(result);
		
		//Translate B to A
		BackupField[] translatedFieldsB = new BackupField[SortedBResult.length];
		for(int i=0; i<SortedBResult.length; i++){
			String translatedKey = "";
			for(int t=0; t<SortedBResult[i].FieldName.split("[|]").length; t++){
				for (int j=0; j<mappedPairs_backup.size(); j++){
					if(mappedPairs_backup.get(j).getColB().equals(SortedBResult[i].FieldName.split("[|]")[t])){
						translatedKey += mappedPairs_backup.get(j).getColA()+"|";
						break;
					}
				}
			}
			translatedKey = translatedKey.substring(0, translatedKey.length()-1);
			
			translatedFieldsB[i] = new BackupField();
			translatedFieldsB[i].FieldName = translatedKey;
			translatedFieldsB[i].Value = SortedBResult[i].Value;
		}
		
		//Combine the two sources
		for(int i = 0; i<translatedFieldsB.length; i++){
			if(combinedBackupFields.containsKey(translatedFieldsB[i].FieldName)){
				int intCurrentValue = Integer.parseInt(combinedBackupFields.get(translatedFieldsB[i].FieldName).toString()) + translatedFieldsB[i].Value;
				combinedBackupFields.remove(translatedFieldsB[i].FieldName);
				combinedBackupFields.put(translatedFieldsB[i].FieldName, intCurrentValue);
			}else{
				combinedBackupFields.put(translatedFieldsB[i].FieldName, translatedFieldsB[i].Value);
			}
		}
		
		//Sort the combined result
		BackupField[] combinedFields = new BackupField[combinedBackupFields.size()];
		int combinedQty = 0;
		while (!combinedBackupFields.isEmpty()){
			int max = 0;
			String maxKey = "";
			for(int i=0; i<combinedBackupFields.keySet().toArray().length;i++){
				if(Integer.parseInt(combinedBackupFields.get(combinedBackupFields.keySet().toArray()[i].toString()).toString())>max){
					max = Integer.parseInt(combinedBackupFields.get(combinedBackupFields.keySet().toArray()[i].toString()).toString());
					maxKey = combinedBackupFields.keySet().toArray()[i].toString();
				}
			}
			combinedFields[combinedQty] = new BackupField();
			combinedFields[combinedQty].FieldName = maxKey;
			combinedFields[combinedQty].Value = max;
			combinedQty++;
			combinedBackupFields.remove(maxKey);
		}
		
		return combinedFields;
	}
	
	//This method sort the backup fields in a hash map by value
	private BackupField[] sort(HashedMap parHashMap){
		BackupField[] result = new BackupField[parHashMap.size()];
		int SortedQty = 0;
		while (!parHashMap.isEmpty()){
			int max = 0;
			String maxKey = "";
			for(int i=0; i<parHashMap.keySet().toArray().length;i++){
				if(Integer.parseInt(parHashMap.get(parHashMap.keySet().toArray()[i].toString()).toString())>max){
					max = Integer.parseInt(parHashMap.get(parHashMap.keySet().toArray()[i].toString()).toString());
					maxKey = parHashMap.keySet().toArray()[i].toString();
				}
			}
			result[SortedQty] = new BackupField();
			result[SortedQty].FieldName = maxKey;
			result[SortedQty].Value = max;
			SortedQty++;
			parHashMap.remove(maxKey);
		}
		
		return result;
	}
}
