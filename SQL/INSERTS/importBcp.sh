@echo off
SETLOCAL ENABLEDELAYEDEXPANSION


SET serverName=SRV-APP-ANOD\SQLEXPRESS
SET databaseName=ANODISATION
:: Remplacer par le nom de la base de données
SET username=sa
:: Remplacer par le nom d'utilisateur SQL Server
SET password=Jeff_nenette
SET firstNumfiche=00087661

bcp "select * from ANODISATION.dbo.DetailsFichesProduction where NumFicheProduction > '%firstNumfiche%' COLLATE French_CI_AS" queryout "DetailsFichesProduction.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.DetailsChargesProduction where NumFicheProduction > '%firstNumfiche%' COLLATE French_CI_AS" queryout "DetailsChargesProduction.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.DetailsPhasesProduction where NumFicheProduction > '%firstNumfiche%' COLLATE French_CI_AS" queryout "DetailsPhasesProduction.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.DetailsGammesProduction where NumFicheProduction > '%firstNumfiche%' COLLATE French_CI_AS" queryout "DetailsGammesProduction.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%

bcp "select * from ANODISATION.dbo.GammesAnodisation" queryout "GammesAnodisation.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.DetailsGammesAnodisation" queryout "DetailsGammesAnodisation.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%

bcp "select * from ANODISATION.dbo.CalibrageTempsGammes" queryout "CalibrageTempsGammes.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.LOGS_CPO" queryout "LOGS_CPO.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
bcp "select * from ANODISATION.dbo.TempsDeplacements" queryout "TempsDeplacements.bcp" -n -S "tcp:%serverName%,1433;TrustServerCertificate=yes" -U %username% -P %password%
