
ALTER INDEX ALL ON ANODISATION.dbo.DetailsFichesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsGammesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsPhasesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsChargesProduction REBUILD;

truncate  table [ANODISATION].dbo.DetailsGammesProduction;
truncate  table [ANODISATION].dbo.DetailsFichesProduction;
truncate  table [ANODISATION].dbo.DetailsPhasesProduction;
truncate  table [ANODISATION].dbo.DetailsChargesProduction;

select count(*) from ANODISATION.dbo.DetailsFichesProduction --1707486
where NumFicheProduction  between '00086810' and '00086775'

select * from ANODISATION.dbo.Premisses where NumPosteDepart=18
--C13-TEMPO-1-AARRETRED-18-DEAC-NH -TEMPO_EGOUT-0-C17-TEMPO_STAB-1-NB -MOAC-FCY	
--18-	260	 -1-2020     -18-610 -215-270        -0-22 -280       -1-201-600 -8000-- Write your own SQL object definition here, and it'll be included in your package.
