DECLARE @DateDebut DATE = '2024-02-07';
DECLARE @DateFin DATE = '2024-03-07';
DECLARE @poste_1 INTEGER = 14;
DECLARE @poste_2 INTEGER = 15;


SELECT 
    D.TempsDeplacement+VB.valeur+VH.valeur
FROM ANODISATION.dbo.DetailsFichesProduction D
INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
    on C.NumFicheProduction=D.NumFicheProduction and C.NumLigne=1
INNER JOIN vitesse_bas VB  on VB.id=C.vitesse_bas
INNER JOIN vitesse_haut VH on VH.id=C.vitesse_haut
WHERE D.NumPostePrecedent=@poste_1 and D.NumPoste=@poste_2
AND D.DateEntreePoste BETWEEN @DateDebut AND @DateFin
ORDER BY D.DateEntreePoste;


WITH VarianceData AS (
SELECT D.NumPostePrecedent AS X, D.NumPoste AS Y, 
    P1.NomPoste AS libelleX, P2.NomPoste AS libelleY, 
    COUNT(*) AS nb_mouvements, 
    AVG(D.TempsDeplacement+VB.valeur+VH.valeur) AS moyenne_t,
    STDEV(D.TempsDeplacement+VB.valeur+VH.valeur) AS variance_t 
FROM ANODISATION.dbo.DetailsFichesProduction D 
INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
    on C.NumFicheProduction=D.NumFicheProduction and C.NumLigne=1
INNER JOIN vitesse_bas VB on VB.id=C.vitesse_bas
INNER JOIN vitesse_haut VH on VH.id=C.vitesse_haut
INNER JOIN ANODISATION.dbo.POSTES P1 ON P1.NumPoste = D.NumPostePrecedent 
INNER JOIN ANODISATION.dbo.POSTES P2 ON P2.NumPoste = D.NumPoste 
WHERE D.NumPoste not in (1,2,0) and D.NumPostePrecedent not in (0,41,42) 
    and abs(D.NumPostePrecedent-D.NumPoste) <20 
    and D.TempsDeplacement <70
    AND D.DateEntreePoste BETWEEN @DateDebut AND @DateFin
GROUP BY D.NumPostePrecedent, D.NumPoste, P1.NomPoste, P2.NomPoste 
) 
SELECT  * FROM VarianceData ORDER BY variance_t DESC







select 
    P1.NomPoste as libelleX,P2.NomPoste as libelleY,D.NumPostePrecedent as X, 
    D.NumPoste as Y,D.DateEntreePoste as J,D.TempsDeplacement as t
from 
    ANODISATION.dbo.DetailsFichesProduction D
    INNER JOIN ANODISATION.dbo.POSTES P1 
    ON P1.NumPoste =D.NumPostePrecedent
    INNER JOIN ANODISATION.dbo.POSTES P2
    ON P2.NumPoste =D.NumPoste
where 
    D.NumPostePrecedent !=0 AND
    D.DateEntreePoste between @DateDebut AND @DateFin
order by D.DateEntreePoste


select NomPoste as libelle,NumPoste as num from ANODISATION.dbo.Postes


select * from ANODISATION.dbo.vitesse_haut
select * from ANODISATION.dbo.vitesse_bas