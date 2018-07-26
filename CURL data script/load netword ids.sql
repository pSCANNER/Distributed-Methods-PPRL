-- Replace <database_name> with database name
-- Replace <path to network ID file> with local path to network ID file received from CURL Honest Broker
-- DELETE EXISTING 
IF OBJECT_ID('<database_name>.dbo.nwid', 'U') IS NOT NULL DROP TABLE <database_name>.[dbo].[nwid];
CREATE TABLE <database_name>.[dbo].[nwid](
	network_id int NOT NULL,
	local_id int NOT NULL	
);

BULK INSERT <database_name>.[dbo].[nwid]
FROM '<path to network ID file>' 
WITH (
	FIRSTROW = 2,
	FIELDTERMINATOR = ',',
	ROWTERMINATOR = '\n',
	TABLOCK
);