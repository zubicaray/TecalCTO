USE ANODISATION;
select distinct F1.NumLigne,--F1.NumPoste N1,
P1.NomPoste M1,--F2.NumPoste N2,
P2.NomPoste M2,DP.TempsEgouttageSecondes as tpsEgout,
DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes as reel,
TPS.normal,TPS.normal+17 as lentAttendu,
DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes-TPS.normal-17 as ecart
from
	ANODISATION.dbo.DetailsFichesProduction  F1
	INNER JOIN ANODISATION.dbo.DetailsFichesProduction  F2
	ON F1.NumFicheProduction =F2.NumFicheProduction
	AND F1.NumLigne=F2.NumLigne-1
	INNER JOIN ANODISATION.dbo.POSTES P1
	on P1.Numposte=F1.Numposte
	INNER JOIN ANODISATION.dbo.POSTES P2
	on P2.Numposte=F2.Numposte
	INNER JOIN ANODISATION.dbo.DetailsGammesProduction DP
	ON F1.NumFicheProduction =DP.NumFicheProduction and
	F1.NumLigne=DP.NumLigne
	INNER JOIN ANODISATION.dbo.DetailsGammesProduction DP2
	ON F2.NumFicheProduction =DP2.NumFicheProduction and
	F2.NumLigne=DP2.NumLigne
	LEFT OUTER JOIN ANODISATION.dbo.[TempsDeplacements] TPS
	ON TPS.depart=DP.NumZone and TPS.arrivee=DP2.NumZone

where F1.NumFicheProduction  in ('00088362') --  slow M et norm  D -,'00088343','00088384'

order by numligne
 
select * from ANODISATION.dbo.DetailsChargesProduction where NumFicheProduction in ('00088384','00088343','00088362')