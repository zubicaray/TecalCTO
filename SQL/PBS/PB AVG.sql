

		select 
			Z1.NumZone N1 ,--Z1.LibelleZone M1,
			Z2.NumZone N2,--Z2.LibelleZone M2,
			AVG(
				DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-
				DP1.TempsEgouttageSecondes
			)	as tps
		from 
			DetailsFichesProduction  F1
			INNER JOIN DetailsFichesProduction  F2
			ON F1.NumFicheProduction COLLATE FRENCH_CI_AS =F2.NumFicheProduction  COLLATE FRENCH_CI_AS  
				AND F1.NumLigne=F2.NumLigne-1			and 	F1.DateSortiePoste< F2.DateEntreePoste
			INNER JOIN DetailsGammesProduction DP1
				ON F1.NumFicheProduction COLLATE FRENCH_CI_AS  =DP1.NumFicheProduction  COLLATE FRENCH_CI_AS and F1.NumLigne =DP1.NumLigne
			INNER JOIN DetailsGammesProduction DP2
				ON F2.NumFicheProduction COLLATE FRENCH_CI_AS =DP2.NumFicheProduction COLLATE FRENCH_CI_AS and F2.NumLigne =DP2.NumLigne			
			INNER JOIN ZONES Z1
				on Z1.NumZone=DP1.NumZone
			INNER JOIN ZONES Z2
				on Z2.NumZone=DP2.NumZone
			
	
		where F1.NumFicheProduction COLLATE FRENCH_CI_AS in (select NumFicheProduction COLLATE FRENCH_CI_AS from CalibrageTempsGammes )   
		group by Z1.NumZone  ,Z2.NumZone 
		
	

		select 
			Z1.NumZone N1 ,--Z1.LibelleZone M1,
			Z2.NumZone N2,--Z2.LibelleZone M2,
			F1.NumLigne L1,F2.NumLigne L2,F1.DateSortiePoste DS1, F2.DateEntreePoste DE2, 
			F1.NumFicheProduction,F2.NumFicheProduction ,
			DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste) -
				DP1.TempsEgouttageSecondes	as tps
		from 
			DetailsFichesProduction  F1
			INNER JOIN DetailsFichesProduction  F2
			ON F1.NumFicheProduction COLLATE FRENCH_CI_AS =F2.NumFicheProduction  COLLATE FRENCH_CI_AS  
				AND F1.NumLigne=F2.NumLigne-1	and 	F1.DateSortiePoste< F2.DateEntreePoste
			INNER JOIN DetailsGammesProduction DP1
				ON F1.NumFicheProduction COLLATE FRENCH_CI_AS  =DP1.NumFicheProduction  COLLATE FRENCH_CI_AS and F1.NumLigne =DP1.NumLigne
			INNER JOIN DetailsGammesProduction DP2
				ON F2.NumFicheProduction COLLATE FRENCH_CI_AS =DP2.NumFicheProduction COLLATE FRENCH_CI_AS and F2.NumLigne =DP2.NumLigne			
			INNER JOIN ZONES Z1
				on Z1.NumZone=DP1.NumZone
			INNER JOIN ZONES Z2
				on Z2.NumZone=DP2.NumZone
			
	
		where F1.NumFicheProduction COLLATE FRENCH_CI_AS in (select NumFicheProduction COLLATE FRENCH_CI_AS from CalibrageTempsGammes )   
		
		and Z1.NumZone= 1 and 			Z2.NumZone=7


select * from DetailsFichesProduction where NumFicheProduction='00083583' order by NumLigne





select DF.numficheproduction,P.Nomposte +' - ' + P.LibellePoste,   
DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, '20240326'), 0),DF.DateEntreePoste), 
DATEDIFF(SECOND, DATEADD(DAY, DATEDIFF(DAY, 0, '20240326'), 0),DF.DateSortiePoste)	,DF.NumLigne, DF.Numposte  
from   [DetailsGammesProduction]  DG RIGHT OUTER JOIN   [DetailsFichesProduction] DF on   	DG.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS 
 and 	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste  INNER JOIN POSTES P on P.Numposte=DF.Numposte 
  and DF.numficheproduction in ('00082680','00082681','00082682','00082683','00082684','00082685','00082686','00082687')    
order by DF.numficheproduction, DF.Numposte,DG.NumLigne

select DF.numficheproduction,P.Nomposte +' - ' + P.LibellePoste,   
DATEDIFF(SECOND,  '20240326',DF.DateEntreePoste), 
DATEDIFF(SECOND, '20240326',DF.DateSortiePoste)	,DF.NumLigne, DF.Numposte  
from   [DetailsGammesProduction]  DG RIGHT OUTER JOIN   [DetailsFichesProduction] DF on   	DG.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS 
 and 	DG.numligne=DF.NumLigne and DG.NumPosteReel=DF.NumPoste  INNER JOIN POSTES P on P.Numposte=DF.Numposte 
  and DF.numficheproduction in ('00082680','00082681','00082682','00082683','00082684','00082685','00082686','00082687')    
order by DF.numficheproduction, DF.Numposte,DG.NumLigne