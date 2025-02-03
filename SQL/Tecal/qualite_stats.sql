use ANODISATION;

select 
    F1.DateEntreePoste,F1.numficheproduction,F1.NumLigne,Z.CodeZone,
    DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste),Z.derive,
    G1.TempsAuPosteSecondes ,
    CASE 
        WHEN   DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-G1.TempsAuPosteSecondes <0 
        THEN 2*(G1.TempsAuPosteSecondes-DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste))
        ELSE DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-Z.derive- G1.TempsAuPosteSecondes 
    END AS TPS_ECART
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
        Z.NumZone not in (1,35) AND
        F1.numficheproduction in 
        ('00085171','00085172','00085173',
        '00085174','00085175','00085176','00085177','00085178','00085179','00085180',
        '00085181','00085182','00085183','00085184','00085185','00085186','00085187','00085188','00085189',
        '00085191','00085192','00085193','00085194','00085195','00085196','00085197','00085198','00085190',    
        '00085199','00085200','00085201','00085202','00085203','00085204','00085205','00085206','00085207',
        '00085208','00085209','00085210','00085211','00085212','00085213','00085214','00085215','00085216')   
        AND
        (
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)>
            (G1.TempsAuPosteSecondes +Z.derive+20) 
            OR
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)<(G1.TempsAuPosteSecondes -20)
        )
        AND F1.DateEntreePoste >= '2024-07-18'    AND F1.DateSortiePoste <='2024-07-19' 
        order by F1.numficheproduction, F1.NumLigne



DECLARE @DateDebut DATE = '20230204';
DECLARE @DateFin DATE = '20240203';

SELECT
    DATEPART(YEAR, F1.DateEntreePoste) AS Year,
    DATEPART(WEEK, F1.DateEntreePoste) AS WeekNumber,
        SUM(CASE
            WHEN   DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-G1.TempsAuPosteSecondes <0
            THEN 1*(G1.TempsAuPosteSecondes-DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste))
            ELSE DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)-Z.derive- G1.TempsAuPosteSecondes
        END )* 100.0
        / SUM(G1.TempsAuPosteSecondes)   as TX_ERREUR


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
        Z.NumZone not in (1,35)
        AND
        (
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)>
            (G1.TempsAuPosteSecondes +Z.derive+20)
            OR
            DATEDIFF(SECOND, F1.DateEntreePoste, F1.DateSortiePoste)<(G1.TempsAuPosteSecondes -20)
        )
        --AND F1.DateEntreePoste >= @DateDebut    AND F1.DateSortiePoste < @DateFin
GROUP BY
    DATEPART(YEAR, F1.DateEntreePoste),
    DATEPART(WEEK, F1.DateEntreePoste)
ORDER BY Year, WeekNumber;
