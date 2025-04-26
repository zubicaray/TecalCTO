USE ANODISATION;

SELECT TABLE_NAME,COLUMN_NAME, COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME in( 'DetailsChargesProduction','DetailsFichesProduction','DetailsGammesProduction')
  AND COLUMN_NAME = 'NumFicheProduction';
USE ANODISATION;
SELECT DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS DatabaseCollation;


 ALTER TABLE DetailsFichesProduction DROP CONSTRAINT DF__DetailsFi__NumFi__70D3A237;
 ALTER TABLE DetailsGammesProduction DROP CONSTRAINT DF__DetailsGa__NumFi__16F94B1F;

DROP INDEX NumFicheProduction ON DetailsFichesProduction;
DROP INDEX NumFicheProduction ON DetailsGammesProduction;

ALTER TABLE DetailsFichesProduction ALTER COLUMN NumFicheProduction NVARCHAR(8) 
COLLATE FRENCH_CI_AS;
ALTER TABLE DetailsGammesProduction ALTER COLUMN NumFicheProduction NVARCHAR(8) 
COLLATE FRENCH_CI_AS;

CREATE INDEX NumFicheProduction ON DetailsFichesProduction (NumFicheProduction);
CREATE INDEX NumFicheProduction ON DetailsGammesProduction (NumFicheProduction);


EXEC sp_help 'DetailsFichesProduction';
