USE ANODISATION;

DROP FUNCTION [dbo].[hasBadCalibrage]
GO

CREATE  FUNCTION hasBadCalibrage(@NumFicheProduction VARCHAR(8))
RETURNS BIT
AS
BEGIN
    DECLARE @count INT;
    DECLARE @OFFSET_BAS INT; 
    DECLARE @OFFSET_HAUT INT;
    DECLARE @result BIT;

    -- Récupération des valeurs d'offset
    SELECT @OFFSET_BAS = Vbas.valeur, @OFFSET_HAUT = Vhaut.valeur
    FROM vitesse_bas Vbas
    INNER JOIN DetailsChargesProduction DC 
        ON DC.NumLigne = 1 
        AND @NumFicheProduction = DC.NumFicheProduction
        AND DC.vitesse_bas = Vbas.id
    INNER JOIN vitesse_haut Vhaut
        ON DC.vitesse_haut = Vhaut.id;

    -- Vérification du calibrage
    SELECT @count = COUNT(*)
    FROM DetailsFichesProduction F
    INNER JOIN Postes P1 ON P1.NumPoste = F.NumPostePrecedent
    INNER JOIN Zones Z1 ON Z1.NumZone = P1.NumZone
    INNER JOIN Postes P2 ON P2.NumPoste = F.NumPoste
    INNER JOIN Zones Z2 ON Z2.NumZone = P2.NumZone
    INNER JOIN TempsDeplacements TD ON Z2.NumZone = TD.arrivee AND Z1.NumZone = TD.depart
    WHERE F.NumFicheProduction = @NumFicheProduction
      AND ABS(F.TempsDeplacement - (TD.normal + @OFFSET_BAS + @OFFSET_HAUT)) > 10;

    -- Détermination du résultat
    SET @result = CASE WHEN @count > 0 THEN 1 ELSE 0 END;

    RETURN @result;
END;

