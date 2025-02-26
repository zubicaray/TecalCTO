WITH CTE AS (
    SELECT 
        ClePrimaire, 
        NumCommandeInterne, 
        DateEntreeEnLigne, 
        NumLigne,
        ROW_NUMBER() OVER (
            PARTITION BY NumCommandeInterne, DateEntreeEnLigne, NumLigne 
            ORDER BY ClePrimaire
        ) AS rn
    FROM dbo.DetailsChargesProduction
)
DELETE FROM dbo.DetailsChargesProduction
WHERE ClePrimaire IN (
    SELECT ClePrimaire FROM CTE WHERE rn > 1
);
