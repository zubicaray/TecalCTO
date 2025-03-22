USE ANODISATION;
---------------------------------------------------------------------------------------------
-- LIENS CPO ET PROD
---------------------------------------------------------------------------------------------
DECLARE @NumFicheProduction VARCHAR(8) = '00088871';

SELECT --l.* 
    d.numligne,l.idzone,l.NumZone,l.descente,l.montee,
    DATEDIFF(SECOND, d.DateEntreePoste, l.entree)-
    DATEDIFF(SECOND, d.DateSortiePoste, l.sortie) as diffReelCPO,
    DATEDIFF(SECOND,d.DateEntreePoste, d.DateSortiePoste) as reel,
    DATEDIFF(SECOND,l.entree, l.sortie) as cpo,
    g.DecompteDuTempsAuPosteReelSecondes


FROM dbo.LOGS_CPO l
JOIN dbo.DetailsFichesProduction d
    ON d.NumFicheProduction = @NumFicheProduction
    AND d.numligne=l.idzone
JOIN dbo.DetailsGammesProduction g
    ON d.NumFicheProduction = g.NumFicheProduction
    AND d.numligne=g.numligne
WHERE 
    label like '%000174' and d.NumLigne>1 and
    l.entree >
        DATEADD(MINUTE, -10, (SELECT MIN(DateEntreePoste) FROM dbo.DetailsFichesProduction WHERE NumFicheProduction = @NumFicheProduction))
        
    AND l.sortie <
        DATEADD(MINUTE, 10, (SELECT MAX(DateSortiePoste) FROM dbo.DetailsFichesProduction WHERE NumFicheProduction = @NumFicheProduction))

ORDER BY l.idzone;

---------------------------------------------------------------------------------------------
-- VISU PROD GUI BDD
---------------------------------------------------------------------------------------------

select 
    distinct  DG.numficheproduction as [N° OF], 	
    DC.NumGammeANodisation as [gamme ],DC.NumBarre as  [barre] ,
    dbo.hasBadCalibrage (DG.numficheproduction) as BAD_CALIB
from   	
    [DetailsGammesProduction]  DG 	
    LEFT OUTER JOIN   [DetailsFichesProduction] DF 	on   		
        DG.numficheproduction=DF.numficheproduction COLLATE FRENCH_CI_AS and 		
        DG.numligne=DF.NumLigne  and DF.NumLigne=1 	
    INNER JOIN 
        ( 
            select 
                distinct numficheproduction,NumGammeANodisation,
                NumBarre from [DetailsChargesProduction] where numligne=1
        ) DC 	
    on   		DC.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS 
   

WHERE
    DF.DateEntreePoste >=  '20250210'  and DF.DateEntreePoste < '20250211'      
order by DG.numficheproduction ,DC.NumBarre
---------------------------------------------------------------------------------------------
-- UPDATE BDD
---------------------------------------------------------------------------------------------
ALTER TABLE dbo.DetailsChargesProduction  ADD vitesse_haut INT NOT NULL DEFAULT(1) 
ALTER TABLE dbo.DetailsChargesProduction  ADD vitesse_bas INT NOT NULL DEFAULT(1) 
ALTER TABLE dbo.DetailsFichesProduction  ADD TempsDeplacement INT NOT NULL DEFAULT(0) 
ALTER TABLE dbo.DetailsFichesProduction  ADD NumPostePrecedent INT NOT NULL DEFAULT(0) 

ALTER TABLE dbo.Postes  ADD NumZone INT NOT NULL DEFAULT(0) 
update P   set  P.NumZone =Z.NumZone
FROM dbo.Postes P
INNER JOIN dbo.Zones Z
ON P.NumPoste between Z.NumPremierPoste and Z.NumDernierPoste

---------------------------------------------------------------------------------------------------
select * from dbo.LOGS_CPO where entree between '20250219' and  '20250220' ;
select * from dbo.DetailsFichesProduction where DateEntreePoste>='20250219';
select * from dbo.DetailsFichesProduction where NumFicheProduction ='00088870';
select * from dbo.LOGS_CPO where label ='37-000183' and idbarre=7 and entree between '20250219' and  '20250220' ;
select * from DetailsChargesProduction where  NumFicheProduction='00088871'

