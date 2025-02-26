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
    distinct  DG.numficheproduction as [N° OF], 	DC.NumGammeANodisation as [gamme ],DC.NumBarre as  [barre] ,
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



select 
    Z1.NumZone N1 ,--Z1.LibelleZone M1,
    Z2.NumZone N2,--Z2.LibelleZone M2,
    DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-DP1.TempsEgouttageSecondes	
    --,    TD.normal-9 as diff
from 
    DetailsFichesProduction  F1
    LEFT OUTER JOIN DetailsFichesProduction  F2
    ON F1.NumFicheProduction =F2.NumFicheProduction
        AND F1.NumLigne=F2.NumLigne-1			
    INNER JOIN DetailsGammesProduction DP1
        ON F1.NumFicheProduction =DP1.NumFicheProduction and F1.NumLigne =DP1.NumLigne
    LEFT OUTER JOIN DetailsGammesProduction DP2
        ON F2.NumFicheProduction =DP2.NumFicheProduction and F2.NumLigne =DP2.NumLigne			
    INNER JOIN Zones Z1         on Z1.NumZone=DP1.NumZone
    LEFT OUTER JOIN  Zones Z2         on Z2.NumZone=DP2.NumZone
    -- INNER JOIN TempsDeplacements TD         on Z2.NumZone=TD.arrivee and Z1.NumZone=TD.depart
    INNER JOIN DetailsChargesProduction DC on DC.NumLigne=1 and F1.NumFicheProduction =DC.NumFicheProduction
where F1.NumFicheProduction='00088320'-- and     ABS(DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)-   DP1.TempsEgouttageSecondes	-    TD.normal+9 )>10

select 
    Z1.NumZone as depart,Z2.NumZone as arrivee,
    DC.vitesse_bas,DC.vitesse_haut,
    F.TempsDeplacement, TD.normal 
from 
    DetailsFichesProduction  F
    INNER JOIN Postes P1
        on P1.NumPoste=F.NumPostePrecedent
    INNER JOIN Zones Z1
        on Z1.NumZone=P1.NumZone
    INNER JOIN Postes P2
        on P2.NumPoste=F.NumPoste
    INNER JOIN Zones Z2
        on Z2.NumZone=P2.NumZone
    INNER JOIN TempsDeplacements TD
        on Z2.NumZone=TD.arrivee and Z1.NumZone=TD.depart
    INNER JOIN DetailsChargesProduction DC
        on DC.NumLigne=1 and F.NumFicheProduction =DC.NumFicheProduction
    

where F.NumFicheProduction='00088320' 


select 
    distinct  DG.numficheproduction as [N° OF], 	DC.NumGammeANodisation as [gamme ],DC.NumBarre as  [barre] ,
    dbo.hasBadCalibrage(DG.numficheproduction)
from   	
    [DetailsGammesProduction]  DG 	
    LEFT OUTER JOIN   [DetailsFichesProduction] DF 	on   		
        DG.numficheproduction=DF.numficheproduction COLLATE FRENCH_CI_AS and 		
        DG.numligne=DF.NumLigne  and DF.NumLigne=1 	
    INNER JOIN ( select distinct numficheproduction,NumGammeANodisation,NumBarre from [DetailsChargesProduction] where numligne=1
    ) DC 	
    on   		DC.numficheproduction=DF.numficheproduction  COLLATE FRENCH_CI_AS 
WHERE		DF.DateEntreePoste >=  '20250113'  and DF.DateEntreePoste < '20250114'      
order by DG.numficheproduction ,DC.NumBarre

Update  TD
SET normal=F.TempsDeplacement + dbo.getOffset(DC.vitesse_bas,DC.vitesse_haut)
FROM 
	DetailsFichesProduction  F
    INNER JOIN DetailsChargesProduction DC
        ON DC.NumLigne=1 and DC.NumFicheProduction=F.NumFicheProduction      
	INNER JOIN Postes P1
        on P1.NumPoste=F.NumPostePrecedent
    INNER JOIN Zones Z1
        on Z1.NumZone=P1.NumZone
    INNER JOIN Postes P2
        on P2.NumPoste=F.NumPoste
    INNER JOIN Zones Z2
        on Z2.NumZone=P2.NumZone
    INNER JOIN TempsDeplacements TD
        on Z2.NumZone=TD.arrivee and Z1.NumZone=TD.depart
		
	
	where F.NumFicheProduction='00088320' 
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

select * from  ANODISATION.dbo.DetailsFichesProduction F where NumFicheProduction='00089115' and numposte=26;
select NumLigne,NumPosteReel 
from  ANODISATION.dbo.DetailsGammesProduction F where NumFicheProduction='00089115'
and  NumPosteReel>0;




---------------------------------------------------------------------------------------
-- INIT TPS DEP
---------------------------------------------------------------------------------------
UPDATE ANODISATION.dbo.DetailsFichesProduction 
SET TempsDeplacement =0 ,NumPostePrecedent=0


UPDATE F2
SET F2.TempsDeplacement = DATEDIFF(SECOND, F1.DateSortiePoste, F2.DateEntreePoste)
-DATEDIFF(SECOND, F1.DateDebutEgouttage,  F1.DateFinEgouttage),
    F2.NumPostePrecedent = F1.NumPoste
FROM ANODISATION.dbo.DetailsFichesProduction F1
JOIN ANODISATION.dbo.DetailsFichesProduction F2
    ON F1.NumFicheProduction = F2.NumFicheProduction
    AND F1.NumLigne = F2.NumLigne - 1
    AND CONVERT(DATE, F1.DateDebutEgouttage) = CONVERT(DATE, F1.DateFinEgouttage)
    AND CONVERT(DATE, F1.DateSortiePoste) = CONVERT(DATE, F2.DateEntreePoste); 