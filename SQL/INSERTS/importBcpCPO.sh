sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -C -Q "TRUNCATE TABLE ANODISATION.dbo.GammesAnodisation"
sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -C -Q "TRUNCATE TABLE ANODISATION.dbo.DetailsGammesAnodisation"
sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -C -Q "TRUNCATE TABLE ANODISATION.dbo.CalibrageTempsGammes"
sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -C -Q "TRUNCATE TABLE ANODISATION.dbo.LOGS_CPO"
sqlcmd -S localhost -U sa -P $BDD_PWD -d ANODISATION -C -Q "TRUNCATE TABLE ANODISATION.dbo.TempsDeplacements"

bcp "ANODISATION.dbo.GammesAnodisation" in "GammesAnodisation.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.DetailsGammesAnodisation" in "DetailsGammesAnodisation.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.CalibrageTempsGammes" in "CalibrageTempsGammes.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.LOGS_CPO" in "LOGS_CPO.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.TempsDeplacements" in "TempsDeplacements.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD

