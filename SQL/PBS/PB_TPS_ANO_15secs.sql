
use ANODISATION;
--select * from postes
SELECT 
	F.NumFicheProduction,Z.codezone, DateEntreePoste,--,DateSortiePoste,--numposte,

	--DATEDIFF(SECOND, CAST(DateEntreePoste AS DATE), DateEntreePoste) AS SecondsSinceMidnight,
	DATEDIFF(SECOND, DateEntreePoste,DateSortiePoste) as 'Temps reel',G.TempsAuPosteSecondes as ' temps prevu',
	DATEDIFF(SECOND, DateEntreePoste,DateSortiePoste) - G.TempsAuPosteSecondes as diff
FROM 
	[ANODISATION].[dbo].[DetailsFichesProduction] F	
	INNER JOIN [ANODISATION].[dbo].[Zones] Z
	ON F.numposte between Z.NumPremierPoste and Z.NumDernierPoste and z.numzone>1
	INNER JOIN [ANODISATION].[dbo].[DetailsGammesProduction] G
	ON G.NumFicheProduction=F.NumFicheProduction and G.NumZone=Z.NumZone and G.NumLigne=F.NumLigne

where numposte  in (18,19,20) and -- G.NumFicheProduction > '00084100'
 --DateEntreePoste between '20241016' and '20241017'
 DateEntreePoste > '20241210'
 and DATEDIFF(SECOND, DateEntreePoste,DateSortiePoste) - G.TempsAuPosteSecondes  > 30
ORDER BY F.NumFicheProduction desc
--DATEDIFF(SECOND, DateEntreePoste,DateSortiePoste) - G.TempsAuPosteSecondes desc

select * from DetailsFichesProduction where NumFicheProduction='00086815';
select * from DetailsGammesProduction 
where NumFicheProduction in 
(select NumFicheProduction from DetailsFichesProduction where DateEntreePoste between '20241214' and '20241217')
and ABS(DecompteDuTempsAuPosteReelSecondes)>30;

select distinct DF.numficheproduction,NumGammeAnodisation,DateEntreePoste from  [DetailsChargesProduction] DC INNER JOIN   [DetailsFichesProduction] DF on   	DC.numficheproduction=DF.numficheproduction  
 COLLATE FRENCH_CI_AS  and DF.numficheproduction in 
 ('00084385','00084386','00084387','00084388','00084389','00084390','00084391','00084392','00084393','00084394')  
 order by DateEntreePoste



Update  [TempsDeplacements] 
SET normal=T.tps
FROM 
	[dbo].[TempsDeplacements] TPS
	INNER JOIN (

		select 
			Z1.NumZone N1 ,--Z1.LibelleZone M1,
			Z2.NumZone N2,--Z2.LibelleZone M2,
			DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-
				DP1.TempsEgouttageSecondes	as tps
		from 
			DetailsFichesProduction  F1
			INNER JOIN DetailsFichesProduction  F2
			ON F1.NumFicheProduction =F2.NumFicheProduction
				AND F1.NumLigne=F2.NumLigne-1			
			INNER JOIN ZONES Z1
				on F1.NumPoste between Z1.NumPremierPoste and Z1.NumDernierPoste
			INNER JOIN ZONES Z2
				on F2.NumPoste between Z2.NumPremierPoste and Z2.NumDernierPoste
			INNER JOIN DetailsGammesProduction DP1
				ON F1.NumFicheProduction =DP1.NumFicheProduction and F1.NumLigne =DP1.NumLigne
			INNER JOIN DetailsGammesProduction DP2
				ON F2.NumFicheProduction =DP2.NumFicheProduction and F2.NumLigne =DP2.NumLigne			
			
			
	
		where F1.NumFicheProduction='00083212' 
	)  T
	ON TPS.depart=T.N1 and TPS.arrivee=T.N2 




	
		select 
			Z1.NumZone N1 ,--Z1.LibelleZone M1,
			Z2.NumZone N2,--Z2.LibelleZone M2,
			DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-
				DP1.TempsEgouttageSecondes	as tps
		from 
			DetailsFichesProduction  F1
			INNER JOIN DetailsFichesProduction  F2
			ON F1.NumFicheProduction =F2.NumFicheProduction
				AND F1.NumLigne=F2.NumLigne-1			
			INNER JOIN DetailsGammesProduction DP1
				ON F1.NumFicheProduction =DP1.NumFicheProduction and F1.NumLigne =DP1.NumLigne
			INNER JOIN DetailsGammesProduction DP2
				ON F2.NumFicheProduction =DP2.NumFicheProduction and F2.NumLigne =DP2.NumLigne			
			INNER JOIN ZONES Z1
				on Z1.NumZone=DP1.NumZone
			INNER JOIN ZONES Z2
				on Z2.NumZone=DP2.NumZone
			
	
		where F1.NumFicheProduction='00083212' 



select * 	from 		DetailsGammesProduction  F1 where F1.NumFicheProduction in ('00083657') order by NumLigne
select * 	from 		DetailsFichesProduction  F1 where F1.NumFicheProduction in ('00083657') order by NumLigne






select --F1.NumFicheProduction,
		F1.NumLigne,P1.NomPoste M1,P2.NomPoste M2,
		DATEDIFF(SECOND,  F1.DateEntreePoste,F1.DateSortiePoste) as dureReel,
		DP.TempsAuPosteSecondes as dureePrevue,
		F1.DateSortiePoste, F2.DateEntreePoste,
		DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes as TpsDeplacement,
		DP.TempsEgouttageSecondes	as tpsEgouttage,
		TD.normal as TpsDepTheorique
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
		INNER JOIN TempsDeplacements TD
		ON TD.depart=DP.NumZone and TD.arrivee=DP2.NumZone

		where F1.NumFicheProduction in ('00085143')--,'00085187') --in (select NumFicheProduction from CalibrageTempsGammes ) 
	
	order by F1.NumFicheProduction,F1.NumLigne