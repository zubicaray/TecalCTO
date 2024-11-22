select distinct F1.NumLigne,--F1.NumPoste N1,
P1.NomPoste M1,--F2.NumPoste N2,
P2.NomPoste M2,DP.TempsEgouttageSecondes as tpsEgout,DATEDIFF(SECOND,  F1.DateSortiePoste,F2.DateEntreePoste)-DP.TempsEgouttageSecondes as reel
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

--where F1.NumFicheProduction  in ('00085305')  -- norm M et D
--where F1.NumFicheProduction  in ('00085307')  -- fast M et  D
--where F1.NumFicheProduction  in ('00085394') -- slow M et slow D
--where F1.NumFicheProduction  in ('00086580 ') -- fast M et norm  D
--where F1.NumFicheProduction  in ('00085211') -- fast  M  et D
--where F1.NumFicheProduction  in ('00086586') -- norm M  et fast  D
--where F1.NumFicheProduction  in ('00086608 ') -- norm M  et slow  D
where F1.NumFicheProduction  in ('00086587') --  slow M et norm  D
--where F1.NumFicheProduction  in ('00085394') -- slow M  et D
--where F1.Num
--gamme SANDEN 776
order by numligne
 
select NumGammeAnodisation from DetailsChargesProduction where NumFicheProduction in ('00085305',
'00085307','00085394','00085312','00085355','00085397','00085656','00085416')