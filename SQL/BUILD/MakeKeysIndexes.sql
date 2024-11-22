create   index IdxDetailsFichesProduction on DetailsFichesProduction(NumFicheProduction);
create   index IdxDateEntreeDetailsFichesProduction on DetailsFichesProduction(DateEntreePoste);
create   index IdxPosteDetailsFichesProduction on DetailsFichesProduction(NumPoste);
create   index IdxNumlignePosteDetailsFichesProduction on DetailsFichesProduction(NumLigne);
ALTER TABLE DetailsFichesProduction ADD PRIMARY KEY (ClePrimaire);
--create unique  index DetailsGammesProduction_num on DetailsGammesProduction(ClePrimaire);

DROP INDEX DetailsFichesProduction_num on DetailsFichesProduction;
DROP INDEX DetailsGammesProduction_num on DetailsGammesProduction;
DROP INDEX DetailsGammesProduction_num on DetailsGammesProduction;

create  index IdxDetailsGammesProduction on DetailsGammesProduction(NumFicheProduction);
create   index IdxZoneDetailsFichesProduction on DetailsGammesProduction(NumZone);
create   index IdxLigneDetailsFichesProduction on DetailsGammesProduction(NumLigne);
ALTER TABLE DetailsGammesProduction ADD PRIMARY KEY (ClePrimaire);

create  index IdxDetailsChargesProduction on DetailsChargesProduction(NumFicheProduction);
ALTER TABLE DetailsChargesProduction ADD PRIMARY KEY (ClePrimaire);

create  index IdxDetailsPhasesProduction on DetailsPhasesProduction(NumFicheProduction);
ALTER TABLE DetailsPhasesProduction ADD PRIMARY KEY (ClePrimaire);


create  index IdxDetailsGammesAnodisation on DetailsGammesAnodisation(NumGamme);
ALTER TABLE DetailsGammesAnodisation ADD PRIMARY KEY (ClePrimaire);



ALTER INDEX ALL ON DetailsFichesProduction REORGANIZE ;
ALTER INDEX ALL ON DetailsGammesProduction REORGANIZE ;