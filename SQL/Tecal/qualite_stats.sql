use ANODISATION;



SELECT
    G1.numficheproduction,
    Z.CodeZone,
    CASE
        WHEN   DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-G1.TempsAuPosteSecondes <0
        THEN 1*(G1.TempsAuPosteSecondes-DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste))
        ELSE DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-Z.derive- G1.TempsAuPosteSecondes
    END,
    G1.TempsAuPosteSecondes


from
    [DetailsGammesProduction]  G1
    LEFT OUTER JOIN [DetailsGammesProduction]  G2
        on G1.numficheproduction=G2.numficheproduction
        and G1.NumLigne+1=G2.NumLigne
    RIGHT OUTER JOIN [DetailsFichesProduction] F1
        on  G1.numficheproduction=F1.numficheproduction
    LEFT OUTER JOIN   [DetailsFichesProduction] F2
        on G1.numficheproduction=F2.numficheproduction
        and F1.NumLigne+1=F2.NumLigne
    INNER JOIN ZONES  Z on G1.Numzone=Z.Numzone
    WHERE 	G1.NumPosteReel=F1.NumPoste and G2.NumPosteReel=F2.NumPoste
        AND
        Z.NumZone in (3,4,9,13,14,16)
        AND 
        G1.TempsAuPosteSecondes>0
        AND
        (
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)>
            (G1.TempsAuPosteSecondes +Z.derive+20)
            OR
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)<(G1.TempsAuPosteSecondes -20)
        )
         AND F1.NumFicheProduction in (
            select distinct NumFicheProduction from DetailsFichesProduction
            where DateEntreePoste >= '20240204'    AND DateSortiePoste < '20250215'
        )
        

ORDER BY G1.NumFicheProduction

select * from DetailsGammesProduction where numficheproduction='00081439'
select * from DetailsFichesProduction where numficheproduction='00081346'

DECLARE @DateDebut DATE = '2024-02-04';
DECLARE @DateFin DATE = '2025-02-15';

SELECT

    CONVERT(DATE, F1.DateEntreePoste) AS Day,
    SUM(CASE
        WHEN DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste) - G1.TempsAuPosteSecondes < 0
        THEN 1 * (G1.TempsAuPosteSecondes - DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste))
        WHEN DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste) > (G1.TempsAuPosteSecondes + Z.derive + 20)
        THEN DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste) - Z.derive - G1.TempsAuPosteSecondes
        ELSE 0
    END) * 100.0 / 
    SUM(G1.TempsAuPosteSecondes
    ) AS TX_ERREUR
FROM
    [DetailsGammesProduction] G1
    LEFT OUTER JOIN [DetailsGammesProduction] G2
        ON G1.numficheproduction = G2.numficheproduction
        AND G1.NumLigne + 1 = G2.NumLigne
    RIGHT OUTER JOIN [DetailsFichesProduction] F1
        ON G1.numficheproduction = F1.numficheproduction
    LEFT OUTER JOIN [DetailsFichesProduction] F2
        ON G1.numficheproduction = F2.numficheproduction
        AND F1.NumLigne + 1 = F2.NumLigne
    INNER JOIN ZONES Z
        ON G1.Numzone = Z.Numzone
   
WHERE  
    G1.NumPosteReel = F1.NumPoste 
    AND G2.NumPosteReel = F2.NumPoste
    AND Z.NumZone IN (3, 4, 9, 13, 14, 16)
    AND G1.TempsAuPosteSecondes > 0
   
    AND F1.NumFicheProduction IN (
        SELECT DISTINCT NumFicheProduction 
        FROM DetailsFichesProduction
        WHERE DateEntreePoste >= @DateDebut AND DateSortiePoste < @DateFin
    )
GROUP BY
    CONVERT(DATE, F1.DateEntreePoste)
ORDER BY  Day;