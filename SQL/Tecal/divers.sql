
-- last 15/02/2025 00088762
select max(NumFicheProduction) from ANODISATION.dbo.DetailsFichesProduction

ALTER TABLE [ANODISATION].dbo.DetailsChargesProduction
ALTER COLUMN NbrReparations VARCHAR(50);

ALTER INDEX ALL ON ANODISATION.dbo.DetailsFichesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsGammesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsPhasesProduction REBUILD;
ALTER INDEX ALL ON ANODISATION.dbo.DetailsChargesProduction REBUILD;

truncate  table [ANODISATION].dbo.DetailsGammesProduction;
truncate  table [ANODISATION].dbo.DetailsFichesProduction;
truncate  table [ANODISATION].dbo.DetailsPhasesProduction;
truncate  table [ANODISATION].dbo.DetailsChargesProduction;


select * from  ANODISATION.dbo.DetailsGammesAnodisation
where numgamme='000747' order by numligne

select count(*) from ANODISATION.dbo.DetailsFichesProduction --1707486
where NumFicheProduction  between '00088384' and '00088384'

WITH CTE AS (
    SELECT 
        NumFicheProduction, 
        NumGammeAnodisation,
        ROW_NUMBER() OVER (PARTITION BY NumGammeAnodisation ORDER BY NumFicheProduction) AS RowNum
    FROM ANODISATION.dbo.DetailsChargesProduction
    WHERE NumFicheProduction 
    in  ('00087384','00087385','00087386','00087387','00087388','00087389','00087391','00087392','00087393','00087394',
    '00085171','00085172','00085173','00085174','00085175','00085176','00085177','00085178','00085179','00085180',
    '00085181','00085182','00085183','00085184',    '00085185','00085186','00085187','00085188','00085189','00085190',
    '00085191',    '00085192','00085193','00085194','00085195','00085196','00085197','00085198',    '00085199')
    --, '00085200','00085201','00085202','00085203','00085204','00085205',    '00085206','00085207','00085208',    '00085209','00085210','00085211','00085212',    '00085213','00085214','00085215','00085216')    
   
    --BETWEEN '00087384' AND '00088384'
)
SELECT NumFicheProduction, NumGammeAnodisation
FROM CTE
WHERE RowNum = 1
ORDER BY NumFicheProduction
OFFSET 0 ROWS FETCH NEXT 10 ROWS ONLY;

select numgammeanodisation
FROM ANODISATION.dbo.DetailsChargesProduction
    WHERE  numficheproduction in 
    ('00085171','00085172','00085173')
      --  '00087384','00087385','00087386','00087387','00087388','00087389','00087391','00087392','00087393','00087394',
    --'00085171','00085172','00085173','00085174','00085175','00085176','00085177','00085178','00085179','00085180','00085181','00085182','00085183','00085184',    '00085185','00085186','00085187','00085188','00085189','00085190','00085191',    '00085192','00085193','00085194','00085195','00085196','00085197','00085198',    '00085199','00085200','00085201','00085202','00085203','00085204','00085205',    '00085206','00085207','00085208','00085209','00085210','00085211','00085212',    '00085213','00085214','00085215','00085216')    
    



select 
   Numficheproduction,numzone, tempsaupostesecondes
 from ANODISATION.dbo.DetailsGammesProduction 
 WHERE NumZone=15 and  NumFicheProduction
    in  ('00087384','00087385','00087386','00087387','00087388','00087389','00087391','00087392','00087393','00087394',
    '00085171','00085172','00085173','00085174','00085175','00085176','00085177','00085178','00085179','00085180',
    '00085181','00085182','00085183','00085184',    '00085185','00085186','00085187','00085188','00085189',
    '00085190','00085191',    '00085192','00085193','00085194','00085195','00085196','00085197','00085198',   
     '00085199','00085200','00085201','00085202','00085203','00085204','00085205',    '00085206',
     '00085207','00085208','00085209','00085210','00085211','00085212',    '00085213','00085214','00085215','00085216')    
    
select 
    FORMAT(SUM(TempsAuPosteSecondes) / 3600, '00') + ':' + -- Heures
    FORMAT((SUM(TempsAuPosteSecondes) % 3600) / 60, '00') + ':' + -- Minutes
    FORMAT(SUM(TempsAuPosteSecondes) % 60, '00') AS [HH:mm:ss]
 from ANODISATION.dbo.DetailsGammesProduction 
 WHERE NumZone=15 and  NumFicheProduction
    in  ('00087384','00087385','00087386','00087387','00087388','00087389','00087391','00087392','00087393','00087394',
    '00085171','00085172','00085173','00085174','00085175','00085176','00085177','00085178','00085179','00085180','00085181','00085182','00085183','00085184',    '00085185','00085186','00085187','00085188','00085189','00085190','00085191',    '00085192','00085193','00085194','00085195','00085196','00085197','00085198',    '00085199','00085200','00085201','00085202','00085203','00085204','00085205',    '00085206','00085207','00085208','00085209','00085210','00085211','00085212',    '00085213','00085214','00085215','00085216')    
   
select *
from ANODISATION.dbo.POSTES
select Numzone,codezone,derive ,securitePonts
from ANODISATION.dbo.ZONES  where derive >0 --where Numzone in (13,6,4)

-- ->DEC
update ANODISATION.dbo.TempsDeplacements set normal=48 where depart=1 and arrivee=3
update ANODISATION.dbo.TempsDeplacements set normal=48 where depart=3 and arrivee=1
-- C04
update ANODISATION.dbo.TempsDeplacements set normal=58 where depart=1 and arrivee=6 
update ANODISATION.dbo.TempsDeplacements set normal=58 where depart=6 and arrivee=1



    select * from ANODISATION.dbo.TempsDeplacements  where depart=1 and arrivee=6

select * from ANODISATION.dbo.TempsDeplacements  where depart=6 and arrivee=1


select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee or depart=arrivee )
--42
select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee+1 or depart+1=arrivee )

--45
select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee+2 or depart+2=arrivee )
--46
select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee+3 or depart+3=arrivee )
--49
select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee+4 or depart+4=arrivee )
--53
select AVG(normal) from ANODISATION.dbo.TempsDeplacements  where
normal !=0  and (depart=arrivee+5 or depart+5=arrivee )

--code ?
--*6565/sdA