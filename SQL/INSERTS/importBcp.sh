bcp "ANODISATION.dbo.DetailsFichesProduction" in "DetailsFichesProduction.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.DetailsPhasesProduction" in "DetailsPhasesProduction.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.DetailsGammesProduction" in "DetailsGammesProduction.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD
bcp "ANODISATION.dbo.DetailsChargesProduction" in "DetailsChargesProduction.bcp" -n -S "tcp:$SERVER,1433;TrustServerCertificate=yes" -U $USER -P $BDD_PWD

