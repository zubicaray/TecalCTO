use ANODISATION;
--select * from [ANODISATION].[dbo].[LOGS_CPO];


SELECT Z.NumZone,idbarre, Z.CodeZone, label, entree, sortie
FROM   Zones Z   INNER JOIN LOGS_CPO L
ON L.NumZone = Z.NumZone and idbarre in (7,9) and L.NumZone=15 and CAST(date_log AS DATE) ='2024-11-30'
ORDER BY entree

select distinct numzone,codezone from zones order by numzone;


truncate table LOGS_CPO;
--DROP  TABLE LOGS_CPO ;

CREATE TABLE LOGS_CPO (
    date_log DATETIME NOT NULL,               -- Clé primaire (partie 1)
    idbarre NUMERIC(10, 0) NOT NULL,      -- Clé primaire (partie 2)
    label VARCHAR(255) NULL,              -- Champ texte
    idZone INT NOT NULL,  
    NumZone INT NOT NULL,                     -- Champ entier
    entree TIME NOT NULL,                     -- Heure d'entrée
    sortie TIME NOT NULL,                     -- Heure de sortie,
    PRIMARY KEY (date_log, idbarre,idZone)       -- Clé primaire composée
);

-- Index supplémentaire (facultatif, car idbarre fait déjà partie de la clé primaire)
CREATE INDEX idx_label ON LOGS_CPO (label);



SELECT 
    l1.NumZone AS NumZone1,
    l2.NumZone AS NumZone2,
    l1.entree AS Entree1,
    l1.sortie AS Sortie1,
    l2.entree AS Entree2,
    l2.sortie AS Sortie2
FROM 
    LOGS_CPO l1
JOIN 
    LOGS_CPO l2
ON 
    l1.NumZone = l2.NumZone -- Même NumZone
    AND l1.date_log = l2.date_log -- Même jour
    AND l1.idbarre < l2.idbarre -- Éviter les doublons et comparer différentes barres
    AND    DATEDIFF (SECONDS ,l1.entree ,l2.sortie) >0 -- Vérifie le chevauchement
       
    
WHERE     CAST(l1.date_log AS DATE) = '2024-11-30' -- Filtrer par date
ORDER BY 
    l1.NumZone, l1.entree, l2.entree;