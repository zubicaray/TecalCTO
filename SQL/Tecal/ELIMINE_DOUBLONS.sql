USE ANODISATION;
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

USE ANODISATION;
WITH CTE AS (
    SELECT 
        ClePrimaire, 
        NumFicheProduction, 
        DateEntreePoste, 
        NumLigne,
        ROW_NUMBER() OVER (
            PARTITION BY NumFicheProduction, DateEntreePoste, NumLigne 
            ORDER BY ClePrimaire
        ) AS rn
    FROM dbo.DetailsFichesProduction
)
DELETE FROM dbo.DetailsFichesProduction
WHERE ClePrimaire IN (
    SELECT ClePrimaire FROM CTE WHERE rn > 1
);

USE ANODISATION;
WITH CTE AS (
    SELECT 
        ClePrimaire, 
        NumFicheProduction,          
        NumLigne,
        ROW_NUMBER() OVER (
            PARTITION BY NumFicheProduction,  NumLigne 
            ORDER BY ClePrimaire
        ) AS rn
    FROM dbo.DetailsGammesProduction
)
DELETE FROM dbo.DetailsGammesProduction
WHERE ClePrimaire IN (
    SELECT ClePrimaire FROM CTE WHERE rn > 1
);


