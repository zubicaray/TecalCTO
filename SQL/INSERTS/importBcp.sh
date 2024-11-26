bcp "ANODISATION.dbo.DetailsFichesProduction" in "DetailsFichesProduction.bcp" -n -S "tcp:localhost,1433;TrustServerCertificate=yes" -U "sa" -P "Jeff_nenette"
bcp "ANODISATION.dbo.DetailsPhasesProduction" in "DetailsPhasesProduction.bcp" -n -S "tcp:localhost,1433;TrustServerCertificate=yes" -U "sa" -P "Jeff_nenette"
bcp "ANODISATION.dbo.DetailsGammesProduction" in "DetailsGammesProduction.bcp" -n -S "tcp:localhost,1433;TrustServerCertificate=yes" -U "sa" -P "Jeff_nenette"
bcp "ANODISATION.dbo.DetailsChargesProduction" in "DetailsChargesProduction.bcp" -n -S "tcp:localhost,1433;TrustServerCertificate=yes" -U "sa" -P "Jeff_nenette"

