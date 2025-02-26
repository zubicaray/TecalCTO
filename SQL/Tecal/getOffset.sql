
CREATE  FUNCTION getOffset(@vitesse_bas INT,@vitesse_haut INT)
RETURNS INT
AS
BEGIN
    DECLARE @OFFSET INT; 
   
    -- Récupération des valeurs d'offset
    SELECT @OFFSET = Vbas.valeur
    FROM vitesse_bas Vbas
    WHERE Vbas.id = @vitesse_bas ;

    SELECT @OFFSET +=  Vhaut.valeur FROM vitesse_haut Vhaut
    WHERE Vhaut.id = @vitesse_haut ;

    RETURN @OFFSET 
END;


