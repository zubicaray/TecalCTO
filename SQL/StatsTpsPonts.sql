/****** Script de la commande SelectTopNRows � partir de SSMS  ******/
SELECT TOP (1000) [depart],Z1.CodeZone
      ,[arrivee],Z2.CodeZone
      ,[lent]
      ,[normal]
      ,[rapide]
  FROM [ANODISATION_SECOURS].[dbo].[TempsDeplacements] T,
  [ANODISATION_SECOURS].[dbo].[Zones] Z1,
  [ANODISATION_SECOURS].[dbo].[Zones] Z2
  WHERE
  Z1.NumZone=T.depart and Z2.numzone=T.arrivee
  
select *  from DetailsGammesAnodisation where NumGamme='000697';

select CONVERT(varchar,DateFinEgouttage,126) from [DetailsFichesProduction] where NumFicheProduction='00084814';
------------------------------------------------------------------------
select * from DetailsChargesProduction where NumFicheProduction like '00083300'
select * from DetailsGammesProduction where NumFicheProduction like '00083300'


select *from [DetailsChargesProduction]  
where numcommandeinterne in ('554384','554392','555180',' 555342','553523')
order by numcommandeinterne


select *from [DetailsChargesProduction] 
-- where  numcommandeinterne in ('553523')

order by DateEntreeEnLigne desc
------------------------------------------------------------------------

update  [ANODISATION_SECOURS].[dbo].[TempsDeplacements] set normal = 51 where depart=20 and arrivee= 27

SELECT
	D1.NumGamme,
    D1.NumLigne,D1.NumZone as N1,D2.NumZone as N2,T.normal
    
  FROM 
	[DetailsGammesAnodisation] D1
	INNER JOIN  [DetailsGammesAnodisation] D2
	ON D1.NumGamme=D2.NumGamme and D1.NumLigne=D2.NumLigne-1
	INNER JOIN TempsDeplacements T
	ON T.depart=D1.NumZone and T.arrivee=D2.NumZone-- and T.normal=0
	where D1.numgamme='000805'
  order by D1.NumGamme,D1.NumLigne
 

 ------------------------------------------------------------------------
update zones set derive=0 where derive is null

update zones set securiteponts=0 
update zones set securiteponts= 1 where numzone in(12,13,14,16,17) select * from zones


select F1.NumFicheProduction,F1.NumLigne,--F1.NumPoste N1,
	P1.NomPoste M1,--F2.NumPoste N2,
	P2.NomPoste M2,
	F1.DateSortiePoste, F2.DateEntreePoste,
	DP.TempsEgouttageSecondes	as tpsEgout,TPS.normal,
	DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes as reel,
	abs(DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes - TPS.normal) as diff
from 
	DetailsFichesProduction  F1
	INNER JOIN DetailsFichesProduction  F2
	ON F1.NumFicheProduction =F2.NumFicheProduction
		AND F1.NumLigne=F2.NumLigne-1
	INNER JOIN POSTES P1
		on P1.Numposte=F1.Numposte
	INNER JOIN POSTES P2
		on P2.Numposte=F2.Numposte
	INNER JOIN DetailsGammesProduction DP
	ON F1.NumFicheProduction =DP.NumFicheProduction and 
		F1.NumLigne=DP.NumLigne
	INNER JOIN DetailsGammesProduction DP2
	ON F2.NumFicheProduction =DP2.NumFicheProduction and 
		F2.NumLigne=DP2.NumLigne
	INNER JOIN [TempsDeplacements] TPS
	ON TPS.depart=DP.NumZone and TPS.arrivee=DP2.NumZone

	where F1.NumFicheProduction  in ('00085305')  --in  (select NumFicheProduction from CalibrageTempsGammes ) 
	
order by F1.NumFicheProduction,F1.NumLigne


select * 	from 		DetailsFichesProduction  F1 where F1.NumFicheProduction in ('00083081') 
select * 	from 		DetailsGammesProduction  F1 where NumZone=15 F1.NumFicheProduction in ('00083081') 
select * 	from 		DetailsChargesProduction  F1 where F1.NumFicheProduction in ('00083081') 

WITH CTE_Doublons AS (
    SELECT 
       cleprimaire,
        ROW_NUMBER() OVER (PARTITION BY NumFicheProduction,	NumLigne,	NumPoste,	DateEntreePoste ORDER BY cleprimaire) AS rn
    FROM 
        DetailsFichesProduction
)
DELETE FROM DetailsFichesProduction
WHERE rn > 1;


WITH CTE_Doublons AS (
    SELECT 
       cleprimaire,
        ROW_NUMBER() OVER (PARTITION BY NumFicheProduction,	NumLigne,	NumPoste,	DateEntreePoste ORDER BY cleprimaire) AS rn
    FROM 
                DetailsFichesProduction

)
-- Supprimer les doublons
DELETE FROM         DetailsFichesProduction
WHERE cleprimaire IN (
    SELECT cleprimaire
    FROM CTE_Doublons
    WHERE rn > 1
);






