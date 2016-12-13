package rosita.linkage;

import rosita.linkage.io.DatabaseConnection;
import rosita.linkage.util.MappingConfig;

public class DatabaseLinker 
{	
	RecordLinker recordLinker = null;
	DatabaseConnection sourceA_DBC = null;
	DatabaseConnection sourceB_DBC = null;
	MappingConfig mapConfig = null;
	
	public DatabaseLinker(DatabaseConnection sourceA, DatabaseConnection sourceB, 
			MappingConfig mapConfig)
	{
		this.sourceA_DBC = sourceA;
		this.sourceB_DBC = sourceB;
		this.mapConfig = mapConfig;
		this.recordLinker = new RecordLinker(mapConfig.getMappedPairs());
	}
	
	public DatabaseLinker(DatabaseConnection sourceA, DatabaseConnection sourceB,
			MappingConfig mapConfig, RecordLinker recordLinker)
	{
		this.sourceA_DBC = sourceA;
		this.sourceB_DBC = sourceB;
		this.mapConfig = mapConfig;
		this.recordLinker = recordLinker;
	}
	
	
	public void linkDB()
	{
		
	}
	
}
