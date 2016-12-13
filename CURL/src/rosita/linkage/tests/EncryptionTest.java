package rosita.linkage.tests;

import rosita.linkage.FRILLinker;
import rosita.linkage.analysis.DBMS;
import rosita.linkage.filtering.DatabaseEncryptor;
import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.io.XML_Reader;
import rosita.linkage.util.MappingConfig;

public class EncryptionTest 
{
	public static void main(String args[]){
		
		String str_enc_file = "";
		
		for(int i =0; i<args.length; i++){
			if (args[i].split("\\=")[0].toLowerCase().equals("file")){
				str_enc_file = args[i].split("\\=")[1].trim();
			}
		}
		executeEncryption(str_enc_file);
		
		
		//executeEncryption("cfg/jamia/a_111.xml");
		//executeEncryption("cfg/jamia/a_112.xml");
		//executeEncryption("cfg/jamia/a_113.xml");
		/*executeEncryption("cfg/jamia/a_121.xml");
		executeEncryption("cfg/jamia/a_122.xml");
		executeEncryption("cfg/jamia/a_123.xml");
		executeEncryption("cfg/jamia/a_131.xml");
		executeEncryption("cfg/jamia/a_132.xml");
		executeEncryption("cfg/jamia/a_133.xml");

		executeEncryption("cfg/jamia/a_211.xml");
		executeEncryption("cfg/jamia/a_212.xml");
		executeEncryption("cfg/jamia/a_213.xml");
		executeEncryption("cfg/jamia/a_221.xml");
		executeEncryption("cfg/jamia/a_222.xml");
		executeEncryption("cfg/jamia/a_223.xml");
		executeEncryption("cfg/jamia/a_231.xml");
		executeEncryption("cfg/jamia/a_232.xml");
		executeEncryption("cfg/jamia/a_233.xml");

		executeEncryption("cfg/jamia/a_311.xml");
		executeEncryption("cfg/jamia/a_312.xml");
		executeEncryption("cfg/jamia/a_313.xml");
		executeEncryption("cfg/jamia/a_321.xml");
		executeEncryption("cfg/jamia/a_322.xml");
		executeEncryption("cfg/jamia/a_323.xml");
		executeEncryption("cfg/jamia/a_331.xml");
		executeEncryption("cfg/jamia/a_332.xml");
		executeEncryption("cfg/jamia/a_333.xml");

		executeEncryption("cfg/jamia/b_111.xml");
		executeEncryption("cfg/jamia/b_112.xml");
		executeEncryption("cfg/jamia/b_113.xml");
		executeEncryption("cfg/jamia/b_121.xml");
		executeEncryption("cfg/jamia/b_122.xml");
		executeEncryption("cfg/jamia/b_123.xml");
		executeEncryption("cfg/jamia/b_131.xml");
		executeEncryption("cfg/jamia/b_132.xml");
		executeEncryption("cfg/jamia/b_133.xml");

		executeEncryption("cfg/jamia/b_211.xml");
		executeEncryption("cfg/jamia/b_212.xml");
		executeEncryption("cfg/jamia/b_213.xml");
		executeEncryption("cfg/jamia/b_221.xml");
		executeEncryption("cfg/jamia/b_222.xml");
		executeEncryption("cfg/jamia/b_223.xml");
		executeEncryption("cfg/jamia/b_231.xml");
		executeEncryption("cfg/jamia/b_232.xml");
		executeEncryption("cfg/jamia/b_233.xml");

		executeEncryption("cfg/jamia/b_311.xml");
		executeEncryption("cfg/jamia/b_312.xml");
		executeEncryption("cfg/jamia/b_313.xml");
		executeEncryption("cfg/jamia/b_321.xml");
		executeEncryption("cfg/jamia/b_322.xml");
		executeEncryption("cfg/jamia/b_323.xml");
		executeEncryption("cfg/jamia/b_331.xml");
		executeEncryption("cfg/jamia/b_332.xml");
		executeEncryption("cfg/jamia/b_333.xml");*/
	}
	
	
	public static void executeEncryption(String str_enc_file)
	{
		//
		
		if(str_enc_file.length()==0){
			System.out.print("Encryption configuration file is missing!!!");
			System.exit(0);
		}
		
		XML_Reader xmlr = new XML_Reader(str_enc_file);

		if(xmlr.getDBMS().equals(DBMS.MySQL)){
			DatabaseConnection readDBC = xmlr.getDatabaseConnection(XML_Reader.READER);
			String readTable = xmlr.getTableName(XML_Reader.READER);
			readTable = str_enc_file.split("/")[str_enc_file.split("/").length-1].split(".")[0];
			String[] readColumnNames = readDBC.getColumnNames(readTable);
	
			DatabaseConnection writeDBC = xmlr.getDatabaseConnection(XML_Reader.WRITER);
			String writeTable = xmlr.getTableName(XML_Reader.WRITER);
			writeTable = "encrypted_"+readTable;
			String[] writeColumnNames = writeDBC.getColumnNames(writeTable);
			MappingConfig mapConfig = new MappingConfig(readTable, readColumnNames, writeTable, writeColumnNames, 
				xmlr.getMappedPairs(), xmlr.getBlockingPair());
			DatabaseEncryptor de = 
					new DatabaseEncryptor(readDBC, writeDBC, mapConfig,xmlr.getDBMS());

			de.setMaxCount(10);
			de.setDoWrite(true);
			de.setOneBlock(false);
			de.setVerbose(true);
			
			de.encryptDB();

		}else if(xmlr.getDBMS().equals(DBMS.PostgreSQL)|| xmlr.getDBMS().equals(DBMS.SQLServer)){
			DatabaseConnection readDBC = xmlr.getDatabaseConnection(XML_Reader.READER);
			String readTable = xmlr.getTableName(XML_Reader.READER);
			readTable = str_enc_file.split("/")[str_enc_file.split("/").length-1].split("\\.")[0];
			String[] readColumnNames = readDBC.getColumnNames(readDBC.getSchema()+"."+readTable);
	
			DatabaseConnection writeDBC = xmlr.getDatabaseConnection(XML_Reader.WRITER);
			String writeTable = xmlr.getTableName(XML_Reader.WRITER);
			writeTable = "encrypted_"+readTable;
			String[] writeColumnNames = writeDBC.getColumnNames(writeDBC.getSchema()+"."+writeTable);
			MappingConfig mapConfig = new MappingConfig(readTable, readColumnNames, writeTable, writeColumnNames, 
				xmlr.getMappedPairs(), xmlr.getBlockingPair());
			DatabaseEncryptor de = 
					new DatabaseEncryptor(readDBC, writeDBC, mapConfig, xmlr.getDBMS());

			de.setMaxCount(10);
			de.setDoWrite(true);
			de.setOneBlock(false);
			de.setVerbose(true);
			
			de.encryptDB();
		}
	}
}
