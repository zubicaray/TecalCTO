


select  * from DetailsFichesProduction where NumFicheProduction='00069138'


select  A.NumFicheProduction , F.DateEntreePoste,A.numligne,F.numligne from DetailsGammesProduction A
LEFT OUTER JOIN  DetailsFichesProduction F
on A.NumFicheProduction=F.NumFicheProduction  and A.numligne=F.numligne
where F.numligne is null and  A.numligne >10
order by A.NumFicheProduction desc 