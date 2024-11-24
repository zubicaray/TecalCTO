use ANODISATION;
DECLARE @DateDebut DATE = '2024-11-01'; -- Remplacez par votre date de début
DECLARE @DateFin DATE = '2024-12-14';   -- Remplacez par votre date de fin

SELECT
    J,DureeOccupation,DureeOccupation*100/DureeMaxPossible
FROM (
    SELECT
        CAST(DateEntreePoste AS DATE ) as J,
        DATEDIFF(SECOND, Min(DateEntreePoste) ,max(DateSortiePoste))*3 AS DureeMaxPossible,
        SUM(DATEDIFF(SECOND, DateEntreePoste ,DateSortiePoste         )) AS DureeOccupation

    FROM DetailsFichesProduction
    WHERE NumPoste IN (18, 19, 20) -- Postes concernés
        AND DateEntreePoste >= @DateDebut -- Exclure les enregistrements terminés avant la période
        AND DateSortiePoste < @DateFin   -- Exclure les enregistrements commençant après la période
    GROUP BY 
        CAST(DateEntreePoste AS DATE)
    HAVING 
        COUNT(*) > 0 -- Elimine les jours sans occupation
) T

SELECT
         CAST(DateEntreePoste AS DATE),
        -- Calcul de la durée réelle d'occupation pour chaque enregistrement
        SUM(DATEDIFF(SECOND, DateEntreePoste ,DateSortiePoste         )) AS DureeOccupation
    FROM DetailsFichesProduction
    WHERE NumPoste IN (18, 19, 20) -- Postes concernés
      AND DateEntreePoste >= @DateDebut -- Exclure les enregistrements terminés avant la période
      AND DateSortiePoste < @DateFin   -- Exclure les enregistrements commençant après la période
    GROUP BY 
        CAST(DateEntreePoste AS DATE)
    HAVING 
        COUNT(*) > 0 -- Elimine les jours sans occupation


WITH CTE_Durees AS (
    SELECT
        
        -- Calcul de la durée réelle d'occupation pour chaque enregistrement
        DATEDIFF(SECOND, 
            CASE 
                WHEN DateEntreePoste > @DateDebut THEN @DateDebut 
                ELSE DateEntreePoste 
            END,
            CASE 
                WHEN DateSortiePoste < @DateFin THEN @DateFin 
                ELSE DateSortiePoste 
            END
        ) AS DureeOccupation
    FROM DetailsFichesProduction
    WHERE NumPoste IN (18, 19, 20) -- Postes concernés
      AND DateEntreePoste > @DateDebut -- Exclure les enregistrements terminés avant la période
      AND DateSortiePoste < @DateFin   -- Exclure les enregistrements commençant après la période
      AND NumPoste in (18,19,20 )
    GROUP BY 
        CAST(DateEntreePoste AS DATE)
    HAVING 
        COUNT(*) > 0 -- Elimine les jours sans occupation
)


SELECT 
    CAST(DateEntreePoste AS DATE) AS Jour,
    MIN(DateEntreePoste) AS DebutOccupation,
    MAX(DateSortiePoste) AS FinOccupation,
    DATEDIFF(MINUTE, MIN(DateEntreePoste), MAX(DateSortiePoste)) AS DureeOccupationMinutes,
    CAST((DATEDIFF(MINUTE, MIN(DateEntreePoste), MAX(DateSortiePoste)) / 1440.0) * 100 AS DECIMAL(5, 2)) AS TauxOccupationPourcentage
FROM 
    DetailsFichesProduction
WHERE 
    NumPoste in (18,19,20 ) AND
    CAST(DateEntreePoste AS DATE) BETWEEN @DateDebut AND @DateFin
GROUP BY 
    CAST(DateEntreePoste AS DATE)
HAVING 
    COUNT(*) > 0 -- Elimine les jours sans occupation
ORDER BY 
    Jour;