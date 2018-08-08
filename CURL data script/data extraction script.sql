-- Script to create CURL data file

-- Assumptions:
----- There is only one preferred record ([prefered_record] = 1) per patient in each table
----- Preferred record is the most recent record
----- [phi_id] is a unique identifier which is shareable 

-- Conventions for data export:
-- Use pipe (|) delimiter
-- For each data value, make sure of the following
-- 		No quotations
-- 		No new line character in the data value
-- 		Include header as line 1. The delimiter of the header must match the delimiter of the data.
--		Don’t use NULL or empty string with quotations (e.g. “”) for fields with missing value
--		Use UTF-8 encoding

IF OBJECT_ID('dbo.curl_person', 'U') IS NOT NULL DROP TABLE [dbo].[curl_person]; 

CREATE TABLE [dbo].[curl_person] AS
SELECT 
	p.[master_id]						as id,
	p.[first_name]						as first name,
	p.[middle_name]						as middle_name,
	p.[surname]						as last_name,
	CONVERT(char(10), p.[birth_date],126)	as dob,
	CASE 
		WHEN p.[gender_concept_id] = 8532 THEN 'F' 
		WHEN p.[gender_concept_id] = 8507 THEN 'M'
		ELSE NULL 
	END 								as sex,
	p.[ssn]								as ssn,
	l.[address_line_1]					as street_address,
	l.[city]							as city,
	l.[state]							as state,
	LEFT(l.[postal_code],5)				as zip5,
	p.race_concept_id					as race,
	p.ethnicity_concept_id				as ethnicity,
	t1.value							as home_phone,
	t2.value							as work_phone,
	t3.value							as email,
	NULL 								as pcp_npi
FROM 
	[dbo].[phi_person] p
	LEFT JOIN [dbo].[phi_location] l ON p.[master_id]=l.[master_id]  AND l.[use_concept_id] = 'home' -- (home address)
	LEFT JOIN [dbo].[phi_telecom] t1 ON p.[master_id]=t1.[master_id] AND t1.[system_concept_id] = 'home' -- (home_phone)
	LEFT JOIN [dbo].[phi_telecom] t2 ON p.[master_id]=t2.[master_id] AND t2.[system_concept_id] = 'work' -- (work_phone)
	LEFT JOIN [dbo].[phi_telecom] t3 ON p.[master_id]=t3.[master_id] AND t3.[system_concept_id] = 'email' -- (email)
WHERE 
	l.[prefered_record] = 1 AND
	t1.[prefered_record] = 1 AND
	t2.[prefered_record] = 1 AND
	t3.[prefered_record] = 1;



	
	
