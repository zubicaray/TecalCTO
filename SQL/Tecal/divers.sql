
select max(NumFicheProduction) from ANODISATION.dbo.DetailsFichesProduction

ALTER INDEX ALL ON ANODISATION.dbo.DetailsFichesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsGammesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsPhasesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsChargesProduction REBUILD;

truncate  table [ANODISATION].dbo.DetailsGammesProduction;
truncate  table [ANODISATION].dbo.DetailsFichesProduction;
truncate  table [ANODISATION].dbo.DetailsPhasesProduction;
truncate  table [ANODISATION].dbo.DetailsChargesProduction;

select count(*) from ANODISATION.dbo.DetailsFichesProduction --1707486
where NumFicheProduction  between '00088384' and '00088384'
delete from ANODISATION.dbo.LOGS_CPO; 
select * from ANODISATION.dbo.LOGS_CPO order by date_log desc

select * from ANODISATION.dbo.Premisses where NumPosteDepart=18
--C13-TEMPO-1-AARRETRED-18-DEAC-NH -TEMPO_EGOUT-0-C17-TEMPO_STAB-1-NB -MOAC-FCY	
--18-	260	 -1-2020     -18-610 -215-270        -0-22 -280       -1-201-600 -8000-- Write your own SQL object definition here, and it'll be included in your package.


select Numzone,codezone from ANODISATION.dbo.ZONES where Numzone in (13,6,4)

--code ?
--*6565/sdA

select NumFicheProduction,NumGammeAnodisation  from  [ANODISATION].dbo.DetailsChargesProduction where NumFicheProduction in(
'00085171','00085172','00085173','00085174','00085175',
'00085176','00085177','00085178','00085179','00085180')