---------------------------------------------------------------------------------------------------

SELECT 
    D.NumPostePrecedent,D.NumPoste,
    D.TempsDeplacement+ dbo.getOffset(C.vitesse_bas, C.vitesse_haut) as TempsDeplacement
FROM ANODISATION.dbo.DetailsFichesProduction D
INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
    on C.NumFicheProduction=D.NumFicheProduction and C.NumLigne=1

WHERE 
D.DateEntreePoste BETWEEN '20250101' AND '2250301'
group by D.NumPostePrecedent,D.NumPoste
ORDER BY D.DateEntreePoste;

WITH VarianceData AS (
SELECT 
      
        P1.NomPoste AS libelleX, 
        P2.NomPoste AS libelleY, 
        STDEV(D.TempsDeplacement+ dbo.getOffset(C.vitesse_bas, C.vitesse_haut)) AS variance_t 
FROM ANODISATION.dbo.DetailsFichesProduction D 
    INNER JOIN ANODISATION.dbo.DetailsChargesProduction C 
        on C.NumFicheProduction=D.NumFicheProduction and C.NumLigne=1
    INNER JOIN ANODISATION.dbo.POSTES P1 ON P1.NumPoste = D.NumPostePrecedent 
    INNER JOIN ANODISATION.dbo.POSTES P2 ON P2.NumPoste = D.NumPoste 
WHERE D.NumPoste not in (1,2,0) and D.NumPostePrecedent not in (0,41,42) 
    and D.TempsDeplacement <60
    and abs(D.NumPostePrecedent-D.NumPoste) <20 AND D.DateEntreePoste BETWEEN  '20250101' AND '2250301'
GROUP BY D.NumPostePrecedent, D.NumPoste, P1.NomPoste, P2.NomPoste 
) 
SELECT TOP 10 * FROM VarianceData ORDER BY variance_t DESC

---------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------




---------------------------------------------------------------------------------------
-- DIFFERENCE DE LIGNES ENTRE FICHE et GAMME  !!!!!!!!!!!!!!!!!!!!!
---------------------------------------------------------------------------------------
SELECT 
    F.NumFicheProduction, F.Nombre_F,G.Nombre_G
FROM
    (
        SELECT 
            F.NumFicheProduction, 
            COUNT(F.NumFicheProduction) AS Nombre_F
        FROM ANODISATION.dbo.DetailsFichesProduction F
        GROUP BY F.NumFicheProduction 
    ) F
    LEFT OUTER JOIN (
        SELECT 
            G.NumFicheProduction,
            COUNT(G.NumFicheProduction) AS Nombre_G
        FROM ANODISATION.dbo.DetailsGammesProduction G
        GROUP BY G.NumFicheProduction
    ) G
    ON F.NumFicheProduction = G.NumFicheProduction

ORDER BY F.NumFicheProduction DESC;

select * from  ANODISATION.dbo.DetailsFichesProduction F 
where NumFicheProduction='00089614' and numposte=26;

select NumLigne,NumPosteReel 
from  ANODISATION.dbo.DetailsGammesProduction F where NumFicheProduction='00089115'
and  NumPosteReel>0;




---------------------------------------------------------------------------------------
-- INIT TPS DEP
---------------------------------------------------------------------------------------
UPDATE ANODISATION.dbo.DetailsFichesProduction 
SET TempsDeplacement =0 ,NumPostePrecedent=0 WHERE NumPoste <3


UPDATE F2
SET F2.TempsDeplacement = DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)
-DATEDIFF(SECOND, F1.DateDebutEgouttage,  F1.DateFinEgouttage),
    F2.NumPostePrecedent = F1.NumPoste
FROM ANODISATION.dbo.DetailsFichesProduction F1
JOIN ANODISATION.dbo.DetailsFichesProduction F2
    ON F1.NumFicheProduction = F2.NumFicheProduction
    AND F1.NumLigne = F2.NumLigne - 1
    AND CONVERT(DATE, F1.DateDebutEgouttage) = CONVERT(DATE, F1.DateFinEgouttage)
    AND CONVERT(DATE, F1.DateSortiePoste) = CONVERT(DATE, F2.DateEntreePoste)
WHERE F2.NumPoste >2 and F2.TempsDeplacement=0