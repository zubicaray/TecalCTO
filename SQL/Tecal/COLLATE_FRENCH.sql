USE ANODISATION;

SELECT TABLE_NAME,COLUMN_NAME, COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME in( 'DetailsPhasesProduction','DetailsChargesProduction','DetailsFichesProduction','DetailsGammesProduction')
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





DROP INDEX DetailsFichesProduction_fiche ON DetailsFichesProduction;
ALTER TABLE DetailsFichesProduction DROP CONSTRAINT DF__DetailsFi__NumFi__0D7A0286;
ALTER TABLE DetailsFichesProduction ALTER COLUMN NumFicheProduction NVARCHAR(8) COLLATE FRENCH_CI_AS;
CREATE INDEX NumFicheProduction ON DetailsFichesProduction (NumFicheProduction);


DROP INDEX DetailsGammesProduction_fiche ON DetailsGammesProduction;
ALTER TABLE [dbo].[DetailsGammesProduction] DROP CONSTRAINT [DF__DetailsGa__NumFi__29221CFB]
ALTER TABLE DetailsGammesProduction ALTER COLUMN NumFicheProduction NVARCHAR(8) COLLATE FRENCH_CI_AS;
CREATE INDEX NumFicheProduction ON DetailsGammesProduction (NumFicheProduction);


DROP INDEX DetailsPhasesProduction_fiche ON DetailsPhasesProduction;
ALTER TABLE [dbo].[DetailsPhasesProduction] DROP CONSTRAINT [DF__DetailsPh__NumFi__32AB8735]
ALTER TABLE [DetailsPhasesProduction] ALTER COLUMN NumFicheProduction NVARCHAR(8) COLLATE FRENCH_CI_AS;
CREATE INDEX NumFicheProduction ON [DetailsPhasesProduction] (NumFicheProduction);

