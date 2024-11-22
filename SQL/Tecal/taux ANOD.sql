use ANODISATION;
DECLARE @DateDebut DATETIME;
DECLARE @DateFin DATETIME;

-- Récupération des limites dynamiques
SELECT 
    @DateDebut = MIN(DateEntreePoste),
    @DateFin = MAX(DateSortiePoste)
FROM DetailsFichesProduction
WHERE NumPoste IN (18, 19, 20) 
and  NumFicheProduction  in('00086778');

-- Calcul du taux d'occupation
WITH CTE_Durees AS (
    SELECT
        NumPoste,
        -- Calcul de la durée réelle d'occupation pour chaque enregistrement
        DATEDIFF(SECOND, 
            CASE 
                WHEN DateEntreePoste < @DateDebut THEN @DateDebut 
                ELSE DateEntreePoste 
            END,
            CASE 
                WHEN DateSortiePoste > @DateFin THEN @DateFin 
                ELSE DateSortiePoste 
            END
        ) AS DureeOccupation
    FROM DetailsFichesProduction
    WHERE NumPoste IN (18, 19, 20) -- Postes concernés
      AND DateSortiePoste > @DateDebut -- Exclure les enregistrements terminés avant la période
      AND DateEntreePoste < @DateFin   -- Exclure les enregistrements commençant après la période
      AND NumFicheProduction  in('00086778')
)
SELECT
    NumPoste,
    SUM(DureeOccupation) AS DureeTotaleOccupation, -- Total de la durée occupée (en secondes)
    DATEDIFF(SECOND, @DateDebut, @DateFin) AS DureeTotalePeriode, -- Durée de la période en secondes
    CAST(SUM(DureeOccupation) * 100.0 / NULLIF(DATEDIFF(SECOND, @DateDebut, @DateFin), 0) AS DECIMAL(10, 2)) AS TauxOccupationPourcentage
FROM CTE_Durees
GROUP BY NumPoste
ORDER BY NumPoste;
