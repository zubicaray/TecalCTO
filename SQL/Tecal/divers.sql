use ANODISATION;


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

select STRING_AGG(numgammeanodisation, '","') WITHIN GROUP (ORDER BY numgammeanodisation) 
       AS result
FROM ANODISATION.dbo.DetailsChargesProduction
    WHERE  numligne=1 and numficheproduction 
    in 
    ('00085171','00085172','00085173'),
    '00085174','00085175','00085176','00085177','00085178','00085179','00085180',
    '00085181','00085182','00085183','00085184','00085185','00085186','00085187','00085188','00085189',
    '00085191','00085192','00085193','00085194','00085195','00085196','00085197','00085198','00085190',    
    '00085199','00085200','00085201','00085202','00085203','00085204','00085205','00085206','00085207',
    '00085208','00085209','00085210','00085211','00085212','00085213','00085214','00085215','00085216')   
;

select 
    DF.numficheproduction,DG.NumZone,DG.TempsAuPosteSecondes ,Z.derive
from   
    [DetailsGammesProduction]  DG 
    RIGHT OUTER JOIN   [DetailsFichesProduction] DF 
    on   	DG.numficheproduction=DF.numficheproduction  
    COLLATE FRENCH_CI_AS  and 	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste  
    INNER JOIN ZONES  Z on DG.Numzone=Z.Numzone  
  
    where DF.numficheproduction in 
    ('00085171','00085172','00085173',
    '00085174','00085175','00085176','00085177','00085178','00085179','00085180',
    '00085181','00085182','00085183','00085184','00085185','00085186','00085187','00085188','00085189',
    '00085191','00085192','00085193','00085194','00085195','00085196','00085197','00085198','00085190',    
    '00085199','00085200','00085201','00085202','00085203','00085204','00085205','00085206','00085207',
    '00085208','00085209','00085210','00085211','00085212','00085213','00085214','00085215','00085216')    
    order by DF.numficheproduction, DG.NumLigne
